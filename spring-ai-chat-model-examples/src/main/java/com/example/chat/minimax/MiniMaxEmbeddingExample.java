package com.example.chat.minimax;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.minimax.api.MiniMaxApiConstants;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * MiniMax 嵌入模型示例
 * 
 * 本类演示如何使用 Spring AI 的 MiniMax 嵌入模型
 * 嵌入模型将文本转换为向量表示，可用于：
 * - 语义搜索
 * - 文本相似度计算
 * - RAG（检索增强生成）
 * - 文本分类
 * 
 * 【关键点】
 * 1. 嵌入模型需要 API 密钥，通过环境变量 MINIMAX_API_KEY 配置
 * 2. 嵌入向量可用于计算文本之间的余弦相似度
 * 3. 支持单文本和多文本嵌入
 */
public class MiniMaxEmbeddingExample {

    /**
     * 主方法：运行所有嵌入模型示例
     * 
     * 【关键点】
     * 1. 从环境变量 MINIMAX_API_KEY 读取 API 密钥
     * 2. 如果未设置，使用默认值作为占位符
     */
    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key-here";
        }

        System.out.println(apiKey);

        var embeddingModel = createEmbeddingModel(apiKey);

        runExamples(embeddingModel);
    }

    /**
     * 创建 MiniMax 嵌入模型实例
     * 
     * 【关键点】
     * 1. 使用 MiniMaxApi 配置 API 基础 URL 和密钥
     * 2. 配置 HTTP 客户端，添加请求拦截器用于调试
     * 3. 使用 embo-01 作为默认嵌入模型
     * 4. MetadataMode.ALL 表示保留所有元数据
     * 
     * @param apiKey MiniMax API 密钥
     * @return 配置好的 EmbeddingModel 实例
     */
    private static EmbeddingModel createEmbeddingModel(String apiKey) {
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
                                .requestInterceptor((request, body, execution) ->  {
                                    // ===================== 打印请求 =====================
                                    System.out.println("=== 请求开始 ===");
                                    System.out.println("方法: " + request.getMethod());
                                    System.out.println("地址: " + request.getURI());
                                    System.out.println("请求头: " + request.getHeaders());
                                    System.out.println("请求体: " + new String(body, StandardCharsets.UTF_8));

                                    // 执行请求
                                    ClientHttpResponse response = execution.execute(request, body);

                                    // ===================== 打印响应（关键：用官方包装类） =====================

                                    System.out.println("\n=== 响应开始 ===");
                                    System.out.println("状态码: " + response.getStatusCode());
                                    System.out.println("响应头: " + response.getHeaders());

                                    // 不可以读取 响应体, 否则会报异常
                                    return response;
                                })
                ),
                MetadataMode.ALL,
                MiniMaxEmbeddingOptions.builder()
                        .model(MiniMaxApi.EmbeddingModel.Embo_01.getValue())
                        .build());
    }

    /**
     * 运行所有嵌入示例
     * 
     * 【关键点】
     * 1. 依次执行多个示例方法
     * 2. 每个示例演示不同的嵌入功能
     * 
     * @param embeddingModel 嵌入模型实例
     */
    private static void runExamples(EmbeddingModel embeddingModel) {
        System.out.println("=".repeat(60));
        System.out.println("MiniMax Embedding 用例");
        System.out.println("=".repeat(60));

        basicEmbedding(embeddingModel);
        multipleTextsEmbedding(embeddingModel);
        embeddingWithCustomModel(embeddingModel);
        calculateCosineSimilarity(embeddingModel);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    /**
     * 基本嵌入示例：单个文本嵌入
     * 
     * 【关键点】
     * 1. 使用 embedForResponse() 方法获取完整响应
     * 2. 响应包含嵌入向量和元数据
     * 3. 嵌入向量是浮点数数组，表示文本的语义特征
     * 
     * @param embeddingModel 嵌入模型实例
     */
    private static void basicEmbedding(EmbeddingModel embeddingModel) {
        System.out.println("\n--- 基本嵌入 ---");
        String text = "法国的首都是巴黎";
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        List<Embedding> embedding = response.getResults();
        System.out.println("文本: " + text);
        System.out.println("嵌入维度: " + embedding.size());
        float[] values = embedding.get(0).getOutput();
        System.out.print("前5个值: ");
        for (int i = 0; i < Math.min(5, values.length); i++) {
            System.out.print(values[i] + " ");
        }
        System.out.println();
    }

    /**
     * 多文本嵌入示例：一次性嵌入多个文本
     * 
     * 【关键点】
     * 1. 批量嵌入比逐个嵌入更高效
     * 2. 每个文本都会生成独立的嵌入向量
     * 3. 适用于批量处理文档、知识库构建等场景
     * 
     * @param embeddingModel 嵌入模型实例
     */
    private static void multipleTextsEmbedding(EmbeddingModel embeddingModel) {
        System.out.println("\n--- 多文本嵌入 ---");
        List<String> texts = List.of("法国的首都是巴黎", "日本的首都是东京", "英国的首都是伦敦");
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        List<Embedding> embeddings = response.getResults();
        System.out.println("文本列表: " + texts);
        System.out.println("嵌入数量: " + embeddings.size());
        for (int i = 0; i < embeddings.size(); i++) {
            float[] embedding = embeddings.get(i).getOutput();
            System.out.println("文本 " + (i + 1) + " 维度: " + embedding.length);
        }
    }

    /**
     * 使用自定义模型示例
     * 
     * 【关键点】
     * 1. embo-01 是 MiniMax 的嵌入模型
     * 2. 不同模型可能有不同的向量维度
     * 3. 可根据需求选择合适的模型
     * 
     * @param embeddingModel 嵌入模型实例
     */
    private static void embeddingWithCustomModel(EmbeddingModel embeddingModel) {
        System.out.println("\n--- 使用自定义模型 (embo-01) ---");
        String text = "人工智能正在改变世界";
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        List<Embedding> embedding = response.getResults();
        System.out.println("文本: " + text);
        System.out.println("模型: embo-01");
        System.out.println("嵌入维度: " + embedding.size());
    }

    /**
     * 计算余弦相似度示例
     * 
     * 【关键点】
     * 1. 余弦相似度衡量两个向量的相似程度
     * 2. 范围从 -1（完全相反）到 1（完全相同）
     * 3. 语义相似的文本相似度较高
     * 4. 用于语义搜索、RAG 等场景
     * 
     * @param embeddingModel 嵌入模型实例
     */
    private static void calculateCosineSimilarity(EmbeddingModel embeddingModel) {
        System.out.println("\n--- 计算余弦相似度 ---");
        String text1 = "法国的首都是巴黎";
        String text2 = "巴黎是法国的首都";
        String text3 = "日本的首都是东京";

        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text1, text2, text3));
        List<Embedding> embeddings = response.getResults();

        float[] embedding1 = embeddings.get(0).getOutput();
        float[] embedding2 = embeddings.get(1).getOutput();
        float[] embedding3 = embeddings.get(2).getOutput();

        double similarity12 = cosineSimilarity(embedding1, embedding2);
        double similarity13 = cosineSimilarity(embedding1, embedding3);

        System.out.println("文本1: " + text1);
        System.out.println("文本2: " + text2);
        System.out.println("文本3: " + text3);
        System.out.println("文本1与文本2相似度: " + String.format("%.4f", similarity12));
        System.out.println("文本1与文本3相似度: " + String.format("%.4f", similarity13));
    }

    /**
     * 计算两个向量的余弦相似度
     * 
     * 【关键点】
     * 1. 余弦相似度 = 点积 / (||A|| * ||B||)
     * 2. 适用于高维向量的相似度比较
     * 3. 对向量长度不敏感，只关注方向
     * 
     * @param a 向量 A
     * @param b 向量 B
     * @return 余弦相似度值
     */
    private static double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}