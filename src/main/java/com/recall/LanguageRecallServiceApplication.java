package com.recall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class LanguageRecallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LanguageRecallServiceApplication.class, args);
    }




}
