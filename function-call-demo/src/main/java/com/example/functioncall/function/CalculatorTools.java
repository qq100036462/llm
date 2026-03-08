package com.example.functioncall.function;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(description = "执行基本的数学计算，支持加、减、乘、除运算")
    public CalculatorResponse calculate(
            @ToolParam(description = "第一个数字") double a,
            @ToolParam(description = "第二个数字") double b,
            @ToolParam(description = "操作类型：add(加)、subtract(减)、multiply(乘)、divide(除)") String operation) {
        
        String op = operation != null ? operation.toLowerCase() : "add";
        
        double result = switch (op) {
            case "add", "加", "+" -> a + b;
            case "subtract", "减", "-" -> a - b;
            case "multiply", "乘", "*" -> a * b;
            case "divide", "除", "/" -> b != 0 ? a / b : Double.NaN;
            default -> throw new IllegalArgumentException("不支持的操作: " + operation);
        };
        
        String opSymbol = switch (op) {
            case "add", "加", "+" -> "+";
            case "subtract", "减", "-" -> "-";
            case "multiply", "乘", "*" -> "×";
            case "divide", "除", "/" -> "÷";
            default -> operation;
        };
        
        return new CalculatorResponse(result, a + " " + opSymbol + " " + b + " = " + result);
    }

    public record CalculatorResponse(double result, String expression) {}
}
