package com.example.functioncall.function;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    @Tool(description = "获取指定城市的天气信息")
    public WeatherResponse getWeather(@ToolParam(description = "城市名称") String city) {
        if (city == null || city.isEmpty()) {
            return new WeatherResponse("未知", "未知", 0, "城市名称不能为空");
        }
        return switch (city.toLowerCase()) {
            case "北京", "beijing" -> new WeatherResponse("北京", "晴天", 25, "今天北京天气晴朗，适合户外活动");
            case "上海", "shanghai" -> new WeatherResponse("上海", "多云", 28, "上海今天多云，气温适中");
            case "广州", "guangzhou" -> new WeatherResponse("广州", "小雨", 30, "广州今天有小雨，记得带伞");
            case "深圳", "shenzhen" -> new WeatherResponse("深圳", "晴天", 32, "深圳天气炎热，注意防暑");
            default -> new WeatherResponse(city, "未知", 20, "暂无该城市的天气信息");
        };
    }

    public record WeatherResponse(String city, String weather, int temperature, String description) {}
}
