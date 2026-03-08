package com.example.config;

import com.example.service.ChatMemoryService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    private final StreamingChatModel streamingChatModel;
    private final RedisChatMemoryStore chatMemoryStore;

    public ChatMemoryConfig(StreamingChatModel streamingChatModel, RedisChatMemoryStore chatMemoryStore) {
        this.streamingChatModel = streamingChatModel;
        this.chatMemoryStore = chatMemoryStore;
    }

    @Bean
    public ChatMemoryService chatMemoryService() {
        return AiServices.builder(ChatMemoryService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();
    }
}
