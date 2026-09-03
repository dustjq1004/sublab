package me.kimyeonsup.home.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.kimyeonsup.home.config.jackson.NullToEmptySerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식 사용
        // null 값을 빈 문자열로 변환
        objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory().withSerializerModifier(new NullToEmptySerializerModifier())
        );
        return objectMapper;
    }

}
