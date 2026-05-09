# spring-ai-rag-chroma-examples

Spring AI + Chroma 向量数据库 RAG 示例，演示如何使用 Chroma 实现语义搜索和检索增强生成。

## 项目简介

Chroma 是一个开源的嵌入数据库，专注于向量存储和相似性搜索。本项目展示了如何将 Spring AI 与 Chroma 集成，构建完整的 RAG 应用。

## 环境前置条件

### 启动 Chroma 服务

```bash
# 使用 Docker 启动 Chroma
docker run -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
```

Chroma 服务地址：`http://localhost:8000`

## 核心示例

### ChromaRagExamples

**功能特点：**
- 使用 ChromaVectorStore 存储和检索文档
- 结合 MiniMax 嵌入模型进行向量化
- 支持元数据过滤和相似度阈值
- 完整的文档 CRUD 操作

**演示场景：**

| 示例 | 说明 |
|------|------|
| 添加文档 | 批量添加带元数据的文档 |
| 基本相似性搜索 | 基于语义找到最相关的文档 |
| 带阈值的搜索 | 只返回相似度超过阈值的文档 |
| 元数据过滤 | 按 category、year 等字段筛选 |
| 删除文档 | 根据 ID 删除文档 |

## 技术架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Spring    │────►│   MiniMax   │────►│   Chroma    │
│   AI RAG    │     │  Embedding  │     │ VectorStore │
└─────────────┘     └─────────────┘     └─────────────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │  语义搜索   │
                                        │ Similarity  │
                                        └─────────────┘
```

## 核心代码

### VectorStore 配置

```java
ChromaApi chromaApi = new ChromaApi("http://localhost:8000", restClientBuilder, new ObjectMapper());

ChromaVectorStore.builder(chromaApi, embeddingModel)
    .collectionName("spring-ai-rag-collection")
    .initializeSchema(true)
    .build();
```

### 相似性搜索

```java
SearchRequest searchRequest = SearchRequest.builder()
    .query("Spring AI 框架")
    .topK(3)
    .build();

List<Document> results = vectorStore.similaritySearch(searchRequest);
```

### 元数据过滤

```java
Filter.Expression filter = new FilterExpressionBuilder()
    .and(
        new FilterExpressionBuilder().in("category", "database"),
        new FilterExpressionBuilder().gte("year", 2023)
    )
    .build();

SearchRequest.builder()
    .query("数据存储")
    .filterExpression(filter)
    .build();
```

## 环境配置

```properties
# MiniMax API 密钥
MINIMAX_API_KEY2=your-api-key

# Chroma 服务地址（代码中配置）
# http://localhost:8000
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+
- Chroma 向量数据库
- MiniMax 嵌入模型

## 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-chroma</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-minimax</artifactId>
</dependency>
```

## 运行方式

```bash
# 1. 启动 Chroma
docker run -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0

# 2. 运行示例
cd spring-ai-rag-chroma-examples
mvn spring-boot:run
```

## Chroma 特点

1. **轻量级**：易于部署和集成
2. **开源免费**：可在本地部署
3. **元数据支持**：支持丰富的过滤条件
4. **云版本**：也提供云端托管服务

## 相关项目

| 项目 | 向量数据库 |
|------|------------|
| `spring-ai-rag-examples` | 无（基础概念） |
| `spring-ai-rag-redis-examples` | Redis Stack |