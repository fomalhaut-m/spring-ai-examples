package com.example.embedding;

import java.io.IOException;
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
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

public class MiniMaxEmbeddingExample {

    public static void main(String[] args) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key-here";
        }

        var embeddingModel = createEmbeddingModel(apiKey);

        runExamples(embeddingModel);
    }

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

    private static void embeddingWithCustomModel(EmbeddingModel embeddingModel) {
        System.out.println("\n--- 使用自定义模型 (embo-01) ---");
        String text = "人工智能正在改变世界";
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        List<Embedding> embedding = response.getResults();
        System.out.println("文本: " + text);
        System.out.println("模型: embo-01");
        System.out.println("嵌入维度: " + embedding.size());
    }

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