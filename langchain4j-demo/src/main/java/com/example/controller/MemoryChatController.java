package com.example.controller;

import com.example.config.RedisChatMemoryStore;
import com.example.service.ChatMemoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
public class MemoryChatController {

    private final ChatMemoryService chatMemoryService;
    private final RedisChatMemoryStore chatMemoryStore;

    public MemoryChatController(ChatMemoryService chatMemoryService, RedisChatMemoryStore chatMemoryStore) {
        this.chatMemoryService = chatMemoryService;
        this.chatMemoryStore = chatMemoryStore;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public SseEmitter chatWithoutMemory(@RequestBody String prompt) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        Flux<String> flux = chatMemoryService.chat("default", prompt);

        flux.subscribe(
            token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            error -> emitter.completeWithError(error),
            () -> {
                System.out.println("Streaming completed (no memory)");
                emitter.complete();
            }
        );

        return emitter;
    }

    @PostMapping(value = "/memory/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public SseEmitter chatWithMemory(
            @PathVariable String sessionId,
            @RequestBody String prompt) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        Flux<String> flux = chatMemoryService.chat(sessionId, prompt);

        flux.subscribe(
            token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            error -> emitter.completeWithError(error),
            () -> {
                System.out.println("Streaming completed for session: " + sessionId);
                emitter.complete();
            }
        );

        return emitter;
    }

    @DeleteMapping("/memory/{sessionId}")
    public String clearMemory(@PathVariable String sessionId) {
        chatMemoryStore.deleteMessages(sessionId);
        return "Memory cleared for session: " + sessionId;
    }

    @GetMapping("/memory/{sessionId}")
    public String getMemoryInfo(@PathVariable String sessionId) {
        var messages = chatMemoryStore.getMessages(sessionId);
        return "Session " + sessionId + " has " + messages.size() + " messages in memory.";
    }
}
