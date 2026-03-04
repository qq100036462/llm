# Spring AI Prompt 详解

## 1. Prompt 类概述

`Prompt` 是 Spring AI 中用于封装用户请求的核心类，它是 `ModelRequest<List<Message>>` 的实现类。在与 Chat Model（大语言模型）进行交互时，所有的请求都通过 `Prompt` 对象来传递。

```java
public class Prompt implements ModelRequest<List<Message>> {
    // 对话历史 + 当前对话内容
    private final List<Message> messages;
    
    // 调用 Chat Model 时的额外参数
    @Nullable
    private ChatOptions chatOptions;
}
```

## 2. 核心参数详解

### 2.1 List<Message> messages

**作用**: 这是传递给大语言模型的主要内容，包含了完整的对话上下文。

**组成结构**:
- `messages` 是一个 `List<Message>` 类型的列表
- 每个 `Message` 对象代表对话中的一条消息
- 消息按照时间顺序排列，形成完整的对话历史

**Message 的类型**:
1. **System Message** (`Message.Role.SYSTEM`)
   - 用于定义模型的行为准则和角色设定
   - 通常放在列表的第一个位置
   - 例如: "你是一个专业的 Java 开发工程师"

2. **User Message** (`Message.Role.USER`)
   - 用户的输入内容
   - 可以包含多个用户消息，形成多轮对话

3. **Assistant Message** (`Message.Role.ASSISTANT`)
   - 模型的回复内容
   - 用于构建对话历史，实现上下文记忆

**示例代码**:
```java
List<Message> messages = Arrays.asList(
    new SystemMessage("你是一个乐于助人的 AI 助手"),
    new UserMessage("什么是 Spring Boot?"),
    new AssistantMessage("Spring Boot 是一个用于创建独立的、生产级别的 Spring 应用程序的框架..."),
    new UserMessage("它有哪些主要特点?")
);

Prompt prompt = new Prompt(messages);
```

### 2.2 ChatOptions chatOptions

**作用**: 这是调用 Chat Model 时的可选配置参数，用于控制模型的生成行为。

**可配置的参数**:

#### 2.2.1 Model
- **类型**: `String`
- **作用**: 指定使用的模型名称
- **示例**: `"deepseek-chat"`, `"gpt-3.5-turbo"`, `"gpt-4"`

#### 2.2.2 Temperature
- **类型**: `Double`
- **取值范围**: 0.0 到 2.0
- **作用**: 控制生成文本的随机性
  - **低温度** (0.0-0.5): 输出更加确定性、保守
  - **高温度** (1.5-2.0): 输出更加随机、创造性
- **默认值**: 通常为 0.7

#### 2.2.3 Max Tokens
- **类型**: `Integer`
- **作用**: 限制模型生成的最大 token 数量
- **注意**: 不同模型有不同的 token 限制

#### 2.2.4 Top P
- **类型**: `Double`
- **取值范围**: 0.0 到 1.0
- **作用**: 核采样方法，控制生成文本的多样性
- **与 Temperature 的区别**: Top P 是基于概率的采样，而 Temperature 是基于温度的缩放

#### 2.2.5 Frequency Penalty
- **类型**: `Double`
- **取值范围**: -2.0 到 2.0
- **作用**: 降低模型重复使用相同词汇的倾向
- **正值**: 减少重复
- **负值**: 增加重复

#### 2.2.6 Presence Penalty
- **类型**: `Double`
- **取值范围**: -2.0 到 2.0
- **作用**: 降低模型谈论与输入内容相同主题的倾向
- **正值**: 鼓励新话题
- **负值**: 允许重复话题

**示例代码**:
```java
Map<String, Object> options = Map.of(
    "model", "deepseek-chat",
    "temperature", 0.7,
    "max_tokens", 1000,
    "top_p", 0.9
);

Prompt prompt = new Prompt("你好", options);
```

## 3. Prompt 的使用方式

### 3.1 构造函数

Spring AI 提供了多种构造 `Prompt` 的方式:

```java
// 方式1: 只传入文本内容
Prompt prompt1 = new Prompt("什么是 Java?");

// 方式2: 传入文本内容和 ChatOptions
Map<String, Object> options = Map.of("temperature", 0.8);
Prompt prompt2 = new Prompt("什么是 Java?", options);

// 方式3: 传入 List<Message>
List<Message> messages = Arrays.asList(
    new UserMessage("什么是 Java?")
);
Prompt prompt3 = new Prompt(messages);

// 方式4: 传入 List<Message> 和 ChatOptions
Prompt prompt4 = new Prompt(messages, options);
```

### 3.2 在 ChatClient 中使用

```java
@Autowired
private ChatClient chatClient;

// 使用 call() 方法 - 同步获取完整响应
String response = chatClient.prompt()
    .user("什么是 Spring Boot?")
    .call()
    .content();

// 使用 stream() 方法 - 流式获取响应
Flux<ChatResponse> responseStream = chatClient.prompt()
    .user("什么是 Spring Boot?")
    .stream();
```

### 3.3 在 ChatModel 中直接使用

```java
@Autowired
private ChatModel chatModel;

// 同步调用
ChatResponse response = chatModel.call(
    new Prompt("什么是 Java?", Map.of("temperature", 0.7))
);

// 流式调用
Flux<ChatResponse> responseStream = chatModel.stream(
    new Prompt("什么是 Java?", Map.of("temperature", 0.7))
);
```

## 4. 实际应用示例

### 4.1 多轮对话场景

```java
public class MultiTurnChatExample {
    
    private final ChatClient chatClient;
    private final List<Message> conversationHistory = new ArrayList<>();
    
    public MultiTurnChatExample(ChatClient chatClient) {
        this.chatClient = chatClient;
        // 初始化系统提示
        conversationHistory.add(new SystemMessage(
            "你是一个专业的 Java 开发工程师，擅长解答各种 Java 相关问题"
        ));
    }
    
    public String chat(String userMessage) {
        // 添加用户消息到历史
        conversationHistory.add(new UserMessage(userMessage));
        
        // 创建 Prompt
        Prompt prompt = new Prompt(conversationHistory);
        
        // 调用模型
        ChatResponse response = chatClient.prompt(prompt)
            .call()
            .chatResponse();
        
        // 将模型回复添加到历史
        conversationHistory.add(response.getResult().getOutput());
        
        return response.getResult().getOutput().getContent();
    }
}
```

### 4.2 带参数的 Prompt

```java
public class AdvancedPromptExample {
    
    private final ChatClient chatClient;
    
    public AdvancedPromptExample(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    public String generateCode(String requirement, String language) {
        String promptText = String.format(
            "请用 %s 编写一个 %s 的示例代码，包含完整的注释", 
            language, requirement
        );
        
        Map<String, Object> options = Map.of(
            "model", "deepseek-chat",
            "temperature", 0.5,  // 代码生成使用较低温度
            "max_tokens", 2000
        );
        
        Prompt prompt = new Prompt(promptText, options);
        
        return chatClient.prompt(prompt)
            .call()
            .content();
    }
}
```

### 4.3 流式处理 Prompt

```java
public Flux<String> streamChat(String userMessage) {
    return chatClient.prompt()
        .user(userMessage)
        .stream()
        .content();
}

// 在 Controller 中使用
@GetMapping("/stream")
public Flux<String> stream(@RequestParam String prompt) {
    return service.streamChat(prompt);
}
```

## 5. 最佳实践

### 5.1 提示词设计原则

1. **清晰明确**: 提示词应该清晰、具体，避免模糊的表述
2. **提供上下文**: 给模型足够的背景信息，帮助它理解需求
3. **分步引导**: 对于复杂任务，可以分步骤引导模型
4. **角色设定**: 使用 System Message 定义模型的角色和行为

### 5.2 参数调优建议

1. **代码生成**: 使用较低的 temperature (0.1-0.5)
2. **创意写作**: 使用较高的 temperature (0.8-1.2)
3. **对话交互**: 使用中等 temperature (0.7-1.0)
4. **摘要生成**: 使用较低的 max_tokens

### 5.3 错误处理

```java
try {
    Prompt prompt = new Prompt(userMessage, options);
    ChatResponse response = chatModel.call(prompt);
    return response.getResult().getOutput().getContent();
} catch (Exception e) {
    log.error("调用大模型失败", e);
    return "抱歉，我无法处理您的请求: " + e.getMessage();
}
```

## 6. 总结

`Prompt` 是 Spring AI 中与大语言模型交互的核心概念，理解它的两个核心参数非常重要:

1. **messages**: 构建完整的对话上下文，是模型理解用户需求的基础
2. **chatOptions**: 控制模型的生成行为，影响输出的质量和风格

通过合理设计 `Prompt` 和配置 `ChatOptions`，可以充分发挥大语言模型的能力，实现各种应用场景。
