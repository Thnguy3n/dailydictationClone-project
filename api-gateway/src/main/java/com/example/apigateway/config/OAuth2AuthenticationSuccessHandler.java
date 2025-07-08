package com.example.apigateway.config;

import com.example.apigateway.service.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final WebClient.Builder webClientBuilder;

    @Value("${APP_OAUTH2_REDIRECT_WEB_SUCCESS:http://localhost:3000/auth/callback}")
    private String webSuccessUrl;

    @Value("${APP_OAUTH2_REDIRECT_WEB_ERROR:http://localhost:3000/auth/error}")
    private String webErrorUrl;

    @Value("${APP_OAUTH2_REDIRECT_MOBILE_SUCCESS:myapp://auth/callback}")
    private String mobileSuccessUrl;

    @Value("${APP_OAUTH2_REDIRECT_MOBILE_ERROR:myapp://auth/error}")
    private String mobileErrorUrl;

    @Value("${APP_SERVICES_USER_SERVICE_URL:http://user-service}")
    private String userServiceUrl;

    @Value("${APP_OAUTH2_TIMEOUT:5000}")
    private int timeoutMs;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        ServerWebExchange exchange = webFilterExchange.getExchange();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String pictureUrl = oAuth2User.getAttribute("picture");

        log.info("OAuth2 authentication success for email: {}", email);

        DeviceInfo deviceInfo = extractDeviceInfoFromState(exchange);
        log.info("Extracted device info: platform={}, deviceId={}, deviceName={}",
                deviceInfo.platform, deviceInfo.deviceId, deviceInfo.deviceName);

        return registerOrUpdateUser(email, name, pictureUrl, deviceInfo)
                .flatMap(response -> {
                    // Luôn luôn redirect cho cả mobile và web
                    return handleAuthenticationResponse(exchange, response, deviceInfo);
                })
                .onErrorResume(error -> handleError(exchange, error, deviceInfo));
    }

    private DeviceInfo extractDeviceInfoFromState(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        log.info("Received state parameter: {}", state);

        if (state != null) {
            try {
                Map<String, String> stateData = CustomOAuth2AuthorizationRequestResolver.decodeDeviceInfoFromState(state);
                log.info("Decoded state data: {}", stateData);

                return new DeviceInfo(
                        stateData.get("device_id"),
                        stateData.get("device_name"),
                        stateData.get("platform"),
                        stateData.get("original_state")
                );
            } catch (Exception e) {
                log.error("Error extracting device info from state", e);
            }
        }

        // Fallback to legacy detection
        String platform = detectPlatform(exchange);
        log.info("Fallback platform detection: {}", platform);

        return new DeviceInfo(
                generateDeviceId(platform),
                platform.equals("mobile") ? "Mobile Device" : "Web Browser",
                platform,
                null
        );
    }

    private String generateDeviceId(String platform) {
        return platform + "_" + System.currentTimeMillis();
    }

    private String detectPlatform(ServerWebExchange exchange) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String platform = exchange.getRequest().getQueryParams().getFirst("platform");

        if (StringUtils.hasText(platform)) {
            return platform.toLowerCase();
        }

        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("flutter") || userAgent.contains("dart")) {
                return "mobile";
            }
            if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
                return "mobile";
            }
        }

        return "web";
    }

    private Mono<Void> handleAuthenticationResponse(ServerWebExchange exchange,
                                                    Map<String, Object> response,
                                                    DeviceInfo deviceInfo) {

        if (response.containsKey("error")) {
            log.error("User registration/update failed: {}", response.get("error"));
            return redirectToError(exchange, deviceInfo, (String) response.get("error"));
        }

        String jwtToken = (String) response.get("token");
        if (!StringUtils.hasText(jwtToken)) {
            log.error("No JWT token received from user service");
            return redirectToError(exchange, deviceInfo, "Authentication failed");
        }

        log.info("JWT token received, redirecting to success URL");
        return redirectToSuccess(exchange, deviceInfo, jwtToken);
    }

    private Mono<Void> redirectToSuccess(ServerWebExchange exchange,
                                         DeviceInfo deviceInfo,
                                         String jwtToken) {

        String baseUrl = "mobile".equals(deviceInfo.platform) ? mobileSuccessUrl : webSuccessUrl;
        log.info("Base redirect URL: {} for platform: {}", baseUrl, deviceInfo.platform);

        Map<String, String> params = new HashMap<>();
        params.put("token", jwtToken);
        params.put("platform", deviceInfo.platform);
        params.put("success", "true");

        if (deviceInfo.deviceId != null) {
            params.put("device_id", deviceInfo.deviceId);
        }
        if (deviceInfo.deviceName != null) {
            params.put("device_name", deviceInfo.deviceName);
        }

        String redirectUrl = buildRedirectUrl(baseUrl, params);
        log.info("Final redirect URL: {}", redirectUrl);

        return performRedirect(exchange, redirectUrl);
    }

    private Mono<Void> redirectToError(ServerWebExchange exchange,
                                       DeviceInfo deviceInfo,
                                       String error) {

        String baseUrl = "mobile".equals(deviceInfo.platform) ? mobileErrorUrl : webErrorUrl;

        Map<String, String> params = new HashMap<>();
        params.put("error", error);
        params.put("platform", deviceInfo.platform);
        params.put("success", "false");

        String redirectUrl = buildRedirectUrl(baseUrl, params);
        log.info("Error redirect URL: {}", redirectUrl);

        return performRedirect(exchange, redirectUrl);
    }

    private String buildRedirectUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.hasText(entry.getValue())) {
                urlBuilder.append(hasQuery ? "&" : "?");
                urlBuilder.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                hasQuery = true;
            }
        }

        return urlBuilder.toString();
    }

    private Mono<Void> performRedirect(ServerWebExchange exchange, String redirectUrl) {
        log.info("Performing redirect to: {}", redirectUrl);

        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));

        return exchange.getResponse().setComplete();
    }

    private Mono<Void> handleError(ServerWebExchange exchange,
                                   Throwable error,
                                   DeviceInfo deviceInfo) {

        log.error("OAuth2 authentication error: {}", error.getMessage(), error);
        return redirectToError(exchange, deviceInfo, "Internal server error");
    }

    private Mono<Map<String, Object>> registerOrUpdateUser(String email, String name, String pictureUrl, DeviceInfo deviceInfo) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("name", name != null ? name : "");
        requestBody.put("pictureUrl", pictureUrl != null ? pictureUrl : "");

        // Thêm device info vào request body để gửi tới user-service
        requestBody.put("deviceId", deviceInfo.deviceId);
        requestBody.put("deviceName", deviceInfo.deviceName);
        requestBody.put("platform", deviceInfo.platform);

        log.info("Registering/updating user: {} with device info", email);

        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/api/auth/oauth2-login")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(response -> response.is4xxClientError() || response.is5xxServerError(), clientResponse -> {
                    log.error("User service returned error status: {}", clientResponse.statusCode());
                    return Mono.error(new RuntimeException("User service error: " + clientResponse.statusCode()));
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnError(error -> log.error("Error during user registration or update: {}", error.getMessage()))
                .onErrorReturn(Map.of("error", "User registration failed"));
    }

    private static class DeviceInfo {
        final String deviceId;
        final String deviceName;
        final String platform;
        final String originalState;

        DeviceInfo(String deviceId, String deviceName, String platform, String originalState) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.platform = platform != null ? platform : "web";
            this.originalState = originalState;
        }
    }
}