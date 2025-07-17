package com.example.springai.domain.openai.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;

    public OpenAIService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel, OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel, OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {
        this.openAiChatModel = openAiChatModel;
    }

    // chatmodel : response
    public String generate(String text) {

        // 메시지
        SystemMessage systemMessage = new SystemMessage(""); // 시스템 동작 방식을 제어하는 지침. 예) 당신은 심리 상담사 입니다.
        UserMessage userMessage = new UserMessage(text); // 사용자가 입력한 질문이나 요청. 예) 오늘따라 일이 잘 안풀려..
        AssistantMessage assistantMessage = new AssistantMessage(""); // 이전 어시스턴트 응답 내용. 예) 오늘은 잠을 몇시간 주무셨나요?


        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // 요청 및 응답
        ChatResponse response = openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    // chatmodel : response stream
    public Flux<String> generateStream(String text) {

        // 메시지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(text);
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // 요청 및 응답
        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText());
    }
}
