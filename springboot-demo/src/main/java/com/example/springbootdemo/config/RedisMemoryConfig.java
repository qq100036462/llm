package com.example.springbootdemo.config;

import com.alibaba.cloud.ai.memory.redis.LettuceRedisChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisMemoryConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    public LettuceRedisChatMemoryRepository redisChatMemoryRepository() {
        return LettuceRedisChatMemoryRepository.builder()
                .host(host)
                .port(port)
                .password(password)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(LettuceRedisChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .build();
    }
}
