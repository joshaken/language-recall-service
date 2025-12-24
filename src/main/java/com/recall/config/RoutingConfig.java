package com.recall.config;

import com.recall.controller.ProxyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RoutingConfig {

    @Bean
    public RouterFunction<ServerResponse> fun(ProxyHandler proxyHandler) {
        return RouterFunctions.route(POST("/api/chat").and(accept(MediaType.TEXT_EVENT_STREAM)), proxyHandler::chat);

    }
}
