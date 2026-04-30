/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mcp;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.*;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest.CompleteArgument;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;

/**
 * MCP 客户端应用程序
 * 
 * 【关键点】
 * 1. 这是一个 Spring Boot 应用，用于演示 MCP 客户端功能
 * 2. MCP（Model Context Protocol）是一个标准化协议，用于连接 AI 模型和外部工具/资源
 * 3. 支持工具调用、资源读取、提示词模板等功能
 * 4. 通过 CommandLineRunner 在启动时自动运行示例
 */
@SpringBootApplication
public class McpClientApplication {

	/**
	 * 主方法：启动 Spring Boot 应用
	 * 
	 * 【关键点】
	 * 1. SpringApplication.run() 启动应用
	 * 2. close() 在应用退出时关闭上下文
	 */
	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args).close();
	}

	/**
	 * CommandLineRunner Bean：在应用启动后自动运行
	 * 
	 * 【关键点】
	 * 1. Spring 自动注入所有配置的 McpSyncClient
	 * 2. 演示工具调用、提示词、资源读取、自动完成等功能
	 * 3. progressToken 用于接收进度通知
	 * 4. CompleteRequest 用于参数自动完成
	 * 
	 * @param mcpClients MCP 客户端列表
	 * @return CommandLineRunner 实例
	 */
	@Bean
	public CommandLineRunner predefinedQuestions(
			List<McpSyncClient> mcpClients) {

		return args -> {

			for (McpSyncClient mcpClient : mcpClients) {
				System.out.println(">>> MCP Client: " + mcpClient.getClientInfo());

				// Call a tool that sends progress notifications
				CallToolRequest toolRequest = CallToolRequest.builder()
						.name("tool1")
						.arguments(Map.of("input", "test input"))
						.progressToken(666)
						.build();
				CallToolResult response = mcpClient.callTool(toolRequest);
				System.out.println("Tool response: " + response);

				CompleteResult nameCompletion = mcpClient.completeCompletion(
					new CompleteRequest(
						new PromptReference("personalized-message"), 
						new CompleteArgument("name", "J")));

				System.out.println("Name completions: " + nameCompletion.completion());

				String nameValue = nameCompletion.completion().values().get(3);

				try {
					GetPromptResult promptResponse = mcpClient
						.getPrompt(new GetPromptRequest("personalized-message", Map.of("name", nameValue)));

					System.out.println("Prompt response: " + promptResponse);
				} catch (Exception e) {
					System.err.println("Error getting prompt: " + e.getMessage());
				}

				nameCompletion = mcpClient.completeCompletion(
					new CompleteRequest(
						new ResourceReference("user-status://{username}"), 
						new CompleteArgument("username", "J")));

				System.out.println("Name completions: " + nameCompletion.completion());

				var resourceResponse = mcpClient.readResource(new ReadResourceRequest("user-status://" + nameCompletion.completion().values().get(0)));

				System.out.println("Resource response: " + resourceResponse);
				
			}
		};
	}
}