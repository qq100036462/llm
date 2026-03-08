package com.example.mcp.server.config;

import com.example.mcp.server.tool.CalculatorTool;
import com.example.mcp.server.tool.DatabaseTool;
import com.example.mcp.server.tool.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class ToolConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(CalculatorTool calculatorTool, DatabaseTool databaseTool, WeatherTool weatherTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorTool, databaseTool, weatherTool)
                .build();
    }

}
