package com.recall.config;

import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Component to log database information on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInfoLogger {

    private final ConnectionFactory connectionFactory;

    /**
     * Logs the database product name and version after the bean is initialized.
     */
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

