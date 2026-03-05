package com.example.controller;

import com.example.service.Assistant;
import dev.langchain4j.service.TokenStream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
public class ChatController {

    private final Assistant assistant;

    public ChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody String prompt) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            try {
                // 调用 @AIService 接口，代码更简洁
                TokenStream tokenStream = assistant.chat(prompt);

                // 将 TokenStream 的内容逐个推送到 SseEmitter
                tokenStream.onNext(token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }).onComplete(response -> {
                    System.out.println("Streaming completed: " + response);
                    emitter.complete();
                }).onError(emitter::completeWithError)
                  .start(); // 开始处理流

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
