package com.example.mcp.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import io.modelcontextprotocol.client.McpSyncClient;

@SpringBootConfiguration
@AutoConfiguration
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {}))
public class McpServerClientExample implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpServerClientExample.class);

    @Autowired(required = false)
    private List<McpSyncClient> mcpSyncClients;

    @Autowired(required = false)
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    public static void main(String[] args) {
        SpringApplication.run(McpServerClientExample.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }

        var chatModel = createChatModel(apiKey);
        var chatClient = ChatClient.builder(chatModel).build();

        System.out.println("=".repeat(60));
        System.out.println("MCP Server 客户端测试");
        System.out.println("=".repeat(60));

        if (mcpSyncClients == null || mcpSyncClients.isEmpty()) {
            System.out.println("❌ 没有找到 MCP 客户端");
            System.out.println("请确认 MCP Server 已启动在 http://localhost:8080");
            return;
        }

        for (McpSyncClient client : mcpSyncClients) {
            System.out.println("\n[1] MCP 客户端: " + client.getClass().getSimpleName());
            var tools = client.listTools();
            System.out.println("    服务器工具: " + tools.tools().size() + " 个");
            for (var tool : tools.tools()) {
                System.out.println("    - " + tool.name());
            }
        }

        if (toolCallbackProvider != null) {
            System.out.println("\n[2] Spring AI 工具回调:");
            ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
            System.out.println("    工具数量: " + tools.length);
            for (ToolCallback tool : tools) {
                System.out.println("    - " + tool.getToolDefinition().name());
            }

            if (tools.length > 0) {
                System.out.println("\n[3] 测试工具调用...");
                String result = chatClient.prompt("计算 10 加 5 等于多少")
                        .toolCallbacks(tools)
                        .call()
                        .content();
                System.out.println("    用户: 计算 10 加 5 等于多少");
                System.out.println("    助手: " + result);
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试完成!");
        System.out.println("=".repeat(60));
    }

    @Bean
    public ChatModel chatModel() {
        String apiKey = System.getenv("MINIMAX_API_KEY2");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "your-api-key";
        }
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder().model("MiniMax-M2.7").build());
    }

    private ChatModel createChatModel(String apiKey) {
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder().model("MiniMax-M2.7").build());
    }
}