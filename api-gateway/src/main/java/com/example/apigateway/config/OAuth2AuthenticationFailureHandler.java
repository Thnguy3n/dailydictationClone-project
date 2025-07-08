//package com.example.apigateway.config;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.server.WebFilterExchange;
//import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@Slf4j
//public class OAuth2AuthenticationFailureHandler implements ServerAuthenticationFailureHandler {
//    @Value("${APP_OAUTH2_REDIRECT_WEB_ERROR:http://localhost:3000/auth/error}")
//    private String webErrorUrl;
//    @Value("${APP_OAUTH2_REDIRECT_MOBILE_ERROR:myapp://auth/error}")
//    private String mobileErrorUrl;
//    @Override
//    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
//        ServerWebExchange exchange = webFilterExchange.getExchange();
//
//        log.error("OAuth2 authentication failed", exception);
//
//        // Extract device info from state
//        String state = exchange.getRequest().getQueryParams().getFirst("state");
//        String platform = "web"; // default
//
//        if (state != null) {
//            try {
//                Map<String, String> stateData = CustomOAuth2AuthorizationRequestResolver.decodeDeviceInfoFromState(state);
//                platform = stateData.getOrDefault("platform", "web");
//            } catch (Exception e) {
//                log.error("Error extracting platform from state", e);
//            }
//        }
//
//        String baseUrl = "mobile".equals(platform) ? mobileErrorUrl : webErrorUrl;
//
//        Map<String, String> params = new HashMap<>();
//        params.put("error", "authentication_failed");
//        params.put("error_description", exception.getMessage());
//        params.put("platform", platform);
//
//        String redirectUrl = buildRedirectUrl(baseUrl, params);
//
//        log.info("Redirecting to error URL: {}", redirectUrl);
//
//        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
//        exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));
//
//        return exchange.getResponse().setComplete();
//    }
//
//    private String buildRedirectUrl(String baseUrl, Map<String, String> params) {
//        StringBuilder urlBuilder = new StringBuilder(baseUrl);
//        boolean hasQuery = baseUrl.contains("?");
//
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            if (entry.getValue() != null) {
//                urlBuilder.append(hasQuery ? "&" : "?");
//                urlBuilder.append(entry.getKey()).append("=")
//                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
//                hasQuery = true;
//            }
//        }
//
//        return urlBuilder.toString();
//    }
//}
