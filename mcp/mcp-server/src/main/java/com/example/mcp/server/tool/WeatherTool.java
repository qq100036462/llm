package com.example.mcp.server.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 天气查询工具
 * 
 * 标准MCP工具实现，支持自动注册
 * Spring AI 会自动扫描并注册
 */
@Slf4j
@Service
public class WeatherTool {

    /**
     * 查询城市天气
     * 
     * @Tool: 声明为 MCP 工具
     * @ToolParam: 参数描述，用于生成 JSON Schema
     */
    @Tool(description = "查询指定城市的当前天气信息，包括温度、湿度、天气状况等")
    public String queryWeather(
            @ToolParam(description = "城市名称，例如：北京、上海、广州", required = true) String city) {
        
        log.info("========================================");
        log.info("🌤️ [MCP工具调用] weather - 天气查询");
        log.info("   📥 入参 city: {}", city);
        long startTime = System.currentTimeMillis();

        Map<String, String> weatherData = Map.of(
                "北京", "晴天，温度 25°C，湿度 45%，空气质量优",
                "上海", "多云，温度 28°C，湿度 65%，空气质量良",
                "广州", "小雨，温度 30°C，湿度 80%，空气质量优",
                "深圳", "阴天，温度 29°C，湿度 75%，空气质量良",
                "杭州", "晴天，温度 26°C，湿度 55%，空气质量优"
        );

        String result = weatherData.getOrDefault(city, 
                "未找到该城市的天气信息，默认：晴天，温度 22°C，湿度 50%");
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("   📤 返回结果: {}", result);
        log.info("   ⏱️ 执行耗时: {} ms", elapsed);
        log.info("========================================");
        return result;
    }

}
