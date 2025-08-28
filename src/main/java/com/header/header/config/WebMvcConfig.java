package com.header.header.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>>  converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);converter.setSupportedMediaTypes(List.of(
                new MediaType("application", "json", StandardCharsets.UTF_8)
        ));
        converters.add(converter);
    }
}
