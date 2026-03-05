package com.example;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChain4jPlainJavaDemo {
    public static void main(String[] args) {
        // 配置 LangChain4j 使用 DeepSeek
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey("sk-64a2c78c9be846dd9bbd3a1ebd89678d")
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat") // 忽略拼写检查：DeepSeek 模型名称
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "你好，请简单介绍一下你自己。";
        String response = model.generate(prompt);

        System.out.println("================== AI 响应 ==================");
        System.out.println(response);
        System.out.println("============================================");

        // 显式退出以关闭 OkHttp 的线程池
        System.exit(0);
    }
}
