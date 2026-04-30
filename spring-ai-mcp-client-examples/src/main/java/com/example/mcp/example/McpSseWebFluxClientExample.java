package com.example.mcp.example;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

public class McpSseWebFluxClientExample {

    private static final Logger logger = LoggerFactory.getLogger(McpSseWebFluxClientExample.class);

    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }

        String serverUrl = "http://localhost:8082";

        var chatModel = createChatModel(apiKey);
        var chatClient = ChatClient.builder(chatModel).build();

        System.out.println("=".repeat(60));
        System.out.println("WebFlux MCP Client 连接到 " + serverUrl);
        System.out.println("=".repeat(60));

        testMcpConnection(chatClient, serverUrl);

        System.out.println("=".repeat(60));
        System.out.println("测试完成!");
        System.out.println("=".repeat(60));
    }

    private static void testMcpConnection(ChatClient chatClient, String serverUrl) {
        String sseUrl = serverUrl + "/sse";
        
        System.out.println("\n[1] 连接 MCP 服务器...");
        System.out.println("    SSE 端点: " + sseUrl);

        try {
            HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(sseUrl).build();

            McpSyncClient mcpClient = McpClient.sync(transport)
                    .clientInfo(new McpSchema.Implementation("webflux-client", "1.0.0"))
                    .requestTimeout(Duration.ofSeconds(30))
                    .build();

            System.out.println("\n[2] 初始化连接...");
            mcpClient.initialize();
            System.out.println("    初始化成功!");

            System.out.println("\n[3] 获取工具列表...");
            McpSchema.ListToolsResult toolsResult = mcpClient.listTools();
            System.out.println("    服务器工具: " + toolsResult.tools().size() + " 个");
            for (McpSchema.Tool tool : toolsResult.tools()) {
                System.out.println("    - " + tool.name());
            }

            ToolCallback[] tools = new SyncMcpToolCallbackProvider(mcpClient).getToolCallbacks();
            System.out.println("    Spring AI 工具: " + tools.length + " 个");

            if (tools.length > 0 && chatClient != null) {
                System.out.println("\n[4] 测试工具调用...");
                String result = chatClient.prompt("计算 10 加 5 等于多少")
                        .toolCallbacks(tools)
                        .call()
                        .content();
                System.out.println("    用户: 计算 10 加 5 等于多少");
                System.out.println("    助手: " + result);
            }

            mcpClient.close();

        } catch (Exception e) {
            System.err.println("    错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ChatModel createChatModel(String apiKey) {
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());
    }
}