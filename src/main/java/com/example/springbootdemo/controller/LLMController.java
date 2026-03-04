package com.example.springbootdemo.controller;

import com.example.springbootdemo.service.SpringAiAlibabaService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class LLMController {

    private final SpringAiAlibabaService llmService;

    public LLMController(SpringAiAlibabaService llmService) {
        this.llmService = llmService;
    }

    @GetMapping(value = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String prompt) {
        return llmService.streamChat(prompt);
    }

}
