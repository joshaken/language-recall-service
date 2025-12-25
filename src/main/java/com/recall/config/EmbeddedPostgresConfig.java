//package com.recall.config;
//
//import com.opentable.db.postgres.embedded.EmbeddedPostgres;
//import com.recall.utils.OnMissingR2dbcUrlCondition;
//import io.r2dbc.spi.ConnectionFactory;
//import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
//import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Conditional;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicReference;
//
//@Configuration
//@Conditional(OnMissingR2dbcUrlCondition.class)
//public class EmbeddedPostgresConfig {
//
//
//    /**
//     * 启动内嵌 PostgreSQL（只启动一次）
//     */
//    private EmbeddedPostgres startEmbeddedPostgres() {
//        return EmbeddedPostgres.start();
//    }
//
//    /**
//     * 提供 R2DBC ConnectionFactory
//     */
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        EmbeddedPostgres pg = startEmbeddedPostgres();
//        DataSource dataSource = pg.getPostgresDatabase(); // JDBC DataSource
//        // Spring Boot 自动桥接 DataSource → R2DBC (via r2dbc-pool + proxy)
//        String jdbcUrl = pg.getJdbcUrl("postgres");
//        String r2dbcUrl = jdbcUrl.replaceFirst("^jdbc:", "r2dbc:");
//        return ConnectionFactoryBuilder.withUrl(r2dbcUrl)
//                .build();
//    }
//
//    /**
//     * （可选）如果你也用 JDBC（比如 Flyway），可以提供 DataSource
//     */
//    @Bean
//    public DataSource dataSource() {
//        EmbeddedPostgres pg = startEmbeddedPostgres();
//        return pg.getPostgresDatabase();
//    }
//}
//
