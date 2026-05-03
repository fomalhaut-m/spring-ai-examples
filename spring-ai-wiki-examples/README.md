# Spring AI Wiki

基于 Spring AI 的智能知识库系统，支持多会话聊天、向量搜索和 MCP 工具调用。

## 功能特性

### 1. 智能对话
- 支持流式输出（SSE）
- 多会话管理，每个会话独立记忆
- 基于 MongoDB 的上下文记忆

### 2. 知识库
- 文档添加与向量存储（Chroma）
- 语义搜索
- RAG 问答（检索增强生成）

### 3. MCP 集成
- 高德地图工具调用
- 可扩展的 MCP 客户端架构

## 环境要求

- Java 17+
- Docker（用于运行中间件）
- MiniMax API Key

## 快速开始

### 1. 启动中间件

```bash
# 启动 MongoDB 和 Chroma
./start-env.sh

# 如需清理数据重新开始
./start-env.sh --reset
```

### 2. 配置环境变量

```bash
# MiniMax Chat API Key
export MINIMAX_API_KEY=your-chat-api-key

# MiniMax Embedding API Key（如与 Chat 不同）
export MINIMAX_API_KEY2=your-embedding-api-key
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

## 访问地址

- 首页：http://localhost:8080/
- 聊天页面：http://localhost:8080/chat
- 知识库页面：http://localhost:8080/knowledge
- 地图页面：http://localhost:8080/map

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/chat` | POST | 普通对话 |
| `/api/chat/stream` | POST | 流式对话 |
| `/api/knowledge` | POST | 添加知识 |
| `/api/knowledge/search` | GET | 搜索知识 |
| `/api/knowledge/qa` | POST | RAG 问答 |
| `/api/memory/{sessionId}` | DELETE | 清除会话记忆 |

## 配置说明

### application.yml

```yaml
spring:
  ai:
    minimax:
      api-key: ${MINIMAX_API_KEY}
      chat:
        options:
          model: MiniMax-M2.7
      embedding:
        api-key: ${MINIMAX_API_KEY2}
        options:
          model: embo-01

  data:
    mongodb:
      host: localhost
      port: 27017
      database: spring-ai-wiki
      username: admin
      password: admin
      authentication-database: admin

  vectorstore:
    chroma:
      client:
        host: http://localhost
        port: 8000
```

## 中间件配置

### MongoDB
- 端口：27017
- 用途：存储聊天记忆

### Chroma
- 端口：8000
- 用途：向量存储，支持语义搜索

## 启动检查

应用启动时会自动执行健康检查：

```
Environment Health Check:
  MiniMax Chat API ............................. OK
  MiniMax Embedding API ......................... OK (dim=1024)
  MongoDB ...................................... OK (CRUD)
  Chroma VectorStore ............................ OK

Health Check: All OK
```

如果任何检查失败，应用将无法启动。

## 目录结构

```
spring-ai-wiki-examples/
├── src/main/java/com/example/wiki/
│   ├── WikiApplication.java          # 启动类
│   ├── config/
│   │   ├── ChatClientConfig.java     # ChatClient 配置
│   │   └── EnvironmentHealthIndicator.java  # 健康检查
│   ├── controller/
│   │   ├── ChatController.java       # 聊天接口
│   │   ├── KnowledgeController.java  # 知识库接口
│   │   ├── MemoryController.java     # 记忆管理接口
│   │   └── WebController.java        # 页面路由
│   ├── service/
│   │   └── WikiService.java         # 核心服务
│   └── model/
│       └── KnowledgeEntry.java       # 知识实体
├── src/main/resources/
│   ├── application.yml               # 配置文件
│   └── templates/                   # HTML 模板
├── docker-compose.yml               # 中间件编排
└── start-env.sh                    # 环境启动脚本
```

## 常见问题

### 1. Chroma 409 冲突错误
```
HTTP 409 - Collection already exists
```
解决：运行 `./start-env.sh --reset` 清理数据卷

### 2. MiniMax API 401/403 错误
检查 API Key 是否正确，是否有对应的模型权限

### 3. MongoDB 认证失败
确认 `authentication-database` 设置为 `admin`
