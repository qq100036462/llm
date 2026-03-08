package com.example.mcp.client.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolsController {

    private final ToolCallbackProvider toolCallbackProvider;

    @GetMapping
    public ToolsResponse listTools() {
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        List<String> names = Arrays.stream(callbacks)
                .map(c -> c.getToolDefinition().name())
                .collect(Collectors.toList());
        ToolsResponse resp = new ToolsResponse();
        resp.setCount(names.size());
        resp.setNames(names);
        return resp;
    }

    @Data
    public static class ToolsResponse {
        private int count;
        private List<String> names;
    }
}

