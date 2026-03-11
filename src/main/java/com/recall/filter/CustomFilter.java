package com.recall.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom global filter for handling OPTIONS requests (CORS preflight).
 */
@Component
public class CustomFilter implements GlobalFilter {

    /**
     * Filters the incoming request.
     * Handles OPTIONS requests by returning an OK status immediately.
     * @param exchange The current server web exchange
     * @param chain The filter chain
     * @return A Mono Void
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
//        response.getHeaders().remove("Access-Control-Allow-Origin");

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
