# spring-ai-rag-redis-examples

Spring AI + Redis 向量存储 RAG 示例，演示如何使用 Redis Stack 实现高效的向量搜索和检索增强生成。

## 项目简介

Redis Stack 是 Redis 的增强版本，提供了全文搜索和向量搜索能力。本项目展示了如何将 Spring AI 与 Redis 集成，利用 Redis 的高性能和丰富的数据结构实现 RAG 应用。

## 环境前置条件

**重要：必须使用 Redis Stack 版本**

普通 Redis 不支持向量搜索功能，必须使用 Redis Stack。

### 启动 Redis Stack

```bash
# 使用 Docker 启动 Redis Stack
docker run -p 6379:6379 redis/redis-stack
```

或者使用 RediSearch 模块：
```bash
docker run -p 6379:6379 redislabs/redismod
```

Redis 服务地址：`localhost:6379`

## 核心示例

### RedisRagExamples

**功能特点：**
- 使用 RedisVectorStore 存储和检索文档
- 结合 MiniMax 嵌入模型进行向量化
- 支持元数据过滤（tag 和 numeric 字段）
- 高性能的向量相似性搜索

**演示场景：**

| 示例 | 说明 |
|------|------|
| 添加文档 | 批量添加带元数据的文档到 Redis |
| 基本相似性搜索 | 基于语义找到最相关的文档 |
| 带阈值的搜索 | 只返回相似度超过阈值的文档 |
| 元数据过滤 | 按 category、year 等字段筛选 |
| 删除文档 | 根据 ID 删除文档 |

## 技术架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Spring    │────►│   MiniMax   │────►│    Redis    │
│   AI RAG    │     │  Embedding  │     │   Stack     │
└─────────────┘     └─────────────┘     └─────────────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │  向量索引   │
                                        │  HNSW/FTC  │
                                        └─────────────┘
```

## 核心代码

### VectorStore 配置

```java
RedisVectorStore.builder(new JedisPooled("localhost", 6379), embeddingModel)
    .indexName("spring-ai-rag-index")
    .prefix("rag:")
    .metadataFields(
        RedisVectorStore.MetadataField.tag("category"),
        RedisVectorStore.MetadataField.numeric("year"))
    .initializeSchema(true)
    .build();
```

### 元数据字段注册

```java
.metadataFields(
    RedisVectorStore.MetadataField.tag("category"),    // 标签字段，支持 IN 查询
    RedisVectorStore.MetadataField.numeric("year"))     // 数值字段，支持范围查询
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
```

## 环境配置

```properties
# MiniMax API 密钥
MINIMAX_API_KEY2=your-api-key

# Redis 连接（在代码中配置）
# host: localhost
# port: 6379
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+
- Redis Stack（支持向量搜索）
- Jedis 连接池
- MiniMax 嵌入模型

## 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-minimax</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 运行方式

```bash
# 1. 启动 Redis Stack
docker run -p 6379:6379 redis/redis-stack

# 2. 运行示例
cd spring-ai-rag-redis-examples
mvn compile
java com.example.redis.RedisRagExamples
```

## Redis 向量存储特点

1. **高性能**：Redis 内存数据库特性，查询速度极快
2. **企业级**：成熟的数据库解决方案，适合生产环境
3. **丰富数据结构**：支持 Hash、Set、Sorted Set 等多种数据结构
4. **高可用**：支持主从复制和集群部署
5. **向量索引**：支持 HNSW 和 FLAT 两种索引算法

## 相关项目

| 项目 | 向量数据库 |
|------|------------|
| `spring-ai-rag-examples` | 无（基础概念） |
| `spring-ai-rag-chroma-examples` | Chroma |