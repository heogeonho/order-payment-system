package com.example.api_server.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 설정
 * ObjectMapper bean을 제공하여 JSON 직렬화/역직렬화를 처리
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Java 8 날짜/시간 타입 지원
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
