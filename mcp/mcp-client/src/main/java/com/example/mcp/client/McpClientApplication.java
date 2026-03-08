package com.example.mcp.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP 客户端启动类
 * 
 * 客户端通过 MCP 协议连接服务端，获取可用工具列表
 * 并将这些工具提供给 AI 模型使用
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
        System.out.println("========================================");
        System.out.println("🚀 MCP Client 启动成功！");
        System.out.println("🔗 连接服务端: http://localhost:8080/mcp");
        System.out.println("🌐 Web界面: http://localhost:8081");
        System.out.println("========================================");
    }

}
