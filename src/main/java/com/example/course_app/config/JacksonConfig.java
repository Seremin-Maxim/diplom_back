package com.example.course_app.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.hibernate.proxy.HibernateProxy;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Конфигурация Jackson для корректной сериализации/десериализации объектов
 */
@Configuration
public class JacksonConfig {

    /**
     * Создает и настраивает ObjectMapper для использования в приложении
     * 
     * @param builder Jackson2ObjectMapperBuilder
     * @return настроенный ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Настройка сериализации
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Регистрация модуля для работы с Java 8 date/time типами
        objectMapper.registerModule(new JavaTimeModule());
        
        // Настройка видимости полей
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        // Добавляем поддержку Hibernate-прокси
        SimpleModule hibernateModule = new SimpleModule();
        hibernateModule.addSerializer(HibernateProxy.class, new ToStringSerializer());
        objectMapper.registerModule(hibernateModule);
        
        return objectMapper;
    }
    
    /**
     * Создает и настраивает MappingJackson2HttpMessageConverter для использования в приложении
     * 
     * @param objectMapper настроенный ObjectMapper
     * @return настроенный MappingJackson2HttpMessageConverter
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
