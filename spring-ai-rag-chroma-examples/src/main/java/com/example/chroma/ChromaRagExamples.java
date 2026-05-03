package com.example.chroma;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.minimax.api.MiniMaxApiConstants;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Spring AI Chroma 向量存储 RAG 示例
 * 
 * 【关键点】
 * 1. 使用 Chroma 作为向量数据库存储文档嵌入
 * 2. 支持相似性搜索查找相关文档
 * 3. 可结合大模型实现 RAG（检索增强生成）
 * 4. 支持元数据过滤
 * 
 * 【前置条件】
 * 1. Chroma 实例（本地或 Cloud）
 * 2. 本地运行：docker run -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
 */
@SpringBootApplication
public class ChromaRagExamples {

    public static void main(String[] args) {
        SpringApplication.run(ChromaRagExamples.class, args);
    }

    /**
     * 执行 RAG 示例
     */
    @Bean
    CommandLineRunner runner(VectorStore vectorStore) {
        return args -> {
            System.out.println("=".repeat(60));
            System.out.println("Spring AI Chroma RAG 示例");
            System.out.println("=".repeat(60));

            // 添加文档到向量存储
            addDocuments(vectorStore);

            // 基本相似性搜索
            basicSimilaritySearch(vectorStore);

            // 带阈值的相似性搜索
            similaritySearchWithThreshold(vectorStore);

            // 元数据过滤搜索
            filteredSearch(vectorStore);

            // 删除文档
            deleteDocuments(vectorStore);

            System.out.println("=".repeat(60));
            System.out.println("所有示例完成!");
            System.out.println("=".repeat(60));
        };
    }

    /**
     * 添加文档到向量存储
     * 
     * 【关键点】
     * 1. Document 类包含文本和元数据
     * 2. 使用 Map.of() 创建元数据
     * 3. vectorStore.add() 自动将文档转换为嵌入向量并存储
     */
    private static void addDocuments(VectorStore vectorStore) {
        System.out.println("\n--- 添加文档到 Chroma 向量存储 ---");

        List<Document> documents = List.of(
                new Document("Spring AI 提供简洁易用的 API 将文档存储到 Chroma 向量数据库中",
                        Map.of("category", "framework", "year", 2024)),
                new Document("Chroma 是一个开源的嵌入数据库，用于存储和搜索向量",
                        Map.of("category", "database", "year", 2024)),
                new Document("Spring Boot 3.3 支持 Java 17+ 的新特性",
                        Map.of("category", "framework", "year", 2024)),
                new Document("向量嵌入将文本转换为数值向量，用于语义搜索",
                        Map.of("category", "ai", "year", 2024)),
                new Document("RAG 结合检索和生成，提升问答系统的准确性",
                        Map.of("category", "ai", "year", 2024)),
                new Document("Chroma 支持元数据过滤和相似性搜索",
                        Map.of("category", "database", "year", 2024)));

        vectorStore.add(documents);

        System.out.println("已添加 " + documents.size() + " 个文档");
        for (Document doc : documents) {
            String preview = doc.getText().length() > 30 
                    ? doc.getText().substring(0, 30) + "..." 
                    : doc.getText();
            System.out.println("  - " + preview);
            System.out.println("    元数据: " + doc.getMetadata());
        }
    }

    /**
     * 基本相似性搜索
     * 
     * 【关键点】
     * 1. 使用 SearchRequest.builder() 构建搜索请求
     * 2. query() 设置查询文本
     * 3. topK() 设置返回结果数量
     * 4. 返回的 Document 包含 score 字段表示相似度
     */
    private static void basicSimilaritySearch(VectorStore vectorStore) {
        System.out.println("\n--- 基本相似性搜索 ---");

        SearchRequest searchRequest = SearchRequest.builder()
                .query("Spring AI 框架")
                .topK(3)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        System.out.println("查询: " + searchRequest.getQuery());
        System.out.println("topK: " + searchRequest.getTopK());
        System.out.println("找到 " + results.size() + " 个相似文档:");

        for (Document doc : results) {
            String preview = doc.getText().length() > 40 
                    ? doc.getText().substring(0, 40) + "..." 
                    : doc.getText();
            System.out.println("  - " + preview);
            System.out.println("    相似度分数: " + doc.getScore());
            System.out.println("    元数据: " + doc.getMetadata());
        }
    }

    /**
     * 带阈值的相似性搜索
     * 
     * 【关键点】
     * 1. similarityThreshold() 设置相似度阈值（0.0-1.0）
     * 2. 只返回超过阈值的文档
     */
    private static void similaritySearchWithThreshold(VectorStore vectorStore) {
        System.out.println("\n--- 带阈值的相似性搜索 ---");

        double threshold = 0.7;
        SearchRequest searchRequest = SearchRequest.builder()
                .query("Java 框架")
                .topK(5)
                .similarityThreshold(threshold)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        System.out.println("查询: " + searchRequest.getQuery());
        System.out.println("阈值: " + threshold);
        System.out.println("找到 " + results.size() + " 个相似文档:");

        for (Document doc : results) {
            String preview = doc.getText().length() > 40 
                    ? doc.getText().substring(0, 40) + "..." 
                    : doc.getText();
            System.out.println("  - " + preview);
            System.out.println("    分数: " + doc.getScore());
        }
    }

    /**
     * 元数据过滤搜索
     * 
     * 【关键点】
     * 1. 使用 Filter.Expression DSL
     * 2. 支持 in、and、or、eq 等操作符
     * 3. 过滤表达式会自动转换为 Chroma 的 where 格式
     */
    private static void filteredSearch(VectorStore vectorStore) {
        System.out.println("\n--- 元数据过滤搜索 ---");

        // 使用 FilterExpressionBuilder 构建过滤表达式
        Filter.Expression filter = new FilterExpressionBuilder()
                .and(
                        new FilterExpressionBuilder().in("category", "database"),
                        new FilterExpressionBuilder().gte("year", 2023)
                )
                .build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query("数据存储")
                .topK(5)
                .filterExpression(filter)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        System.out.println("查询: " + searchRequest.getQuery());
        System.out.println("过滤: category in ['database', 'ai'] AND year >= 2024");
        System.out.println("找到 " + results.size() + " 个相似文档:");

        for (Document doc : results) {
            System.out.println("  - " + doc.getText());
            System.out.println("    元数据: " + doc.getMetadata());
        }
    }

    /**
     * 删除文档
     */
    private static void deleteDocuments(VectorStore vectorStore) {
        System.out.println("\n--- 删除文档 ---");

        SearchRequest searchRequest = SearchRequest.builder()
                .query("Spring AI")
                .topK(10)
                .build();

        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        if (!docs.isEmpty()) {
            List<String> ids = docs.stream()
                    .map(Document::getId)
                    .toList();

            vectorStore.delete(ids);

            System.out.println("已删除 " + ids.size() + " 个文档");
        } else {
            System.out.println("没有找到要删除的文档");
        }
    }

    /**
     * EmbeddingModel Bean
     * 
     * 【关键点】
     * 1. 用于文本到向量的转换
     * 2. MiniMax 嵌入模型
     */
    public EmbeddingModel embeddingModel() {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key-here";
        }

        System.out.println(apiKey);

        return new MiniMaxEmbeddingModel(
                new MiniMaxApi(
                        MiniMaxApiConstants.DEFAULT_BASE_URL,
                        apiKey,
                        RestClient.builder()
                                .requestFactory(
                                        new JdkClientHttpRequestFactory(
                                                HttpClient.newBuilder()
                                                        .connectTimeout(Duration.ofSeconds(30))
                                                        .version(HttpClient.Version.HTTP_2)
                                                        .build()
                                        )
                                )
                                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                                .requestInterceptor((request, body, execution) -> {
                                    // 打印请求
                                    System.out.println("=== 请求开始 ===");
                                    System.out.println("方法: " + request.getMethod());
                                    System.out.println("地址: " + request.getURI());
                                    System.out.println("请求头: " + request.getHeaders());
                                    System.out.println("请求体: " + new String(body, StandardCharsets.UTF_8));

                                    // 执行请求
                                    ClientHttpResponse response = execution.execute(request, body);

                                    // 打印响应
                                    System.out.println("\n=== 响应开始 ===");
                                    System.out.println("状态码: " + response.getStatusCode());
                                    System.out.println("响应头: " + response.getHeaders());

                                    return response;
                                })
                ),
                MetadataMode.ALL,
                MiniMaxEmbeddingOptions.builder()
                        .model(MiniMaxApi.EmbeddingModel.Embo_01.getValue())
                        .build());
    }

    /**
     * ChatModel Bean
     */
    public ChatModel chatModel() {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }

        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());
    }

    /**
     * VectorStore Bean
     * 
     * 【关键点】
     * 1. 使用 ChromaVectorStore
     * 2. 需要 ChromaApi 和 EmbeddingModel
     * 3. initializeSchema(true) 自动创建集合
     */
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建 RestClient.Builder
        RestClient.Builder restClientBuilder = RestClient.builder();

        // 创建 ChromaApi
        ChromaApi chromaApi = new ChromaApi("http://localhost:8000", restClientBuilder, new ObjectMapper());

        // 创建 ChromaVectorStore
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName("spring-ai-rag-collection")
                .initializeSchema(true)
                .build();
    }

    /**
     * RAG 问答完整示例
     * 
     * 【关键点】
     * 1. 检索相关文档
     * 2. 构建上下文
     * 3. 调用大模型生成回答
     */
    private static void ragQuestionAnswering(VectorStore vectorStore, ChatModel chatModel) {
        String question = "什么是 Spring AI?";

        // 1. 检索相关文��
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .build());

        // 2. 构建上下文
        StringBuilder context = new StringBuilder();
        for (Document doc : relevantDocs) {
            context.append("- ").append(doc.getText()).append("\n");
        }

        // 3. 构建 prompt
        String prompt = "基于以下上下文回答问题。\n\n" +
                "上下文:\n" + context + "\n\n" +
                "问题: " + question + "\n\n" +
                "回答:";

        // 4. 调用大模型
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        String answer = chatClient.prompt(prompt).call().content();

        System.out.println("问题: " + question);
        System.out.println("回答: " + answer);
    }
}