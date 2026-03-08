package com.example.mcp.server.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 数据库查询工具
 * 
 * 标准MCP工具实现，支持自动注册
 */
@Slf4j
@Service
public class DatabaseTool {

    // 模拟数据库
    private final Map<String, User> userDatabase = Map.of(
            "1001", new User("1001", "张三", "zhangsan@example.com", "VIP", 15000.00),
            "1002", new User("1002", "李四", "lisi@example.com", "普通", 5000.00),
            "1003", new User("1003", "王五", "wangwu@example.com", "VIP", 28000.00),
            "1004", new User("1004", "赵六", "zhaoliu@example.com", "普通", 800.00)
    );

    /**
     * 查询用户信息
     */
    @Tool(description = "查询用户信息数据库，支持按用户ID查询。可以获取用户姓名、邮箱、会员等级、账户余额等信息。")
    public String queryUser(
            @ToolParam(description = "用户ID，例如：1001、1002", required = true) String userId) {
        
        log.info("========================================");
        log.info("🗄️ [MCP工具调用] database - 数据库查询");
        log.info("   📥 入参 userId: {}", userId);
        long startTime = System.currentTimeMillis();

        User user = userDatabase.get(userId);
        
        String result;
        if (user != null) {
            result = String.format(
                    "用户信息 [ID: %s]:\n- 姓名: %s\n- 邮箱: %s\n- 会员等级: %s\n- 账户余额: ¥%.2f",
                    user.id, user.name, user.email, user.level, user.balance
            );
            log.info("   📤 返回结果: 查询成功 - {}", user.name);
        } else {
            result = "未找到用户ID: " + userId;
            log.warn("   📤 返回结果: 用户不存在");
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("   ⏱️ 执行耗时: {} ms", elapsed);
        log.info("========================================");
        return result;
    }

    // 用户数据类
    private record User(String id, String name, String email, String level, double balance) {}

}
