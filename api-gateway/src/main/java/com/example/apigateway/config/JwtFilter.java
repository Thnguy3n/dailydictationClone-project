package com.example.apigateway.config;

import com.example.apigateway.service.AuthService;
import com.example.apigateway.service.JwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {
//
//    private final JwtService jwtService;
//    private final AuthService authService;
//
//    public JwtFilter(JwtService jwtService, @Lazy AuthService authService) {
//        this.jwtService = jwtService;
//        this.authService = authService;
//    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                String username = jwtService.extractUserName(jwtToken);

                if (username != null && !username.isEmpty()) {
                    return authService.getUserDetails(username)
                            .map(ResponseEntity::getBody)
                            .filter(userDetails -> userDetails != null && jwtService.validateToken(jwtToken, userDetails))
                            .doOnNext(userDetails -> {
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                exchange.getAttributes().put("authentication", authenticationToken);
                            })
                            .then(chain.filter(exchange))
                            .onErrorResume(ex -> {
                                return chain.filter(exchange);
                            });
                }
            } catch (Exception e) {
                return chain.filter(exchange);
            }
        }

        return chain.filter(exchange);
    }
}