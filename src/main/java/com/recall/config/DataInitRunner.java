package com.recall.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.r2dbc.core.DatabaseClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitRunner implements ApplicationRunner {

    private final DatabaseClient databaseClient;
    private final ConnectionFactory connectionFactory;

    @Override
    public void run(ApplicationArguments args) {

        databaseClient.sql("SELECT COUNT(id) FROM sentences")
                .map(row -> row.get(0, Long.class))
                .one()
                .defaultIfEmpty(0L)
                .filter(count -> count == 0)
                .flatMap(count -> {
                    log.info("sentences 表为空，开始初始化数据");
                    ResourceDatabasePopulator populator =
                            new ResourceDatabasePopulator(
                                    new ClassPathResource("data-init.sql")
                            );
                    return populator.populate(connectionFactory);
                })
                .doOnSuccess(v -> log.info("数据初始化完成"))
                .subscribe();
    }
}
