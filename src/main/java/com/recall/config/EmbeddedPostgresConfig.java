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
///**
// * Configuration for embedded PostgreSQL database.
// * This is currently commented out and not active.
// */
//@Configuration
//@Conditional(OnMissingR2dbcUrlCondition.class)
//public class EmbeddedPostgresConfig {
//
//
//    /**
//     * Starts an embedded PostgreSQL instance (starts only once).
//     * @return The embedded PostgreSQL instance
//     */
//    private EmbeddedPostgres startEmbeddedPostgres() {
//        return EmbeddedPostgres.start();
//    }
//
//    /**
//     * Provides an R2DBC ConnectionFactory for the embedded database.
//     * @return The R2DBC ConnectionFactory
//     */
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        EmbeddedPostgres pg = startEmbeddedPostgres();
//        DataSource dataSource = pg.getPostgresDatabase(); // JDBC DataSource
//        // Spring Boot automatically bridges DataSource → R2DBC (via r2dbc-pool + proxy)
//        String jdbcUrl = pg.getJdbcUrl("postgres");
//        String r2dbcUrl = jdbcUrl.replaceFirst("^jdbc:", "r2dbc:");
//        return ConnectionFactoryBuilder.withUrl(r2dbcUrl)
//                .build();
//    }
//
//    /**
//     * (Optional) Provides a DataSource if JDBC is also used (e.g., for Flyway).
//     * @return The JDBC DataSource
//     */
//    @Bean
//    public DataSource dataSource() {
//        EmbeddedPostgres pg = startEmbeddedPostgres();
//        return pg.getPostgresDatabase();
//    }
//}
//
