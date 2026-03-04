package com.example.springbootdemo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring AI 服务类
 * 演示各种提示词相关的用法
 */
@Service
public class SpringAiAlibabaService implements InitializingBean {

    private ChatClient chatClient;

    private final ChatModel chatModel;

    public SpringAiAlibabaService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    // ================================================================================
    // 1. 基础用法 - 简单对话
    // ================================================================================

    /**
     * 最简单的对话方式
     * 只传入用户消息，使用默认配置
     */
    public Flux<String> simpleChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)  // 设置用户消息
                .stream()           // 使用流式响应
                .content();         // 只获取内容部分
    }

    // ================================================================================
    // 2. System Prompt - 预设角色/系统提示
    // ================================================================================

    /**
     * 设置系统提示（System Prompt）
     * 用于定义模型的角色、行为准则、知识范围等
     * 系统提示在对话中具有最高优先级，影响模型的整体行为
     */
    public Flux<String> chatWithSystemPrompt(String userMessage) {
        return chatClient.prompt()
                .system("你是一个毒舌博主，说话很噎人，请根据用户问题，怼他")  // 系统提示
                .user(userMessage)  // 用户消息
                .stream()
                .content();
    }

    /**
     * 多个系统提示的示例
     * 可以多次调用 system() 方法，它们会按顺序合并
     */
    public Flux<String> chatWithMultipleSystemPrompts(String userMessage) {
        return chatClient.prompt()
                .system("你是一个专业的 Java 开发工程师")  // 第一个系统提示：角色设定
                .system("你的回答应该简洁明了，包含代码示例")  // 第二个系统提示：回答风格
                .user(userMessage)
                .stream()
                .content();
    }

    // ================================================================================
    // 3. User Prompt - 用户消息
    // ================================================================================

    /**
     * 用户消息（User Prompt）
     * 用户的输入内容，可以是问题、指令或对话内容
     */
    public Flux<String> chatWithUserPrompt(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)  // 用户消息
                .stream()
                .content();
    }

    /**
     * 多个用户消息的示例（多轮对话）
     * 可以多次调用 user() 方法，构建完整的对话历史
     */
    public Flux<String> chatWithMultipleUserMessages(String message1, String message2) {
        return chatClient.prompt()
                .user(message1)  // 第一条用户消息
                .user(message2)  // 第二条用户消息
                .stream()
                .content();
    }

    // ================================================================================
    // 4. Assistant Prompt - 助手消息
    // ================================================================================

    /**
     * 助手消息（Assistant Prompt）
     * 用于构建对话历史，让模型记住之前的回复
     * 在多轮对话中非常有用
     */
    public Flux<String> chatWithAssistantMessage(String userMessage, String assistantMessage) {
        List<Message> messages = List.of(
                new UserMessage(userMessage),
                new AssistantMessage(assistantMessage),
                new UserMessage("那这个怎么实现呢？")
        );
        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    // ================================================================================
    // 5. Tool Prompt - 工具调用
    // ================================================================================

    /**
     * 工具消息（Tool Prompt）
     * 用于工具调用场景，让模型知道可以使用哪些工具
     */
    public Flux<String> chatWithTools(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    // ================================================================================
    // 6. Chat Options - 模型参数配置
    // ================================================================================

    /**
     * ChatOptions - 模型参数配置
     * 用于控制模型的生成行为
     */
    public Flux<String> chatWithOptions(String userMessage) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .topP(0.9)
                .frequencyPenalty(0.5)
                .presencePenalty(0.5)
                .build();
        return chatClient.prompt()
                .user(userMessage)
                .options(options)
                .stream()
                .content();
    }

    /**
     * 使用 ChatOptions 对象配置
     * 更类型安全的方式
     */
    public Flux<String> chatWithTypedOptions(String userMessage) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.8)
                .maxTokens(500)
                .build();
        return chatClient.prompt()
                .user(userMessage)
                .options(options)
                .stream()
                .content();
    }

    // ================================================================================
    // 7. Prompt Template - 模板提示词
    // ================================================================================

    /**
     * PromptTemplate - 提示词模板
     * 使用模板引擎动态生成提示词，支持变量替换
     */
    public Flux<String> chatWithTemplate(String userName, String topic) {
        String template = """
                你是一个乐于助人的助手。
                用户姓名：{name}
                请围绕 "{topic}" 这个话题与用户 {name} 进行对话。
                回答要友好且专业。
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "name", userName,
                "topic", topic
        ));

        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    /**
     * 使用 PromptTemplate 的另一种方式
     */
    public Flux<String> chatWithTemplate2(String question, String context) {
        String template = """
                背景知识：{context}
                
                问题：{question}
                
                请基于背景知识回答问题，如果背景知识中没有相关信息，请说明。
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", question
        ));
        List<Message> messages = List.of(
                new SystemMessage("你是一个知识渊博的助手，请根据提供的背景知识回答问题"),
                new UserMessage(String.format("""
                        背景知识：%s
                        
                        问题：%s
                        
                        请基于背景知识回答问题，如果背景知识中没有相关信息，请说明。
                        """, context, question))
        );
        Prompt finalPrompt = new Prompt(messages);
        return chatClient.prompt(finalPrompt)
                .stream()
                .content();
    }

    // ================================================================================
    // 8. 完整的多参数组合示例
    // ================================================================================

    /**
     * 完整示例：组合使用所有提示词参数
     * 这是一个复杂的对话场景，包含系统提示、用户提示、助手提示和模型参数
     */
    public Flux<String> complexChatScenario(String userMessage) {
        List<Message> messages = List.of(
                new SystemMessage("""
                        你是一个专业的 Java 技术专家，同时也是一位幽默的博主。
                        你的写作风格：
                        1. 专业但不枯燥，善于用比喻解释复杂概念
                        2. 适当使用网络流行语，但要适度
                        3. 每篇文章至少包含一个代码示例
                        4. 结尾要有总结和思考
                        """),
                new AssistantMessage("""
                        好的，我明白了。我会以专业且幽默的方式讲解 Java 技术。
                        请开始提问吧！
                        """),
                new UserMessage(userMessage)
        );
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.9)
                .maxTokens(1500)
                .topP(0.95)
                .build();
        Prompt prompt = new Prompt(messages, options);
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    // ================================================================================
    // 9. 调用方法对比
    // ================================================================================

    /**
     * call() 方法 - 同步调用
     * 等待模型完整响应后返回
     */
    public String syncChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()    // 同步调用，返回完整响应
                .content();
    }

    /**
     * stream() 方法 - 流式调用
     * 实时流式返回模型的响应，适合大模型生成长文本
     */
    public Flux<String> streamChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()  // 流式调用，实时返回
                .content();
    }

    // ================================================================================
    // 10. 其他常用方法
    // ================================================================================

    /**
     * 获取完整的 ChatResponse（不仅仅是内容）
     * 包含元数据、使用统计等信息
     */
    public String chatWithFullResponse(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 使用 Prompt 对象直接创建
     * 适用于复杂的提示词场景
     */
    public Flux<String> chatWithPromptObject(String userMessage) {
        Prompt prompt = new Prompt(userMessage);
        
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    /**
     * 使用 Prompt 对象并传入选项
     */
    public Flux<String> chatWithPromptAndOptions(String userMessage) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.7)
                .maxTokens(1000)
                .build();
        Prompt prompt = new Prompt(userMessage, options);
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    // ================================================================================
    // 11. 实际应用场景示例
    // ================================================================================

    /**
     * 场景1：编程助手
     * 专注于代码生成和解释
     */
    public Flux<String> codingAssistant(String programmingLanguage, String requirement) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.5)
                .build();
        return chatClient.prompt()
                .system("""
                        你是一个专业的编程助手，擅长 {language} 语言。
                        你的职责：
                        1. 提供高质量的代码示例
                        2. 解释代码的原理和最佳实践
                        3. 指出潜在的问题和优化建议
                        4. 使用中文回答
                        """)
                .user(String.format(
                        "请用 %s 实现一个 %s 的功能，包含完整的注释和错误处理。",
                        programmingLanguage, requirement
                ))
                .options(options)
                .stream()
                .content();
    }

    /**
     * 场景2：写作助手
     * 用于文章、博客等创意写作
     */
    public Flux<String> writingAssistant(String topic, String tone, int length) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.8)
                .build();
        return chatClient.prompt()
                .system("""
                        你是一个专业的写作助手。
                        请根据用户需求创作内容。
                        """)
                .user(String.format("""
                        请以 "%s" 为话题，采用 "%s" 的风格，写一篇约 %d 字的文章。
                        要求：逻辑清晰、语言流畅、有吸引力。
                        """, topic, tone, length))
                .options(options)
                .stream()
                .content();
    }

    /**
     * 场景3：对话机器人（多轮对话）
     * 维护对话历史，实现上下文理解
     */
    public Flux<String> conversationBot(List<String> conversationHistory, String newUserMessage) {
        List<Message> messages = List.of(
                new SystemMessage("你是一个友好的对话机器人，能够理解上下文进行对话"),
                new UserMessage(conversationHistory.get(0)),
                new AssistantMessage("我是你的对话助手，有什么可以帮你的？"),
                new UserMessage(newUserMessage)
        );
        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    /**
     * 场景4：问答系统（RAG 场景）
     * 结合检索到的上下文进行问答
     */
    public Flux<String> qaSystem(String question, String context) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.3)
                .build();
        return chatClient.prompt()
                .system("""
                        你是一个知识问答助手。
                        请根据提供的背景知识回答问题。
                        如果背景知识中没有相关信息，请如实告知用户。
                        回答要简洁明了，不超过 200 字。
                        """)
                .user(String.format("""
                        背景知识：
                        %s
                        
                        问题：
                        %s
                        """, context, question))
                .options(options)
                .stream()
                .content();
    }

}
