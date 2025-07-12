package com.example.paymentservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    @Qualifier("externalRestTemplate")
    public RestTemplate externalRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    @Qualifier("internalRestTemplate")
    public RestTemplate internalRestTemplate() {
        return new RestTemplate();
    }
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
