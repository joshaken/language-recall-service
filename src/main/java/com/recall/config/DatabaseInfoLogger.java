package com.recall.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionMetadata;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInfoLogger {

    private final ConnectionFactory connectionFactory;

    @PostConstruct
    public void logDatabaseInfo() {
        Mono.usingWhen(
                        connectionFactory.create(),
                        conn -> Mono.just(conn.getMetadata()),
                        conn -> Mono.from(conn.close())
                )
                .doOnNext(metadata -> {
                    log.info("DB: {} {}", metadata.getDatabaseProductName(), metadata.getDatabaseVersion());
                })
                .doOnError(e ->
                        log.error("Failed to detect database type", e)
                )
                .subscribe();
    }
}

