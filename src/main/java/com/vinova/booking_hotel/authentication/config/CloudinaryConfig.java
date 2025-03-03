package com.vinova.booking_hotel.authentication.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary getCloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dzihbbinr",
                "api_key", "424214716917593",
                "api_secret", "TaJ7wj3NFhFB_RV8CkBsXERYJiM",
                "secure", true
        ));
    }
}