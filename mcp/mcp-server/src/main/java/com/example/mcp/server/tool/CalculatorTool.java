package com.example.mcp.server.tool;

import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CalculatorTool {

    @Tool(description = "执行数学表达式计算，支持加减乘除、括号、幂运算等。例如：123 + 456, (100 - 50) * 2, 2^10")
    public String calculate(
            @ToolParam(description = "数学表达式，例如：123 + 456, (100 - 50) * 2", required = true) String expression) {
        
        log.info("========================================");
        log.info("🧮 [MCP工具调用] calculator - 计算器");
        log.info("   📥 入参 expression: {}", expression);
        long startTime = System.currentTimeMillis();

        try {
            String processedExpression = expression
                    .replace("^", "^")
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("x", "*")
                    .replace("X", "*");
            
            Expression e = new ExpressionBuilder(processedExpression).build();
            double result = e.evaluate();
            
            String output;
            if (result == (long) result) {
                output = String.format("%s = %d", expression, (long) result);
            } else {
                output = String.format("%s = %.4f", expression, result);
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("   📤 返回结果: {}", output);
            log.info("   ⏱️ 执行耗时: {} ms", elapsed);
            log.info("========================================");
            return output;
            
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("   ❌ 计算错误: {}", ex.getMessage(), ex);
            log.info("   ⏱️ 执行耗时: {} ms", elapsed);
            log.info("========================================");
            return "计算错误: " + ex.getMessage();
        }
    }

}
