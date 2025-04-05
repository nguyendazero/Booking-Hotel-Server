package com.vinova.booking_hotel.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Các origin bạn cần cho phép, thêm nếu cần
                .allowedOrigins("http://localhost:5173", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Tất cả các phương thức bạn muốn cho phép
                .allowedHeaders("*") // Header cho phép
                .allowCredentials(true); // Cho phép cookie nếu cần
    }
}