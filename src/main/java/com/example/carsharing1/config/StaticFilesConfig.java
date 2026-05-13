package com.example.carsharing1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticFilesConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        // classpath first: Docker/Railway JAR has no ./frontend/dist next to the process
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "file:frontend/dist/");
    }
}
