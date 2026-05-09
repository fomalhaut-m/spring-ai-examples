# spring-ai-mcp-client-examples

Spring AI MCP (Model Context Protocol) 客户端示例，演示如何连接外部 MCP 服务器并使用其提供的工具。

## 项目简介

MCP 是一种开放协议，用于将 AI 模型与外部工具和数据源连接。本项目展示了如何使用 Spring AI 的 MCP 客户端功能，连接不同类型的 MCP 服务器。

## 核心示例

### McpClientExample

**功能特点：**
- 支持多种传输协议（SSE、Streamable HTTP、STDIO）
- 自动将 MCP 工具注册为 Spring AI 工具
- 与 MiniMax 大模型集成

**演示场景：**

#### 1. STDIO 传输示例

通过标准输入输出连接本地 MCP 服务器。

**示例工具：**
- `@modelcontextprotocol/server-filesystem` - 文件系统操作
  - 创建文件
  - 读取文件
  - 写入文件

```
npx -y @modelcontextprotocol/server-filesystem .
```

#### 2. SSE 传输示例

通过 Server-Sent Events 连接远程 MCP 服务器。

**示例：**
- 高德地图 MCP 服务 (mcpmarket.cn)
  - 驾车路线规划
  - 天气查询
  - 地理编码

## 技术架构

```
┌─────────────────┐      STDIO/SSE      ┌─────────────────┐
│   Spring AI     │ ──────────────────► │   MCP Server    │
│   ChatClient    │                     │  (本地/远程)     │
└─────────────────┘                     └─────────────────┘
        │
        ▼
┌─────────────────┐
│   MiniMax LLM    │
└─────────────────┘
```

## 环境配置

```properties
# MiniMax API 密钥
MINIMAX_API_KEY2=your-api-key

# 高德地图 MCP 密钥（可选）
# 从 mcpmarket.cn 获取
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+
- Jackson 2.19.4
- MiniMax 聊天模型
- MCP Java SDK

## 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

## 运行方式

```bash
cd spring-ai-mcp-client-examples
mvn spring-boot:run
```

或直接运行主类：
```java
com.example.mcp.example.McpClientExample
```

## MCP 服务器列表

可从 [mcpmarket.cn](https://www.mcpmarket.cn) 获取更多 MCP 服务器。

## 特点

1. **多协议支持**：STDIO（本地进程）、SSE（远程服务）、Streamable HTTP
2. **自动工具转换**：MCP 工具自动转换为 Spring AI ToolCallback
3. **会话管理**：支持 MCP 协议的会话功能
4. **灵活配置**：支持超时设置、客户端信息配置