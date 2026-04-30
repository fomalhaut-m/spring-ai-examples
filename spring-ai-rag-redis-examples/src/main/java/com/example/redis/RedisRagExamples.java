package com.example.redis;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
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
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import redis.clients.jedis.JedisPooled;

/**
 * Spring AI Redis 向量存储 RAG 示例
 * 
 * 【关键点】
 * 1. 使用 Redis 作为向量数据库存储文档嵌入
 * 2. 支持相似性搜索查找相关文档
 * 3. 可结合大模型实现 RAG（检索增强生成）
 * 4. 支持元数据过滤
 * 
 * 【重要前置条件】
 * ★ 必须使用 Redis Stack 版本（普通 Redis 不支持向量搜索）
 * 启动命令：docker run -p 6379:6379 redis/redis-stack
 * 或：docker run -p 6379:6379 redislabs/redismod
 */
public class RedisRagExamples {

    public static void main(String[] args) {
        // 手动创建 EmbeddingModel
        EmbeddingModel embeddingModel = createEmbeddingModel();
        
        // 手动创建 VectorStore
        VectorStore vectorStore = createVectorStore(embeddingModel);
        
        // 执行示例
        System.out.println("=".repeat(60));
        System.out.println("Spring AI Redis RAG 示例");
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
    }

    /**
     * 添加文档到向量存储
     * 
     * 【关键点】
     * 1. Document 类包含文本和元数据
     * 2. 使用 Map.of() 创建元数据
     * 3. vectorStore.add() 自动将文档转换为嵌入向量并存储
     * 
     * @param vectorStore 向量存储
     */
    private static void addDocuments(VectorStore vectorStore) {
        System.out.println("\n--- 添加文档到 Redis 向量存储 ---");

        List<Document> documents = List.of(
                new Document("Spring AI 提供简洁易用的 API 将文档存储到 Redis 向量数据库中",
                        Map.of("category", "framework", "year", 2024)),
                new Document("Redis 是一个开源的内存数据结构存储，可用作数据库、缓存",
                        Map.of("category", "database", "year", 2023)),
                new Document("Spring Boot 3.3 支持 Java 17+ 的新特性",
                        Map.of("category", "framework", "year", 2024)),
                new Document("向量嵌入将文本转换为数值向量，用于语义搜索",
                        Map.of("category", "ai", "year", 2024)),
                new Document("RAG 结合检索和生成，提升问答系统的准确性",
                        Map.of("category", "ai", "year", 2024)),
                new Document("Redis Stack 提供全文搜索和向量搜索能力",
                        Map.of("category", "database", "year", 2023)));

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
     * 
     * @param vectorStore 向量存储
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
     * 3. 阈值越高结果越精确
     * 
     * @param vectorStore 向量存储
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
     * 1. 使用 FilterExpressionBuilder 构建过滤条件
     * 2. 支持 in、and、or、>=、<= 等操作符
     * 3. 元数据字段需要在 RedisVectorStore 中注册
     * 
     * @param vectorStore 向量存储
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
        System.out.println("过滤: category = 'database' AND year >= 2023");
        System.out.println("找到 " + results.size() + " 个相似文档:");

        for (Document doc : results) {
            System.out.println("  - " + doc.getText());
            System.out.println("    元数据: " + doc.getMetadata());
        }
    }

    /**
     * 删除文档
     * 
     * 【关键点】
     * 1. 需要文档 ID 列表
     * 2. 删除后无法恢复
     * 
     * @param vectorStore 向量存储
     */
    private static void deleteDocuments(VectorStore vectorStore) {
        System.out.println("\n--- 删除文档 ---");

        // 重新查询获取文档 ID
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
     * 创建 EmbeddingModel
     * 
     * 【关键点】
     * 1. 用于文本到向量的转换
     * 2. MiniMax 嵌入模型
     * 3. 需要 API 密钥
     */
    private static EmbeddingModel createEmbeddingModel() {
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
     * 创建 VectorStore
     * 
     * 【关键点】
     * 1. 使用 RedisVectorStore
     * 2. 需要 EmbeddingModel
     * 3. initializeSchema(true) 自动创建索引
     * 4. 注册元数据字段用于过滤
     */
    private static VectorStore createVectorStore(EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(new JedisPooled("localhost", 6379), embeddingModel)
                .indexName("spring-ai-rag-index")
                .prefix("rag:")
                .metadataFields(
                        RedisVectorStore.MetadataField.tag("category"),
                        RedisVectorStore.MetadataField.numeric("year"))
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
     * 
     * @param vectorStore 向量存储
     * @param chatModel 聊天模型
     */
    private static void ragQuestionAnswering(VectorStore vectorStore, ChatModel chatModel) {
        String question = "什么是 Spring AI?";

        // 1. 检索相关文档
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