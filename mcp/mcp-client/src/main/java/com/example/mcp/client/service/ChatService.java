package com.example.mcp.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;  // 恢复原有方式
    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String message) {
        log.info("========================================");
        log.info("💬 用户输入: {}", message);
        
        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        log.info("🔧 可用 MCP 工具数量: {}", toolCallbacks.length);
        for (ToolCallback callback : toolCallbacks) {
            log.info("   - 工具名称: {}", callback.getToolDefinition().name());
            log.info("     工具描述: {}", callback.getToolDefinition().description());
        }
        
        // 简单路由到服务端工具（兜底），确保功能可用
        try {
            if (message.contains("计算")) {
                String expr = message.replaceAll(".*计算", "").trim();
                if (expr.isEmpty()) expr = "123 + 456";
                URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/mcp/exec/calculate")
                        .queryParam("expression", expr)
                        .build(true)
                        .toUri();
                ResponseEntity<String> res = restTemplate.getForEntity(uri, String.class);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    return res.getBody();
                }
            }
            if (message.contains("天气")) {
                String city = message.replaceAll(".*(北京|上海|广州|深圳|杭州).*", "$1");
                if (city == null || city.isEmpty()) city = "北京";
                URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/mcp/exec/weather")
                        .queryParam("city", city)
                        .build(true)
                        .toUri();
                ResponseEntity<String> res = restTemplate.getForEntity(uri, String.class);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    return res.getBody();
                }
            }
            if (message.contains("用户") && message.matches(".*\\d{4}.*")) {
                String userId = message.replaceAll(".*?(\\d{4}).*", "$1");
                URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/mcp/exec/database")
                        .queryParam("userId", userId)
                        .build(true)
                        .toUri();
                ResponseEntity<String> res = restTemplate.getForEntity(uri, String.class);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    return res.getBody();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ 兜底路由到服务端工具失败: {}", e.getMessage());
        }
        
        log.info("🚀 开始调用 AI...");
        long startTime = System.currentTimeMillis();
        
        String response = chatClient.prompt()
                .user(message)
                .toolCallbacks(toolCallbackProvider)  // 使用原有的ToolCallbackProvider
                .call()
                .content();
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("⏱️ 响应耗时: {} ms", elapsed);
        log.info("🤖 AI 响应: {}", response);
        log.info("========================================");
        
        return response;
    }

}
