package com.example.wiki.controller;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import com.example.wiki.common.ApiResponse;

/**
 * 记忆控制器
 */
@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final ChatMemory chatMemory;

    public MemoryController(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    /**
     * 清除会话记忆
     */
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Boolean> clearMemory(@PathVariable String sessionId) {
        try {
            chatMemory.clear(sessionId);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}