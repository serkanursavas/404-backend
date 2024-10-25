package com.squad.squad.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        // Support both "application/json" and "application/json;charset=UTF-8"
        jsonConverter.setSupportedMediaTypes(
                Collections.singletonList(new MediaType("application", "json", StandardCharsets.UTF_8)));

        converters.add(jsonConverter);
    }
}