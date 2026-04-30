package com.example.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.mcp.provider.SpringAiToolProvider;

/**
 * MCP 服务器应用程序
 * 
 * 【关键点】
 * 1. 这是一个 Spring Boot 应用，作为 MCP 服务器运行
 * 2. 提供工具、资源、提示词等功能给 MCP 客户端
 * 3. 支持多种传输方式：STDIO、SSE、Streamable HTTP
 * 4. 通过 @McpTool、@McpResource、@McpPrompt 等注解声明功能
 */
@SpringBootApplication
public class McpServerApplication {

	/**
	 * 主方法：启动 MCP 服务器
	 * 
	 * 【关键点】
	 * 1. SpringApplication.run() 启动应用
	 * 2. 根据配置选择传输方式（STDIO、SSE、Streamable HTTP）
	 */
	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	/**
	 * 注册 Spring AI 工具回调提供者作为 MCP 工具
	 * 
	 * 【关键点】
	 * 1. 将 Spring AI 的 ToolCallbackProvider 注册为 Bean
	 * 2. Spring AI 的 @Tool 注解工具可以与 MCP 的 @McpTool 注解工具一起使用
	 * 3. MethodToolCallbackProvider 将方法自动转换为工具回调
	 * 
	 * @param weatherService 天气服务提供者
	 * @return 工具回调提供者
	 */
	@Bean
	ToolCallbackProvider weatherTools(SpringAiToolProvider weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}
}
