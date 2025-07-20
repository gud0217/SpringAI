package com.example.springai.domain.openai.service;

import com.example.springai.domain.openai.dto.CityResponseDTO;
import com.example.springai.domain.openai.entity.ChatEntity;
import com.example.springai.domain.openai.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;

    public OpenAIService(OpenAiChatModel openAiChatModel,
                         ChatMemoryRepository chatMemoryRepository,
                         ChatRepository chatRepository) {
        this.openAiChatModel = openAiChatModel;
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatRepository = chatRepository;
    }

    // chatmodel : response
    public CityResponseDTO generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

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
        return chatClient.prompt(prompt)
                .call()
                .entity(CityResponseDTO.class);
    }





    // chatmodel : response stream
    public Flux<String> generateStream(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 유저&페이지별 ChatMemory를 관리하기 위한 Key (우선 명시적)
        String userId = "gud0217" + "_" + "1";

        // 전체 대화 저장용
        ChatEntity chatUserEntity = new ChatEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(text);

        // 메시지
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text)); // 신규 메시지 추가

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 응답 메시지를 저장할 임시 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        // 요청 및 응답
        return chatClient.prompt(prompt)
                .tools(new ChatTools())
                .stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    // chatMemory 저장
                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    // 전체 대화 저장용
                    ChatEntity chatAssistantEntity = new ChatEntity();
                    chatAssistantEntity.setUserId(userId);
                    chatAssistantEntity.setType(MessageType.ASSISTANT);
                    chatAssistantEntity.setContent(responseBuffer.toString());

                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
                });

//        return openAiChatModel.stream(prompt)
//                .mapNotNull(response -> {
//                    String token = response.getResult().getOutput().getText();
//                    responseBuffer.append(token);
//                    return token;
//                })
//                .doOnComplete(() -> {
//
//                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
//                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));
//
//                    // 전체 대화 저장용
//                    ChatEntity chatAssistantEntity = new ChatEntity();
//                    chatAssistantEntity.setUserId(userId);
//                    chatAssistantEntity.setType(MessageType.ASSISTANT);
//                    chatAssistantEntity.setContent(responseBuffer.toString());
//
//                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
//                });
    }
}
