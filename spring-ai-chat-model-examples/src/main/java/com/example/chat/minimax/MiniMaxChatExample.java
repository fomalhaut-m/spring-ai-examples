package com.example.chat.minimax;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;

import reactor.core.publisher.Flux;

/**
 * MiniMax 聊天模型示例
 * 
 * 【关键点】
 * 1. 演示 MiniMax 模型的各种功能
 * 2. 支持基本对话、参数调优、系统消息、流式响应等
 * 3. 温度参数控制生成随机性
 * 4. 最大令牌数限制响应长度
 */
public class MiniMaxChatExample {

    /**
     * 主方法：运行聊天示例
     * 
     * 【关键点】
     * 1. 从环境变量读取 API 密钥
     * 2. 创建 ChatModel 和 ChatClient
     * 3. 运行所有示例
     */
    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key-here";
        }
        
        var chatModel = createChatModel(apiKey);
        var chatClient = ChatClient.builder(chatModel).build();
        
        runExamples(chatClient);
    }

    private static ChatModel createChatModel(String apiKey) {
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                .model("MiniMax-M2.7")
                .build());
    }

    private static void runExamples(ChatClient chatClient) {
        System.out.println("=".repeat(60));
        System.out.println("MiniMax Chat 用例");
        System.out.println("=".repeat(60));

        basicChat(chatClient);
        chatWithTemperature(chatClient);
        chatWithMaxTokens(chatClient);
        chatWithTopP(chatClient);
        chatWithSystemMessage(chatClient);
        streamChat(chatClient);
        chatWithPresencePenalty(chatClient);
        chatWithFrequencyPenalty(chatClient);
        chatWithStopSequences(chatClient);
        advancedChat(chatClient);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    private static void basicChat(ChatClient chatClient) {
        System.out.println("\n--- 基本对话 ---");
        String response = chatClient.prompt("法国的首都是什么?").call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }

    private static void chatWithTemperature(ChatClient chatClient) {
        System.out.println("\n--- 使用温度参数 (0.2) ---");
        String response = chatClient.prompt()
                .user("法国的首都是什么?")
                .options(ChatOptions.builder().temperature(0.2).build())
                .call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }

    private static void chatWithMaxTokens(ChatClient chatClient) {
        System.out.println("\n--- 使用最大令牌数 (50) ---");
        String response = chatClient.prompt()
                .user("法国的首都是什么?")
                .options(ChatOptions.builder().maxTokens(50).build())
                .call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }

    private static void chatWithTopP(ChatClient chatClient) {
        System.out.println("\n--- 使用TopP采样 (0.5) ---");
        String response = chatClient.prompt()
                .user("法国的首都是什么?")
                .options(ChatOptions.builder().topP(0.5).build())
                .call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }

    private static void chatWithSystemMessage(ChatClient chatClient) {
        System.out.println("\n--- 使用系统消息 ---");
        String response = chatClient.prompt()
                .system("你是一个有帮助的助手,请用诗歌的风格回答问题。")
                .user("法国的首都是什么?")
                .call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }

    private static void streamChat(ChatClient chatClient) {
        System.out.println("\n--- 流式响应 ---");
        System.out.println("用户: 法国的首都是什么?");
        System.out.print("助手: ");
        Flux<String> response = chatClient.prompt()
                .user("法国的首都是什么?")
                .stream().content();
        System.out.println("\n(流式响应已接收)");
    }

    private static void chatWithPresencePenalty(ChatClient chatClient) {
        System.out.println("\n--- 使用存在惩罚 (1.0) ---");
        String response = chatClient.prompt()
                .user("介绍一下法国。")
                .options(ChatOptions.builder().presencePenalty(1.0).build())
                .call().content();
        System.out.println("用户: 介绍一下法国。");
        System.out.println("助手: " + response);
    }

    private static void chatWithFrequencyPenalty(ChatClient chatClient) {
        System.out.println("\n--- 使用频率惩罚 (1.0) ---");
        String response = chatClient.prompt()
                .user("介绍一下法国。")
                .options(ChatOptions.builder().frequencyPenalty(1.0).build())
                .call().content();
        System.out.println("用户: 介绍一下法国。");
        System.out.println("助手: " + response);
    }

    private static void chatWithStopSequences(ChatClient chatClient) {
        System.out.println("\n--- 使用停止序列 ---");
        String response = chatClient.prompt()
                .user("给我讲一个小故事。")
                .options(ChatOptions.builder().stopSequences(List.of("结束")).build())
                .call().content();
        System.out.println("用户: 给我讲一个小故事。");
        System.out.println("助手: " + response);
    }

    private static void advancedChat(ChatClient chatClient) {
        System.out.println("\n--- 组合多个选项 ---");
        String response = chatClient.prompt()
                .user("法国的首都是什么?")
                .options(ChatOptions.builder()
                        .temperature(0.7)
                        .maxTokens(100)
                        .topP(0.9)
                        .presencePenalty(0.5)
                        .frequencyPenalty(0.5)
                        .build())
                .call().content();
        System.out.println("用户: 法国的首都是什么?");
        System.out.println("助手: " + response);
    }
}