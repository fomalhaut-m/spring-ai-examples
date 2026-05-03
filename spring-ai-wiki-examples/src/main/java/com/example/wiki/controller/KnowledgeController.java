package com.example.wiki.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import com.example.wiki.common.ApiResponse;
import com.example.wiki.model.KnowledgeEntry;
import com.example.wiki.service.WikiService;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final WikiService wikiService;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public KnowledgeController(WikiService wikiService, ChatClient chatClient, ChatMemory chatMemory) {
        this.wikiService = wikiService;
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 添加知识
     */
    @PostMapping
    public ApiResponse<KnowledgeEntry> addKnowledge(@RequestBody AddKnowledgeRequest request) {
        try {
            return ApiResponse.success(wikiService.addKnowledge(request.getTitle(), request.getContent(), request.getCategory()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 搜索知识（向量搜索）
     */
    @PostMapping("/search")
    public ApiResponse<List<KnowledgeEntry>> searchKnowledge(@RequestBody SearchRequest request) {
        try {
            return ApiResponse.success(wikiService.searchKnowledge(request.getQuery()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除知识
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteKnowledge(@PathVariable String id) {
        try {
            return ApiResponse.success(wikiService.deleteKnowledge(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 知识问答（RAG）
     */
    @PostMapping("/qa")
    public ApiResponse<Map<String, Object>> qa(@RequestBody QaRequest request) {
        try {
            var knowledge = wikiService.searchKnowledge(request.getQuestion());

            StringBuilder context = new StringBuilder();
            if (!knowledge.isEmpty()) {
                context.append("相关知识：\n");
                for (var entry : knowledge) {
                    context.append("- ").append(entry.title()).append("：").append(entry.content()).append("\n");
                }
            }

            String prompt = context.length() > 0
                    ? context + "\n\n请根据以上知识回答问题：\n\n问题：" + request.getQuestion()
                    : "请回答：\n\n问题：" + request.getQuestion();

            var advisor = MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(request.getSessionId())
                    .build();

            String reply = chatClient.prompt()
                    .user(prompt)
                    .advisors(advisor)
                    .call()
                    .content();

            return ApiResponse.success(Map.of("reply", reply, "knowledgeFound", knowledge.size()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Data
    public static class AddKnowledgeRequest {
        private String title;
        private String content;
        
        @JsonProperty("category")
        @Getter @Setter
        private String category = "general";
    }

    @Data
    public static class SearchRequest {
        private String query;
    }

    @Data
    public static class QaRequest {
        private String question;
        
        @JsonProperty("sessionId")
        @Getter @Setter
        private String sessionId = "default";
    }
}