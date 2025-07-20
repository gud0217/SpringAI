package com.example.springai.domain.openai.controller;

import com.example.springai.domain.openai.dto.CityResponseDTO;
import com.example.springai.domain.openai.service.ChatService;
import com.example.springai.domain.openai.service.OpenAIService;
import com.example.springai.domain.openai.entity.ChatEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private final OpenAIService openAIService;
    private final ChatService chatService;

    public ChatController(OpenAIService openAIService, ChatService chatService) {
        this.openAIService = openAIService;
        this.chatService = chatService;
    }

    @GetMapping
    public String chatPage() {
        return "chat";
    }

    @ResponseBody
    @PostMapping("/chat")
    public CityResponseDTO chat(@RequestBody Map<String, String> body) {
        return openAIService.generate(body.get("text"));
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody Map<String, String> body) {
        return openAIService.generateStream(body.get("text"));
    }

    @ResponseBody
    @PostMapping("/chat/history/{userId}")
    public List<ChatEntity> getChatHistory(@PathVariable("userId") String userId) {
        return chatService.readAllChats(userId);
    }
}
