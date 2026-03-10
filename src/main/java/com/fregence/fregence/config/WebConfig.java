package com.fregence.fregence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://localhost:8080/uploads/perfumes/sekil.jpg linkini aktiv edir
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}