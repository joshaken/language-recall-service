package com.recall.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Configuration class for CORS (Cross-Origin Resource Sharing).
 */
@Configuration
public class CorsGlobalConfig {

    /**
     * Creates a CorsWebFilter bean to handle CORS requests.
     * @return The configured CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from localhost:3000
        config.addAllowedOriginPattern("http://localhost:3000");

        // Allow all headers and methods
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
