# MCP (Model Context Protocol) 技术详解与实战

## 目录
1. [MCP 简介](#1-mcp-简介)
2. [核心概念](#2-核心概念)
3. [架构设计](#3-架构设计)
4. [项目结构](#4-项目结构)
5. [服务端实现详解](#5-服务端实现详解)
6. [客户端实现详解](#6-客户端实现详解)
7. [工作流程](#7-工作流程)
8. [启动与测试](#8-启动与测试)
9. [MCP vs Function Calling](#9-mcp-vs-function-calling)
10. [总结与展望](#10-总结与展望)

---

## 1. MCP 简介

### 1.1 什么是 MCP？

**MCP (Model Context Protocol)** 是由 **Anthropic** 于 2024 年推出的开放协议，旨在标准化 AI 模型与外部工具、数据源之间的交互方式。

> 官方定义：MCP 是一个开放协议，用于标准化应用程序向 LLM 提供上下文的方式。可以将 MCP 想象成 AI 应用程序的 USB-C 接口——就像 USB-C 标准化了设备与各种外设的连接方式，MCP 标准化了 AI 模型与不同数据源和工具的连接方式。

### 1.2 为什么需要 MCP？

在 MCP 出现之前，AI 应用集成外部工具面临以下问题：

| 问题 | 说明 |
|------|------|
| **碎片化** | 每个 AI 平台有自己的工具调用方式（OpenAI Functions、Google Tools、LangChain Tools 等） |
| **重复开发** | 同样的工具需要为不同平台重复实现 |
| **生态封闭** | 工具与特定平台绑定，难以跨平台复用 |
| **维护困难** | N 个工具 × M 个平台 = N×M 个适配器 |

**MCP 的解决方案**：
- 统一的协议标准
- 一次实现，到处运行
- 工具提供者只需实现 MCP Server
- AI 应用只需实现 MCP Client

### 1.3 MCP 生态

```
┌─────────────────────────────────────────────────────────────┐
│                      MCP 生态系统                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐      MCP Protocol      ┌──────────────┐ │
│   │  MCP Client  │  ◄──────────────────►  │  MCP Server  │ │
│   │   (AI应用)    │    (标准化通信协议)     │   (工具服务)  │ │
│   └──────────────┘                        └──────────────┘ │
│          │                                        │         │
│          │ 发现工具列表                              │ 注册工具 │
│          │ 调用工具                                 │ 执行逻辑 │
│          │                                        │         │
│   ┌──────▼──────┐                        ┌──────▼──────┐   │
│   │   Claude    │                        │  天气查询    │   │
│   │   Cursor    │                        │  数据库查询  │   │
│   │   Claude Desktop │                   │  文件系统    │   │
│   │   自定义AI应用  │                      │  Git操作    │   │
│   └─────────────┘                        └─────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 核心概念

### 2.1 三大核心原语

MCP 定义了三种核心能力：

#### 1. Tools（工具）
- **作用**：让 AI 执行操作（查询、计算、修改）
- **场景**：天气查询、数据库操作、API 调用
- **特点**：可读写，有副作用

#### 2. Resources（资源）
- **作用**：为 AI 提供只读数据
- **场景**：文件内容、数据库记录、API 响应
- **特点**：只读，无副作用

#### 3. Prompts（提示模板）
- **作用**：预定义的提示模板
- **场景**：标准化任务流程
- **特点**：可复用，可参数化

### 2.2 关键术语

| 术语 | 英文 | 说明 |
|------|------|------|
| MCP Server | MCP 服务端 | 提供工具/资源的服务 |
| MCP Client | MCP 客户端 | 连接 Server 使用工具 |
| Transport | 传输层 | 通信方式（stdio/sse） |
| Capability | 能力 | Server 提供的功能声明 |
| Tool Call | 工具调用 | AI 请求执行工具 |

---

## 3. 架构设计

### 3.1 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                     MCP 系统架构                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                    MCP Client 层                       │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │ │
│  │  │  ChatClient │  │  McpClient  │  │  Tool Call  │   │ │
│  │  │  (Spring AI)│  │  (MCP SDK)  │  │   Handler   │   │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │ │
│  └───────────────────────────────────────────────────────┘ │
│                           │                                 │
│                           │ HTTP/SSE                        │
│                           ▼                                 │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                    MCP Server 层                       │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │ │
│  │  │   McpServer │  │   Tools     │  │  Resources  │   │ │
│  │  │   (核心)     │  │  (工具注册)  │  │  (资源暴露)  │   │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │ │
│  │  │ WeatherTool │  │CalculatorTool│  │ DatabaseTool│   │ │
│  │  │  (天气查询)  │  │  (计算器)    │  │ (数据库查询)│   │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 通信流程

```
用户提问
    │
    ▼
┌─────────────┐
│ MCP Client  │ ──1. 连接 Server──► ┌─────────────┐
│   (AI应用)   │                     │ MCP Server  │
└─────────────┘ ◄─2. 返回工具列表──  │   (工具)     │
    │                               └─────────────┘
    ▼
┌─────────────┐
│  AI 模型    │ ◄── 工具信息加入 Prompt
└─────────────┘
    │
    ▼
AI 决定调用工具
    │
    ▼
┌─────────────┐
│ MCP Client  │ ──3. 调用工具──► ┌─────────────┐
└─────────────┘                  │ MCP Server  │
    │ ◄────4. 返回结果────────  │ (执行逻辑)   │
    ▼                            └─────────────┘
┌─────────────┐
│  AI 模型    │ ◄── 工具结果加入上下文
└─────────────┘
    │
    ▼
生成最终回复
```

---

## 4. 项目结构

```
mcp/
├── pom.xml                          # 父 POM
├── mcp-server/                      # MCP 服务端模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/mcp/server/
│       │   ├── McpServerApplication.java      # 启动类
│       │   ├── config/
│       │   │   └── McpServerConfig.java       # 服务端配置
│       │   └── tool/
│       │       ├── WeatherTool.java           # 天气工具
│       │       ├── CalculatorTool.java        # 计算器工具
│       │       └── DatabaseTool.java          # 数据库工具
│       └── resources/
│           └── application.yml
│
├── mcp-client/                      # MCP 客户端模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/mcp/client/
│       │   ├── McpClientApplication.java      # 启动类
│       │   ├── config/
│       │   │   └── McpClientConfig.java       # 客户端配置
│       │   ├── service/
│       │   │   └── McpChatService.java        # 聊天服务
│       │   └── controller/
│       │       └── McpChatController.java     # REST API
│       └── resources/
│           └── application.yml
│
└── README.md                        # 本文档
```

---

## 5. 服务端实现详解

### 5.1 启动类

```java
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
```

### 5.2 服务端配置

```java
@Configuration
public class McpServerConfig {

    /**
     * 注册 MCP Servlet 传输层到 /sse 路径
     */
    @Bean
    public ServletRegistrationBean<McpServletServerTransport> mcpServletTransport(
            McpServletServerTransport transport) {
        return new ServletRegistrationBean<>(transport, "/sse");
    }

    /**
     * 创建 MCP 服务端，注册所有工具
     */
    @Bean
    public McpServer mcpServer(McpServletServerTransport transport,
                               WeatherTool weatherTool,
                               CalculatorTool calculatorTool,
                               DatabaseTool databaseTool) {
        
        // 创建服务端
        McpServer server = McpServer.sync(transport)
                .serverInfo("mcp-demo-server", "1.0.0")
                .build();

        // 注册工具
        server.addTool(weatherTool.getToolDefinition());
        server.addTool(calculatorTool.getToolDefinition());
        server.addTool(databaseTool.getToolDefinition());

        return server;
    }
}
```

### 5.3 工具实现

工具是 MCP 的核心，每个工具包含四个要素：

```java
@Component
public class WeatherTool {

    public McpServerFeatures.SyncToolRegistration getToolDefinition() {
        // 1. 工具名称
        String name = "weather";
        
        // 2. 工具描述（AI 通过描述理解何时使用该工具）
        String description = "查询指定城市的当前天气信息";

        // 3. 参数 Schema（JSON Schema 格式）
        String inputSchema = """
            {
                "type": "object",
                "properties": {
                    "city": {
                        "type": "string",
                        "description": "城市名称"
                    }
                },
                "required": ["city"]
            }
            """;

        // 4. 执行处理器
        McpSchema.Tool tool = new McpSchema.Tool(name, description, inputSchema);
        
        return new McpServerFeatures.SyncToolRegistration(
                tool,
                (exchange, arguments) -> {
                    // 获取参数
                    String city = (String) arguments.get("city");
                    
                    // 执行业务逻辑
                    String result = queryWeather(city);
                    
                    // 返回结果
                    return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent(result)),
                            false
                    );
                }
        );
    }
}
```

---

## 6. 客户端实现详解

### 6.1 客户端配置

```java
@Configuration
public class McpClientConfig {

    @Bean
    public McpClient mcpClient() {
        // 创建 HTTP 传输层，连接到服务端
        HttpClientMcpTransport transport = new HttpClientMcpTransport(
                "http://localhost:8080/sse"
        );

        // 创建 MCP 客户端
        return McpClient.sync(transport).build();
    }
}
```

### 6.2 聊天服务

```java
@Service
public class McpChatService {

    private final ChatClient chatClient;  // Spring AI
    private final McpClient mcpClient;    // MCP Client

    public String chat(String userMessage) {
        // 1. 从 MCP Server 获取工具列表
        List<McpSchema.Tool> mcpTools = mcpClient.listTools().tools();

        // 2. 构建包含工具信息的 Prompt
        String systemPrompt = buildSystemPrompt(mcpTools);

        // 3. 调用 AI
        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        // 4. 如果 AI 需要调用工具
        if (aiResponse.contains("<tool_call>")) {
            // 解析并执行工具调用
            String toolResult = executeToolCall(aiResponse);
            
            // 将结果反馈给 AI
            aiResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .user("工具结果：" + toolResult)
                    .call()
                    .content();
        }

        return aiResponse;
    }
}
```

---

## 7. 工作流程

### 7.1 完整交互流程

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────┐
│  用户   │────►│  MCP Client │────►│  AI 模型    │     │         │
└─────────┘     └─────────────┘     └─────────────┘     │         │
                     │                                   │         │
                     │ 1. 获取工具列表                      │         │
                     ▼                                   │         │
              ┌─────────────┐                            │         │
              │  MCP Server │                            │         │
              └─────────────┘                            │         │
                     │                                   │         │
                     │ 2. 返回工具定义                     │         │
                     ▼                                   │         │
              ┌─────────────┐     ┌─────────────┐        │         │
              │  MCP Client │────►│  AI 模型    │        │         │
              └─────────────┘     └─────────────┘        │         │
                                       │                 │         │
                                       │ 3. AI 决定调用工具        │
                                       ▼                 │         │
                                ┌─────────────┐          │         │
                                │  MCP Client │──────────┘         │
                                └─────────────┘     4. 调用工具     │
                                       │                           │
                                       ▼                           │
                                ┌─────────────┐                    │
                                │  MCP Server │◄───────────────────┘
                                └─────────────┘
                                       │
                                       │ 5. 执行工具逻辑
                                       ▼
                                ┌─────────────┐
                                │  MCP Client │────► 6. 返回结果给 AI
                                └─────────────┘
                                       │
                                       ▼
                                ┌─────────────┐
                                │  AI 生成最终回复
                                └─────────────┘
                                       │
                                       ▼
                                ┌─────────────┐
                                │  返回给用户  │
                                └─────────────┘
```

---

## 8. 启动与测试

### 8.1 启动步骤

```bash
# 1. 先启动 MCP Server（端口 8080）
cd mcp-server
# 在 IDEA 中右键 McpServerApplication.java → Run

# 2. 再启动 MCP Client（端口 8081）
cd mcp-client
# 在 IDEA 中右键 McpClientApplication.java → Run
```

### 8.2 测试接口

```bash
# 测试天气查询
curl "http://localhost:8081/api/mcp/chat?message=北京今天天气怎么样"

# 测试计算器
curl "http://localhost:8081/api/mcp/chat?message=帮我计算123乘以456"

# 测试数据库查询
curl "http://localhost:8081/api/mcp/chat?message=查询用户1001的信息"
```

---

## 9. MCP vs Function Calling

| 特性 | MCP | Function Calling |
|------|-----|------------------|
| **标准化** | ✅ 开放协议 | ❌ 各平台不同 |
| **跨平台** | ✅ 一次实现到处运行 | ❌ 需要适配各平台 |
| **生态** | 🌱 新兴生态 | 🌳 成熟生态 |
| **工具发现** | ✅ 动态发现 | ❌ 静态配置 |
| **复杂度** | 中等 | 简单 |
| **适用场景** | 多平台工具、开放生态 | 单一平台、快速开发 |

### 9.1 对比图示

```
Function Calling (传统方式):
┌─────────┐    ┌─────────────┐    ┌─────────────┐
│  OpenAI │    │   Claude    │    │   Google    │
│  Tools  │    │   Tools     │    │   Tools     │
└────┬────┘    └──────┬──────┘    └──────┬──────┘
     │                │                  │
     └────────────────┴──────────────────┘
                      │
              ┌───────▼───────┐
              │  天气查询工具   │  ← 需要为每个平台单独实现
              │  计算器工具    │
              └───────────────┘

MCP (标准化方式):
┌─────────┐    ┌─────────────┐    ┌─────────────┐
│  OpenAI │    │   Claude    │    │   Google    │
│  + MCP  │    │   + MCP     │    │   + MCP     │
└────┬────┘    └──────┬──────┘    └──────┬──────┘
     │                │                  │
     └────────────────┴──────────────────┘
                      │
              ┌───────▼───────┐
              │   MCP Server  │
              │  (天气/计算器) │  ← 一次实现，到处使用
              └───────────────┘
```

---

## 10. 总结与展望

### 10.1 MCP 的优势

1. **标准化**：统一的协议，降低集成成本
2. **可复用**：工具一次实现，多平台使用
3. **动态发现**：运行时获取工具列表，灵活扩展
4. **生态开放**：任何人都可以创建和共享工具

### 10.2 适用场景

- ✅ 需要跨平台复用工具的场景
- ✅ 构建工具生态平台
- ✅ 需要动态发现工具的场景
- ✅ 长期维护的大型项目

### 10.3 未来展望

MCP 作为新兴协议，正在快速发展：
- 更多平台支持（Claude Desktop、Cursor 已支持）
- 工具生态逐渐丰富
- Spring AI 等框架正在集成 MCP 支持

### 10.4 学习资源

- [MCP 官方文档](https://modelcontextprotocol.io/)
- [MCP SDK GitHub](https://github.com/modelcontextprotocol)
- [Spring AI MCP 集成](https://docs.spring.io/spring-ai/reference/)

---

## 附录：核心类图

```
┌─────────────────────────────────────────────────────────────┐
│                      MCP Server 核心类                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │  McpServer   │────────►│    Tool      │                 │
│  │  (服务端核心) │         │  (工具定义)   │                 │
│  └──────────────┘         └──────────────┘                 │
│         │                           │                       │
│         │ 管理                       │ 包含                   │
│         ▼                           ▼                       │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │   Transport  │         │  inputSchema │                 │
│  │   (传输层)    │         │  (参数Schema)│                 │
│  └──────────────┘         └──────────────┘                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      MCP Client 核心类                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │  McpClient   │────────►│  listTools() │                 │
│  │  (客户端核心) │         │  callTool()  │                 │
│  └──────────────┘         └──────────────┘                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: 1.0  
**最后更新**: 2025-03-07  
**作者**: AI Assistant
