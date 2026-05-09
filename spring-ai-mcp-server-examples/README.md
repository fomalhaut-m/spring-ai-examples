# spring-ai-mcp-server-examples

Spring AI MCP (Model Context Protocol) 服务器示例，演示如何将本地工具暴露为 MCP 服务供 AI 应用使用。

## 项目简介

本项目展示了如何使用 Spring AI 构建 MCP 服务器，通过注解驱动的方式将 Spring Bean 方法快速暴露为 MCP 工具、资源和提示模板。

## 核心组件

### 1. 工具提供者 (ToolProvider)

使用 `@McpTool` 和 `@McpToolParam` 注解声明 MCP 工具。

**功能特点：**
- 天气查询功能
- 支持经纬度定位
- 调用 Open-Meteo API 获取实时温度

```java
@McpTool(description = "Get the temperature (in celsius) for a specific location")
public WeatherResponse getTemperature(
    @McpToolParam(description = "The location latitude") double latitude,
    @McpToolParam(description = "The location longitude") double longitude,
    @McpToolParam(description = "The city name") String city)
```

### 2. 其他 Provider 示例

| 类名 | 功能说明 |
|------|----------|
| `AsyncToolProvider` | 异步工具示例 |
| `CompletionProvider` | 文本补全功能 |
| `DocumentProvider` | 文档搜索/检索 |
| `PromptProvider` | 提示模板管理 |
| `SpringAiToolProvider` | Spring AI 工具集成 |
| `ToolProvider2/3` | 更多工具示例 |
| `UserProfileResourceProvider` | 用户资源管理 |

## 技术架构

```
┌─────────────────┐      STDIO/SSE      ┌─────────────────┐
│   MCP Client    │ ◄────────────────── │   MCP Server    │
│   (AI 应用)     │                     │   (Spring AI)   │
└─────────────────┘                     └─────────────────┘
                                              │
                                              ▼
                                    ┌─────────────────┐
                                    │  @McpTool       │
                                    │  @McpResource   │
                                    │  @McpPrompt     │
                                    └─────────────────┘
                                              │
                                              ▼
                                    ┌─────────────────┐
                                    │   Service Bean  │
                                    └─────────────────┘
```

## 环境配置

```properties
# 服务器配置（在 application.properties 中）
spring.application.name=spring-ai-mcp-server
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+
- Spring WebFlux（非阻塞 I/O）
- MCP 协议支持

## 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

## 支持的传输方式

| 传输方式 | 说明 |
|----------|------|
| STDIO | 标准输入输出，用于本地进程通信 |
| SSE | Server-Sent Events，支持远程客户端 |
| Streamable HTTP | HTTP 流式传输 |

## MCP 注解说明

| 注解 | 用途 |
|------|------|
| `@McpTool` | 声明一个工具方法 |
| `@McpToolParam` | 声明工具参数及描述 |
| `@McpResource` | 声明一个资源 |
| `@McpPrompt` | 声明一个提示模板 |

## 运行方式

```bash
cd spring-ai-mcp-server-examples
mvn spring-boot:run
```

## 测试客户端

项目包含测试类用于验证 MCP 服务器功能：

- `ClientSse.java` - SSE 客户端测试
- `ClientStdio.java` - STDIO 客户端测试
- `ClientStreamableHttp.java` - HTTP 流式客户端测试
- `SampleClient.java` - 示例客户端

## 特点

1. **注解驱动**：使用 `@McpTool` 快速创建工具
2. **自动元数据生成**：自动生成工具描述和参数 Schema
3. **多传输协议**：支持 STDIO、SSE、Streamable HTTP
4. **非阻塞 I/O**：基于 WebFlux 实现高性能
5. **易于扩展**：通过添加新方法轻松扩展工具集