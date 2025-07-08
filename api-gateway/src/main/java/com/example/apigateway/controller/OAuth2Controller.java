//package com.example.apigateway.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/oauth2")
//@RequiredArgsConstructor
//@Slf4j
//public class OAuth2Controller {
//
//    /**
//     * Endpoint để xử lý callback từ Google OAuth2
//     * Được gọi bởi Google sau khi user authorize
//     */
//    @GetMapping("/callback/google")
//    public Mono<String> handleGoogleCallback(
//            @RequestParam(value = "code", required = false) String code,
//            @RequestParam(value = "state", required = false) String state,
//            @RequestParam(value = "error", required = false) String error,
//            ServerWebExchange exchange) {
//
//        log.info("Received Google OAuth2 callback - code: {}, state: {}, error: {}",
//                code != null ? "present" : "null", state, error);
//
//        if (error != null) {
//            log.error("OAuth2 callback error: {}", error);
//            return Mono.just("OAuth2 Error: " + error);
//        }
//
//        if (code == null) {
//            log.error("OAuth2 callback missing authorization code");
//            return Mono.just("OAuth2 Error: Missing authorization code");
//        }
//
//        // Redirect to the standard OAuth2 login processing endpoint
//        // Spring Security sẽ tự động xử lý authorization code
//        String redirectUrl = "/login/oauth2/code/google?code=" + code;
//        if (state != null) {
//            redirectUrl += "&state=" + state;
//        }
//
//        log.info("Redirecting to Spring Security OAuth2 processor: {}", redirectUrl);
//
//        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
//        exchange.getResponse().getHeaders().setLocation(java.net.URI.create(redirectUrl));
//
//        return exchange.getResponse().setComplete().then(Mono.just("Redirecting..."));
//    }
//
//    /**
//     * Endpoint để khởi tạo Google OAuth2 flow
//     * Được gọi từ Flutter app
//     */
//    @GetMapping("/authorization/google")
//    public Mono<Void> initiateGoogleOAuth2(
//            @RequestParam(value = "platform", defaultValue = "web") String platform,
//            @RequestParam(value = "device_id", required = false) String deviceId,
//            @RequestParam(value = "device_name", required = false) String deviceName,
//            ServerWebExchange exchange) {
//
//        log.info("Initiating Google OAuth2 flow for platform: {}, deviceId: {}, deviceName: {}",
//                platform, deviceId, deviceName);
//
//        // Redirect to Spring Security OAuth2 authorization endpoint
//        String redirectUrl = "/oauth2/authorization/google?platform=" + platform;
//
//        if (deviceId != null) {
//            redirectUrl += "&device_id=" + deviceId;
//        }
//        if (deviceName != null) {
//            redirectUrl += "&device_name=" + deviceName;
//        }
//
//        log.info("Redirecting to: {}", redirectUrl);
//
//        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
//        exchange.getResponse().getHeaders().setLocation(java.net.URI.create(redirectUrl));
//
//        return exchange.getResponse().setComplete();
//    }
//
//    /**
//     * Endpoint để test OAuth2 user info (optional)
//     */
//    @GetMapping("/user")
//    public Mono<Map<String, Object>> getCurrentUser(Authentication authentication) {
//        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
//            return Mono.just(Map.of("error", "Not authenticated"));
//        }
//
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        return Mono.just(Map.of(
//                "name", oAuth2User.getAttribute("name"),
//                "email", oAuth2User.getAttribute("email"),
//                "picture", oAuth2User.getAttribute("picture")
//        ));
//    }
//}