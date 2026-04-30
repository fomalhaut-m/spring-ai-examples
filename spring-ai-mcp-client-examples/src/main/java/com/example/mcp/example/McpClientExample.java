package com.example.mcp.example;

import java.time.Duration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;

public class McpClientExample {

    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }

        var chatModel = createChatModel(apiKey);
        var chatClient = ChatClient.builder(chatModel).build();

        System.out.println("=".repeat(60));
        System.out.println("Spring AI MCP 客户端示例");
        System.out.println("=".repeat(60));

        stdioMcpDemo(chatClient);
        amapMcpDemo(chatClient);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    public static ChatModel createChatModel(String apiKey) {
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());
    }

    private static void stdioMcpDemo(ChatClient chatClient) {
        System.out.println("\n--- STDIO MCP 客户端 (本地进程协议) ---");
        String content = chatClient.prompt("创建 test.txt 文件，内容为 Hello MCP 加上当前时间")
                .toolCallbacks(stdioMcpTools())
                .call()
                .content();
        System.out.println("用户: 创建 test.txt 文件，内容为 Hello MCP");
        System.out.println("助手: " + content);
    }

    private static void amapMcpDemo(ChatClient chatClient) {
        System.out.println("\n--- 高德地图 MCP 客户端 (mcpmarket.cn) ---");
        try {
            String content = chatClient.prompt("规划从西安未央区到长安区的驾车路线")
                    .toolCallbacks(amapMcp())
                    .call()
                    .content();
            System.out.println("用户: 查询北京现在的天气");
            System.out.println("助手: " + content);
        } catch (Exception e) {
            System.out.println("高德 MCP 服务器连接失败: " + e.getMessage());
        }
    }

    public static ToolCallback[] stdioMcpTools() {
        ServerParameters params = ServerParameters.builder("npx.cmd") // windows
                .args("-y", "@modelcontextprotocol/server-filesystem", ".")
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);
        StdioClientTransport transport = new StdioClientTransport(params, jsonMapper);
        McpSyncClient mcpClient = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("spring-ai-mcp-demo", "1.0.0"))
                .requestTimeout(Duration.ofSeconds(60))
                .build();
        mcpClient.initialize();
        return new SyncMcpToolCallbackProvider(mcpClient).getToolCallbacks();
    }


    public static ToolCallback[] amapMcp() {
        HttpClientSseClientTransport transport = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=b5fbf3f5afb33ad74b1f8542629f8e17").build();
        McpSyncClient mcpClient = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("spring-ai-mcp-demo", "1.0.0"))
                .requestTimeout(Duration.ofSeconds(60))
                .build();
        mcpClient.initialize();
        return new SyncMcpToolCallbackProvider(mcpClient).getToolCallbacks();
    }
}