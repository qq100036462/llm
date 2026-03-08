package com.example.config;

import com.alibaba.fastjson2.JSON;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisChatMemoryStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisChatMemoryStore chatMemoryStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        chatMemoryStore = new RedisChatMemoryStore(redisTemplate);
    }

    @Test
    void testGetMessages_WhenNoMessages_ReturnsEmptyList() {
        when(valueOperations.get(anyString())).thenReturn(null);

        List<ChatMessage> messages = chatMemoryStore.getMessages("test-session");

        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    void testGetMessages_WhenMessagesExist_ReturnsMessages() {
        List<ChatMessage> expectedMessages = List.of(
                new UserMessage("Hello"),
                new AiMessage("Hi there!")
        );
        String json = JSON.toJSONString(expectedMessages);
        when(valueOperations.get(anyString())).thenReturn(json);

        List<ChatMessage> messages = chatMemoryStore.getMessages("test-session");

        assertNotNull(messages);
        assertEquals(2, messages.size());
    }

    @Test
    void testUpdateMessages_SavesToRedis() {
        List<ChatMessage> messages = List.of(
                new UserMessage("Hello"),
                new AiMessage("Hi there!")
        );

        chatMemoryStore.updateMessages("test-session", messages);

        verify(valueOperations, times(1)).set(anyString(), anyString(), any());
    }

    @Test
    void testDeleteMessages_DeletesFromRedis() {
        chatMemoryStore.deleteMessages("test-session");

        verify(redisTemplate, times(1)).delete(anyString());
    }
}
