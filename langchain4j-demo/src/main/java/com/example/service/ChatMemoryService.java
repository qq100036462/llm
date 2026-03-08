package com.example.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 带记忆的 AI 服务接口
 */
@AiService(streamingChatModel = "openAiStreamingChatModel")
public interface ChatMemoryService {

    /**
     * 带记忆的流式聊天方法
     * @param memoryId 记忆ID，用于区分不同的会话
     * @param message 用户的提问
     * @return Flux<String>，一个可以逐个 token 读取的流
     */
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
