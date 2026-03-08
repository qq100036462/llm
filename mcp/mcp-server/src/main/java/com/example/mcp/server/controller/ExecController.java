package com.example.mcp.server.controller;

import com.example.mcp.server.tool.CalculatorTool;
import com.example.mcp.server.tool.DatabaseTool;
import com.example.mcp.server.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp/exec")
@RequiredArgsConstructor
public class ExecController {

    private final CalculatorTool calculatorTool;
    private final WeatherTool weatherTool;
    private final DatabaseTool databaseTool;

    @GetMapping("/calculate")
    public String calculate(@RequestParam String expression) {
        return calculatorTool.calculate(expression);
    }

    @GetMapping("/weather")
    public String weather(@RequestParam String city) {
        return weatherTool.queryWeather(city);
    }

    @GetMapping("/database")
    public String database(@RequestParam String userId) {
        return databaseTool.queryUser(userId);
    }
}

