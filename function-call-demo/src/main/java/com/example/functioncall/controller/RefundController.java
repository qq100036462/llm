package com.example.functioncall.controller;

import com.example.functioncall.service.RefundService;
import com.example.functioncall.service.RefundService.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/refund")
public class RefundController {

    private static final Logger log = LoggerFactory.getLogger(RefundController.class);

    private final RefundService refundService;
    
    // 简单的会话存储（实际项目中应使用Redis等）
    private final Map<String, List<ChatMessage>> sessionStore = new HashMap<>();

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = request.getSessionId();
        String userMessage = request.getMessage();
        
        log.info("[退款客服] 收到请求 - SessionId: {}, 消息: {}", sessionId, userMessage);
        
        // 获取或创建会话历史
        List<ChatMessage> history = sessionStore.getOrDefault(sessionId, new ArrayList<>());
        
        // 调用AI服务
        String aiResponse = refundService.chat(userMessage, history);
        
        // 保存对话历史
        history.add(new ChatMessage("user", userMessage));
        history.add(new ChatMessage("assistant", aiResponse));
        sessionStore.put(sessionId, history);
        
        log.info("[退款客服] 响应完成 - SessionId: {}, 历史轮数: {}", sessionId, history.size() / 2);
        
        return new ChatResponse(aiResponse, sessionId);
    }

    @PostMapping("/reset")
    public String resetSession(@RequestParam String sessionId) {
        sessionStore.remove(sessionId);
        log.info("[退款客服] 会话已重置 - SessionId: {}", sessionId);
        return "会话已重置";
    }

    // 请求DTO
    public static class ChatRequest {
        private String sessionId;
        private String message;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // 响应DTO
    public static class ChatResponse {
        private String response;
        private String sessionId;

        public ChatResponse(String response, String sessionId) {
            this.response = response;
            this.sessionId = sessionId;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
