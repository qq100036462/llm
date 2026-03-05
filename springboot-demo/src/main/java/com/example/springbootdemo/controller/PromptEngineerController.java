package com.example.springbootdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai/prompt")
public class PromptEngineerController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    /**
     * 1. 预设角色：毒舌博主
     * 访问地址：/ai/prompt/chat?message=你的问题
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("你是一个毒舌博主，说话很噎人，请根据用户问题，怼他")
                .user(message)
                .stream()
                .content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 2. 提示工程技巧：Few-Shot (少样本) 推理
     * 通过示例引导模型进行数字转换。
     */
    @GetMapping(value = "/chat2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat2(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("""
                        你是一个专门处理数字转换的 AI 助手。
                        请根据用户输入的数字，直接给出结果，不需要任何思考过程和多余的废话。
                        推理逻辑参考示例：
                        1 = 5
                        2 = 10
                        3 = 15
                        如果用户输入不是数字，请回复：无法回答，请输入数字。
                        """)
                .user(message)
                .stream()
                .content();
    }

    /**
     * 3. 提示工程技巧：输出结构化 (Output Structuring) 与 Few-Shot
     * 修复方案：将 .call() 改为 .stream() 异步流式调用，解决 WebFlux 环境下的阻塞报错。
     */
    @GetMapping(value = "/shot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> shot(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("""
                        你是一个文本优化助手，请根据用户输入的问题进行改写。
                        改写策略：
                        1. 改写其中的错别字。
                        2. 做内容精简，将冗余内容精简成一句话。
                        请严格按照 JSON 格式输出，包含以下键："错别字改写", "内容精简"。
                        
                        参考示例：
                        Input：ni好
                        Output ：{"错别字改写":"你好","内容精简":""}

                        Input：我今天心情不错，我想知道今天是什么天气才让我心情这么好的？
                        Output ：{"错别字改写":"","内容精简":"今天是什么天气？"}
                        """)
                .user(message)
                .stream()
                .content();
    }

    /**
     * 4. 提示工程技巧：角色扮演 (Role Playing) 与输出结构化
     */
    @GetMapping(value = "/promptsEngineer3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat3(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("""
                        你是一个富有创意的作家。
                        请生成包括书名、作者和类别的三本虚构的、非真实存在的中文书籍清单。
                        请以 JSON 数组格式提供，每个对象包含以下键：book_id, title, author, genre。
                        """)
                .user(message)
                .stream()
                .content();
    }

    /**
     * 5. 提示工程技巧：复杂任务分解 (Task Decomposition)
     */
    @GetMapping(value = "/chat4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat4(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("""
                        你是一个高效的文本处理助手，请依次执行以下操作：
                        1. 用一句话概括用户提供的文本。
                        2. 将该摘要翻译成英语。
                        3. 在英语摘要中列出出现的所有人名。
                        4. 输出一个 JSON 对象，包含以下键：english_summary, num_names。
                        
                        请用换行符分隔上述步骤的答案。
                        """)
                .user(message)
                .stream()
                .content();
    }

    /**
     * 6. 提示工程技巧：思维链 (Chain of Thought, CoT)
     */
    @GetMapping(value = "/chat5", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat5(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("""
                        你是一个擅长数学推理的 AI 助手。
                        对于用户提出的问题，请一步一步展示你的思考过程，最后给出明确的最终答案。
                        """)
                .user(message.isEmpty() ? "一个水果摊有5箱苹果，每箱重15公斤。今天卖掉了35公斤，还剩下多少公斤苹果？" : message)
                .stream()
                .content();
    }

    /**
     * 7. 提示工程技巧：PromptTemplate (提示词模板)
     * <p>
     * 详解：
     * 1. 模板定义：通过 {placeholder} 语法定义占位符，例如 {topic} 和 {tone}。
     * 2. 动态填充：PromptTemplate.create(Map) 会将参数 Map 中的 key 替换掉模板中的占位符。
     * 3. 复用性：这允许我们将固定的指令逻辑（Template）与动态的数据内容（User Message）分离。
     * 4. 场景：适用于生成博客、摘要、问答等需要固定结构但主题多变的场景。
     *
     * @param message 用户的输入，将作为模板中的 {topic}
     * @return 渲染后的流式回复
     */
    @GetMapping(value = "/template", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithTemplate(@RequestParam(value = "message") String message) {
        // 1. 定义提示词模板。{topic} 和 {tone} 是占位符。
        String templateString = """
                你是一个专业的文案策划。
                请围绕主题：{topic}
                以 {tone} 的风格，写一段吸引人的营销文案。
                """;

        // 2. 创建 PromptTemplate 对象。
        PromptTemplate promptTemplate = new PromptTemplate(templateString);

        // 3. 准备参数 Map，动态填充模板中的占位符。
        Map<String, Object> renderParams = Map.of(
                "topic", message,       // 用户的输入作为主题
                "tone", "幽默且充满活力"  // 固定的风格设定
        );

        // 4. 通过模板创建最终的 Prompt 对象。
        Prompt finalPrompt = promptTemplate.create(renderParams);

        // 5. 将生成的 Prompt 传递给 ChatClient 执行流式调用。
        return chatClient.prompt(finalPrompt)
                .stream()
                .content();
    }
}
