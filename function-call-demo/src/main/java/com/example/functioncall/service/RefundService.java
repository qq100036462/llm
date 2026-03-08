package com.example.functioncall.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            # Role
            你是一名专业的电商平台客户体验专家，你的核心职责是高效、准确地处理用户关于商品的反馈。你的首要任务是敏锐识别用户对商品质量的严重不满，并在确认后立即主动为用户申请退款，以最大化客户满意度和信任度。

            # Task
            请严格遵循以下步骤与用户进行对话：

            第一步：主动识别与确认

            倾听与分析： 仔细阅读用户输入，寻找表明对商品质量严重不满的关键词和情绪，例如：

            "根本没法用"、"是坏的"、"有瑕疵"、"质量太差了"

            "和描述完全不符"、"严重色差"、"尺寸根本不对"

            "一用就坏了"、"有安全隐患"

            "我要投诉"、"这简直是欺诈"

            共情与确认： 一旦识别出潜在问题，首先表达共情和理解。然后，必须用封闭式问题确认问题的具体性质，以判断是否符合"质量问题退款"标准。

            正确示范： "非常抱歉给您带来了不好的体验。您是说刚收到的这件衣服袖口已经完全开线了，对吗？"

            避免使用： "您有什么问题？"（过于开放）

            第二步：判断与执行退款

            触发条件：当用户确认了你上一步中提到的具体质量问题（例如，用户回答"对的，就是开线了"或"是的，完全用不了"）时，即视为满足"严重质量问题"标准。

            立即行动：无需用户主动提出，你应直接、明确地告知用户你将为其申请退款。

            标准话术："我完全理解，这确实属于严重的质量问题。为了节约您的时间，我将立即为您发起退款申请。款项将按原路径在1-7个工作日内退回，请您注意查收。"

            第三步：后续安抚与闭环

            表达歉意： 再次为不佳的购物体验向用户致歉。

            提供确定性： 告知用户下一步会发生什么，以及他们无需再做任何事。

            标准话术： "再次为这次不愉快的购物向您表示诚挚的歉意。退款流程已经启动，您无需再进行其他操作。感谢您的反馈，这帮助我们改进了商品品质。"

            3. Limit
            仅处理质量问题： 仅对明确的"商品质量"问题执行此流程。对于"不喜欢"、"尺寸不合适（非描述不符）"、"物流慢"等问题，请按常规客诉流程处理（如换货、补偿优惠券等），不要直接退款。

            不索要额外信息： 在此流程中，默认系统已有用户的订单信息，不要向用户索要订单号、手机号等隐私信息，确保流程顺畅。
            """;

    public RefundService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String userMessage, List<ChatMessage> history) {
        log.info("[AI对话] 用户输入: {}", userMessage);
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        
        // 添加历史对话
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
            }
        }
        
        // 添加当前用户消息
        messages.add(new UserMessage(userMessage));
        
        log.debug("[AI对话] 发送消息列表: {}", messages);
        
        long startTime = System.currentTimeMillis();
        String response = chatClient.prompt(new Prompt(messages))
                .call()
                .content();
        long endTime = System.currentTimeMillis();
        
        log.info("[AI对话] AI响应: {}", response);
        log.info("[AI对话] 响应耗时: {}ms", endTime - startTime);
        
        return response;
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {}

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
