package com.example.mcp.client.controller;

import com.example.mcp.client.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 * 
 * 提供 REST API 供前端调用
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class McpChatController {

    private final ChatService chatService;

    /**
     * 聊天接口
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("📨 收到聊天请求: {}", request.getMessage());
        
        String response = chatService.chat(request.getMessage());
        
        return new ChatResponse(response);
    }

    /**
     * 测试接口 - GET 方式
     */
    @GetMapping
    public ChatResponse chatGet(@RequestParam String message) {
        log.info("📨 收到聊天请求 (GET): {}", message);
        
        String response = chatService.chat(message);
        
        return new ChatResponse(response);
    }

    // DTO
    @Data
    public static class ChatRequest {
        private String message;
    }

    @Data
    @RequiredArgsConstructor
    public static class ChatResponse {
        private final String response;
    }

}
