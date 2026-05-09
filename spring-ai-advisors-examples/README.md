# spring-ai-advisors-examples

聊天记忆 (Chat Memory) 实践项目，专注于 Spring AI 的对话上下文管理。

## 项目简介

本项目演示了如何在 Spring AI 中使用 ChatMemory 接口实现多轮对话的上下文记忆功能。通过 Advisor 模式将记忆功能集成到 ChatClient 中，支持多会话隔离和消息窗口管理。

## 核心示例

### InMemoryChatMemoryRepositoryExample

**功能特点：**
- 使用 `InMemoryChatMemoryRepository` 作为内存存储
- 通过 `MessageWindowChatMemory` 管理消息窗口（最多保留 10 条消息）
- 自定义 `ChatMemoryWrapper` 添加日志记录功能

**演示场景：**

| 示例 | 说明 |
|------|------|
| 单轮对话 | 无记忆的独立对话 |
| 多轮对话（带记忆） | 使用 `MessageChatMemoryAdvisor` 记住对话历史 |
| 对话摘要 | 助手能记住并总结之前讨论过的内容 |
| 多会话独立记忆 | 通过 `conversationId` 隔离不同会话的记忆 |

**技术要点：**
- `MessageChatMemoryAdvisor` 将聊天记忆与 ChatClient 集成
- `ChatMemory.add()` 方法存储消息
- `ChatMemory.get()` 方法检索历史消息
- 多会话通过不同的 `conversationId` 实现隔离

## 环境配置

```properties
# 设置 MiniMax API 密钥
MINIMAX_API_KEY2=your-api-key
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+
- MiniMax 聊天模型

## 运行方式

```bash
cd spring-ai-advisors-examples
mvn spring-boot:run
```

或直接运行主类：
```java
com.example.chatmemory.InMemoryChatMemoryRepositoryExample
```