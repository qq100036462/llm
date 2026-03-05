package com.example.service;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

/**
 * 定义一个声明式的 AI 服务接口。
 * LangChain4j 会自动为这个接口创建一个代理实现，将方法调用转换为对大模型的调用。
 */
@AiService
public interface Assistant {

    /**
     * 定义一个流式聊天方法。
     * @param message 用户的提问
     * @return TokenStream，一个可以逐个 token 读取的流
     */
    TokenStream chat(String message);
}
