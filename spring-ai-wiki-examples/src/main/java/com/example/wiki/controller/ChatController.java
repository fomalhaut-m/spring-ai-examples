package com.example.wiki.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.example.wiki.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 普通对话
     */
    @PostMapping
    public ApiResponse<String> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.error("消息内容不能为空");
        }
        try {
            var advisor = MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(request.getSessionId())
                    .build();

            String reply = chatClient.prompt()
                    .user(request.getMessage())
                    // 提示词 返回格式必须是 markdown
                    .system("请以 markdown 格式返回")
                    .advisors(advisor)
                    .call()
                    .content();
            return ApiResponse.success(reply);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 流式对话
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        var advisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(request.getSessionId())
                .build();

        return chatClient.prompt()
                .user(request.getMessage())
                .system("请以 markdown 格式返回")
                .advisors(advisor)
                .stream()
                .content();
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public ApiResponse<List<org.springframework.ai.chat.messages.Message>> getHistory(
            @RequestBody HistoryRequest request) {
        try {
            var messages = chatMemory.get(request.getSessionId());
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Data
    public static class ChatRequest {
        private String message;

        @JsonProperty("sessionId")
        @Getter
        @Setter
        private String sessionId = "default";
    }

    @Data
    public static class HistoryRequest {
        @JsonProperty("sessionId")
        @Getter
        @Setter
        private String sessionId = "default";
    }
}