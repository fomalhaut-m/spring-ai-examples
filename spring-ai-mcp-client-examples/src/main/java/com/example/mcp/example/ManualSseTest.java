package com.example.mcp.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ManualSseTest {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String sseUrl = "http://localhost:8080/sse";

        System.out.println("连接 SSE：" + sseUrl);

        CompletableFuture<String> endpointFuture = new CompletableFuture<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sseUrl))
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(10))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body().forEach(line -> {
                        System.out.println("SSE 接收：" + line);
                        if (line.startsWith("data:")) {
                            String path = line.substring(5).trim();
                            String fullUrl = "http://localhost:8080" + path;
                            System.out.println("✅ 动态消息地址：" + fullUrl);
                            endpointFuture.complete(fullUrl);
                        }
                    });
                });

        // 等待获取 endpoint
        String messageUrl = endpointFuture.get();
        System.out.println("\n🎉 成功获取消息地址：" + messageUrl);
    }
}