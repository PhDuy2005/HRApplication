package com.se347.nhom4.HRApplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                // Root path
                "/",

                // Authentication endpoints - không cần kiểm tra permission
                "/api/v1/auth/**",

                // Actuator endpoints - health check, monitoring
                "/actuator/**",
                "/api/v1/actuator/**",

                // API Documentation - Swagger
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",

                // Static resources
                "/storage/**"
        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}