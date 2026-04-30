package com.example.rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;

public class RagExamples {

    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }

        var chatModel = new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());

        System.out.println("=".repeat(60));
        System.out.println("Spring AI RAG 示例");
        System.out.println("=".repeat(60));

        basicRagDemo(chatModel);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    private static void basicRagDemo(ChatModel chatModel) {
        System.out.println("\n--- 基础 RAG 流程 ---");

        System.out.println("1. 文档加载 (Document Loading)");
        List<Document> documents = List.of(
                Document.builder()
                        .text("Spring Boot makes it easy to create Spring applications.")
                        .metadata(Map.of("source", "spring-boot-guide", "type", "framework"))
                        .build(),
                Document.builder()
                        .text("Spring AI simplifies building AI-powered applications.")
                        .metadata(Map.of("source", "spring-ai-guide", "type", "framework"))
                        .build()
        );
        System.out.println("加载文档数量: " + documents.size());

        System.out.println("2. 文本分割 (Text Chunking)");
        System.out.println("将长文档分割成较小的块");

        System.out.println("3. 向量化 (Embedding)");
        System.out.println("将文本转换为向量表示 (需要 EmbeddingModel)");

        System.out.println("4. 存储到向量数据库 (Vector Store)");
        System.out.println("存储向量以便快速相似性搜索 (需要 VectorStore)");

        System.out.println("5. 检索 (Retrieval)");
        System.out.println("根据用户查询找到相关文档 (需要 VectorStore)");

        System.out.println("6. 生成 (Generation)");
        System.out.println("将检索到的上下文提供给 LLM 生成回答");

        var chatClient = ChatClient.builder(chatModel).build();

        String context = "Spring Boot is a framework that makes it easy to create Spring applications. " +
                "Spring AI is a framework that simplifies building AI-powered applications.";

        String response = chatClient.prompt()
                .system("Based on the following context information, answer the user's question. " +
                        "Context: " + context)
                .user("What is Spring Boot?")
                .call()
                .content();

        System.out.println("用户: What is Spring Boot?");
        System.out.println("助手: " + response);
    }
}