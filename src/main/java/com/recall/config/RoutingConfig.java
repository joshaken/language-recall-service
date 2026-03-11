package com.recall.config;

import com.recall.controller.ProxyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Configuration class for routing HTTP requests to handlers.
 */
@Configuration
public class RoutingConfig {

    /**
     * Defines the routing rules for chat requests.
     * @param proxyHandler The proxy handler instance
     * @return The router function
     */
    @Bean
    public RouterFunction<ServerResponse> fun(ProxyHandler proxyHandler) {
        return RouterFunctions.route(
                POST("/api/chat")
                        .and(accept(MediaType.TEXT_EVENT_STREAM))
                , proxyHandler::chat);

    }
}
