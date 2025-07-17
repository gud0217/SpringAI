package com.example.springai.controller;

import com.example.springai.domain.openai.service.OpenAIService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Controller
public class ChatController {

    private final OpenAIService openAIService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping
    public String chatPage(){
        return "chat";
    }

    @ResponseBody
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) {
        return openAIService.generate(body.get("text"));
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody Map<String, String> body) {
        return openAIService.generateStream(body.get("text"));
    }
}
