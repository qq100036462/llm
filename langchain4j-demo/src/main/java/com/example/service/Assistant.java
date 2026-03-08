package com.example.service;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.MemoryId;
import reactor.core.publisher.Flux;

/**
 * 定义一个声明式的 AI 服务接口。
 * LangChain4j 会自动为这个接口创建一个代理实现，将方法调用转换为对大模型的调用。
 */
@AiService(streamingChatModel = "openAiStreamingChatModel")
public interface Assistant {

    /**
     * 定义一个流式聊天方法（无记忆）
     * @param message 用户的提问
     * @return Flux<String>，一个可以逐个 token 读取的流
     */
    Flux<String> chat(String message);

    /**
     * 定义一个带记忆的流式聊天方法
     * @param memoryId 记忆ID，用于区分不同的会话
     * @param message 用户的提问
     * @return Flux<String>，一个可以逐个 token 读取的流
     */
    Flux<String> chatWithMemory(@MemoryId String memoryId, String message);
}
