package com.ratelimiter.ratelimiter.config;

import com.ratelimiter.ratelimiter.filter.TokenBucketRateLimiterFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//defined how spring cloud gateway routes the incoming requests.
// client -> backend services

@Configuration
@AllArgsConstructor
public class GatewayConfig {

    private final RateLimiterProperties rateLimiterProperties;
    private final TokenBucketRateLimiterFilter tokenBucketRateLimiterFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){

        return builder.routes()
                .route("api-route", r->r
                        .path("/api/**")
                        .filters(f->f
                                .stripPrefix(1)
                                .filter(tokenBucketRateLimiterFilter.apply(tokenBucketRateLimiterFilter.newConfig()))
                        )
                        .uri(rateLimiterProperties.getApiServerUrl()))
                .build();
    }
}

// Request arrives > stripPrefix(1) > remove /api/prefix > TokenBucketRateLimiterFilter > api-server-url