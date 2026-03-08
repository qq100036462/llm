package com.example.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP 服务端启动类
 * 
 * MCP (Model Context Protocol) 是 Anthropic 推出的开放协议，
 * 用于标准化 AI 模型与外部工具/数据源的交互
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
        System.out.println("========================================");
        System.out.println("🚀 MCP Server 启动成功！");
        System.out.println("📡 STATELESS ASYNC 端点: http://localhost:8080/api/mcp");
        System.out.println("🔧 提供工具: 天气查询、计算器、数据库查询");
        System.out.println("🔄 协议模式: STATELESS ASYNC");
        System.out.println("========================================");
    }

}
