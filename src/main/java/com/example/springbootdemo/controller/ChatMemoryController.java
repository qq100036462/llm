package com.example.springbootdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示让大模型拥有“记忆”的自动化方式
 * 
 * 1. 内存记忆：引入 spring-ai-autoconfigure-model-chat-memory 后，默认提供 InMemoryChatMemory。
 * 2. Redis 持久化：引入 spring-ai-alibaba-starter-memory-redis 后，ChatMemory 会被自动替换为 Redis 存储。
 *    即使应用重启，只要 chatId 一致，对话历史依然会从 Redis 中找回。
 */
@RestController
@RequestMapping("/ai/memory")
public class ChatMemoryController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;

    /**
     * 自动注入 Spring AI 配置好的 ChatMemory
     */
    @Autowired
    private ChatMemory chatMemory;

    private ChatClient chatClient;

    /**
     * 手动维护的历史记录列表（用于方法 1）
     */
    private final List<Message> chatHistory = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 构建 ChatClient
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 方法 1: 手动维护历史记录 (Manual History)
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithMemory(@RequestParam String message) {
        chatHistory.add(new UserMessage(message));
        return chatClient.prompt()
                .messages(chatHistory)
                .stream()
                .content();
    }

    /**
     * 方法 2: 使用 Advisor 自动化记忆 (MessageChatMemoryAdvisor)
     * <p>
     * 引入 autoconfigure 包后，我们可以直接利用 Advisor 来简化多轮对话。
     * Advisor 会自动完成：
     * 1. 从 chatMemory 读取指定 chatId 的历史。
     * 2. 将历史注入当前 Prompt。
     * 3. 对话结束后将 AI 的回复自动保存回 chatMemory。
     */
    @GetMapping(value = "/advisor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> advisorMemory(@RequestParam String message, @RequestParam(defaultValue = "default_user") String chatId) {
        return chatClient.prompt()
                // 使用自动注入的 chatMemory
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // 必须指定会话 ID，key 是固定的 ChatMemory.CONVERSATION_ID
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .user(message)
                .stream()
                .content();
    }

    /**
     * 清空记忆
     */
    @GetMapping("/clear")
    public String clearMemory(@RequestParam(defaultValue = "default_user") String chatId) {
        chatMemory.clear(chatId);
        chatHistory.clear();
        return "记忆已清空（包括手动列表和自动存储）";
    }
}
