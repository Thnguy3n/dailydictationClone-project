package com.example.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {
    private final ServerOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return defaultResolver.resolve(exchange)
                .flatMap(authorizationRequest -> customizeAuthorizationRequest(exchange, authorizationRequest));
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        return defaultResolver.resolve(exchange, clientRegistrationId)
                .flatMap(authorizationRequest -> customizeAuthorizationRequest(exchange, authorizationRequest));
    }

    private Mono<OAuth2AuthorizationRequest> customizeAuthorizationRequest(
            ServerWebExchange exchange,
            OAuth2AuthorizationRequest authorizationRequest) {

        if (authorizationRequest == null) {
            return Mono.empty();
        }

        // Lấy device info từ query parameters của request gốc
        String deviceId = exchange.getRequest().getQueryParams().getFirst("device_id");
        String deviceName = exchange.getRequest().getQueryParams().getFirst("device_name");
        String platform = exchange.getRequest().getQueryParams().getFirst("platform");

        if (!StringUtils.hasText(deviceId)) {
            deviceId = generateDeviceId(platform, exchange);
        }

        if (!StringUtils.hasText(deviceName)) {
            deviceName = generateDeviceName(platform, exchange);
        }

        if (!StringUtils.hasText(platform)) {
            platform = detectPlatform(exchange);
        }

        log.info("Customizing OAuth2 request - deviceId: {}, deviceName: {}, platform: {}",
                deviceId, deviceName, platform);

        String originalState = authorizationRequest.getState();
        String customState = encodeDeviceInfoToState(originalState, deviceId, deviceName, platform);

        OAuth2AuthorizationRequest customRequest = OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(customState)
                .build();

        log.info("Created custom OAuth2 request with state: {}", customState);
        return Mono.just(customRequest);
    }

    private String generateDeviceId(String platform, ServerWebExchange exchange) {
        // Lấy thông tin từ headers để tạo device ID unique hơn
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "";

        // Tạo hash từ các thông tin có sẵn
        String baseInfo = (userAgent != null ? userAgent : "") +
                (xForwardedFor != null ? xForwardedFor : "") +
                remoteAddress;

        String prefix = StringUtils.hasText(platform) ? platform : "unknown";
        String uniqueId = UUID.nameUUIDFromBytes(baseInfo.getBytes()).toString().substring(0, 8);

        return prefix + "_" + uniqueId;
    }

    private String generateDeviceName(String platform, ServerWebExchange exchange) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();

            // Detect từ User-Agent
            if (userAgent.contains("flutter")) {
                return "Flutter Mobile App";
            } else if (userAgent.contains("android")) {
                return "Android Device";
            } else if (userAgent.contains("iphone") || userAgent.contains("ios")) {
                return "iOS Device";
            } else if (userAgent.contains("chrome")) {
                return "Chrome Browser";
            } else if (userAgent.contains("firefox")) {
                return "Firefox Browser";
            } else if (userAgent.contains("safari")) {
                return "Safari Browser";
            }
        }

        // Fallback based on platform
        if (StringUtils.hasText(platform)) {
            switch (platform.toLowerCase()) {
                case "android":
                    return "Android Device";
                case "ios":
                    return "iOS Device";
                case "mobile":
                    return "Mobile Device";
                case "web":
                    return "Web Browser";
                default:
                    return "Unknown Device";
            }
        }

        return "Unknown Device";
    }

    private String detectPlatform(ServerWebExchange exchange) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();

            if (userAgent.contains("flutter") || userAgent.contains("dart")) {
                return "mobile";
            }
            if (userAgent.contains("android")) {
                return "android";
            }
            if (userAgent.contains("iphone") || userAgent.contains("ios")) {
                return "ios";
            }
            if (userAgent.contains("mobile")) {
                return "mobile";
            }
        }

        return "web";
    }

    private String encodeDeviceInfoToState(String originalState, String deviceId, String deviceName, String platform) {
        try {
            Map<String, String> stateData = new HashMap<>();
            stateData.put("original_state", originalState != null ? originalState : "");
            stateData.put("device_id", deviceId);
            stateData.put("device_name", deviceName);
            stateData.put("platform", platform);
            stateData.put("timestamp", String.valueOf(System.currentTimeMillis()));

            // Encode to Base64
            String json = new ObjectMapper().writeValueAsString(stateData);
            String encodedState = Base64.getEncoder().encodeToString(json.getBytes());

            log.info("Encoded state data: {}", json);
            log.info("Final encoded state: {}", encodedState);

            return encodedState;
        } catch (Exception e) {
            log.error("Error encoding device info to state", e);
            return originalState != null ? originalState : UUID.randomUUID().toString();
        }
    }

    public static Map<String, String> decodeDeviceInfoFromState(String encodedState) {
        try {
            log.info("Decoding state: {}", encodedState);
            String json = new String(Base64.getDecoder().decode(encodedState));
            log.info("Decoded JSON from state: {}", json);

            Map<String, String> result = new ObjectMapper().readValue(json, Map.class);
            log.info("Parsed state data: {}", result);

            return result;
        } catch (Exception e) {
            log.error("Error decoding device info from state: {}", encodedState, e);
            return new HashMap<>();
        }
    }
}