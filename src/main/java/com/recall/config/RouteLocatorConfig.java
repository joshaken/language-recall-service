package com.recall.config;

public class RouteLocatorConfig {
    //    @Bean
//    public RouteLocator customRoutes(RouteLocatorBuilder builder){
//        return builder.routes()
//                // 请求网关路径包含 /api/ec/** 的都会被路由到eureka-client
//                .route("eureka-client",r->r.path("/api/ec/**")
//                        .filters(f->f.stripPrefix(2))
//                        .uri("lb://eureka-client"))
//                // 可以配置多个route
//                .route("eureka-client2",r->r.path("/api/ec2/**")
//                        .filters(f->f.stripPrefix(2))
//                        .uri("lb://eureka-client"))
//                .build();
//    }
}
