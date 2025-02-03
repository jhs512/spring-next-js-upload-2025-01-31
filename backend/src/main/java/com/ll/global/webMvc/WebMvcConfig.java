package com.ll.global.webMvc;

import com.ll.global.app.AppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/gen/**")
                .addResourceLocations("file:///" + AppConfig.getGenFileDirPath() + "/");
    }
}