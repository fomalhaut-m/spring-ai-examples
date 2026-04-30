package com.example.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * MCP 客户端处理器提供者
 * 
 * 【关键点】
 * 1. 提供 MCP 客户端的各种事件处理器
 * 2. 支持进度通知、日志记录、采样、引导等功能
 * 3. 通过注解声明式配置处理器
 * 4. clients 参数指定处理器应用的客户端 ID
 */
@Service
public class McpClientHandlerProviders {

	private static final Logger logger = LoggerFactory.getLogger(McpClientHandlerProviders.class);

	/**
	 * 处理进度通知
	 * 
	 * 【关键点】
	 * 1. @McpProgress 注解标记这是进度通知处理器
	 * 2. clients = "server1" 表示只处理来自 server1 的通知
	 * 3. 进度通知包含进度令牌、当前进度、总进度和消息
	 * 4. 用于长时间运行的工具调用状态更新
	 * 
	 * @param progressNotification 从服务器接收的进度通知
	 */
	@McpProgress(clients = "server1")
	public void progressHandler(ProgressNotification progressNotification) {
		logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
				progressNotification.progressToken(), progressNotification.progress(),
				progressNotification.total(), progressNotification.message());
	}

	/**
	 * 处理日志消息通知
	 * 
	 * 【关键点】
	 * 1. @McpLogging 注解标记这是日志通知处理器
	 * 2. 日志包含日志级别和消息内容
	 * 3. 用于记录工具执行过程中的日志信息
	 * 
	 * @param loggingMessage 日志消息通知
	 */
	@McpLogging(clients = "server1")
	public void loggingHandler(LoggingMessageNotification loggingMessage) {
		logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
	}

	/**
	 * 处理采样请求
	 * 
	 * 【关键点】
	 * 1. @McpSampling 注解标记这是采样处理器
	 * 2. 当服务器需要调用 LLM 时会触发此方法
	 * 3. 返回 CreateMessageResult 包含 LLM 响应
	 * 4. 模型偏好可影响模型选择
	 * 
	 * @param llmRequest LLM 请求
	 * @return 消息创建结果
	 */
	@McpSampling(clients = "server1")
	public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
		logger.info("MCP SAMPLING: {}", llmRequest);

		String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
		String modelHint = llmRequest.modelPreferences().hints().get(0).name();

		return CreateMessageResult.builder()
				.content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
				.build();
	}

	/**
	 * 用于引导的 Person 记录
	 * 
	 * 【关键点】
	 * 1. 使用 Number 而非 Integer/Long，因为 MCP 只支持 Number 类型
	 * 2. 包含名称和年龄字段
	 */
	public record Person(String name, Number age) {}

	/**
	 * 处理引导请求
	 * 
	 * 【关键点】
	 * 1. @McpElicitation 注解标记这是引导处理器
	 * 2. 引导用于从用户获取结构化数据
	 * 3. 返回 StructuredElicitResult 包含引导结果
	 * 4. Action 可以是 ACCEPT（接受）或 CANCEL（取消）
	 * 
	 * @param request 引导请求
	 * @return 结构化引导结果
	 */
	@McpElicitation(clients = "server1")
	public StructuredElicitResult<Person> elicitationHandler(McpSchema.ElicitRequest request) {
		logger.info("MCP ELICITATION: {}", request);
		return new StructuredElicitResult<>(ElicitResult.Action.ACCEPT, new Person("John Doe", 42), null);
	}

}
