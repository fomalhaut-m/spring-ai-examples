# spring-ai-rag-examples

Spring AI RAG (Retrieval-Augmented Generation) 基础示例，演示检索增强生成的核心流程。

## 项目简介

RAG 是一种将信息检索与大语言模型生成相结合的技术，能够让 AI 基于知识库中的最新信息回答问题，有效减少"幻觉"问题。

本项目展示了 RAG 的基础流程，虽然未接入实际向量数据库，但清晰演示了完整的 RAG 架构。

## RAG 流程概述

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  文档    │───►│  分割    │───►│  向量化  │───►│  存储    │
│  加载   │    │  Chunk   │    │ Embedding│    │ VectorDB │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                              │
                                              ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  回答   │◄───│  生成    │◄───│  检索    │◄───│  查询    │
│  Output │    │  LLM     │    │ Retrieval│    │  Query   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

## 核心示例

### RagExamples

**功能特点：**
- 演示 RAG 的六个核心步骤
- 使用 MiniMax 作为生成模型
- 模拟完整的检索增强生成流程

**演示步骤：**

| 步骤 | 说明 |
|------|------|
| 1. 文档加载 (Document Loading) | 创建 Document 对象 |
| 2. 文本分割 (Text Chunking) | 将长文档分割成块 |
| 3. 向量化 (Embedding) | 文本转向量（需要 EmbeddingModel） |
| 4. 存储 (Vector Store) | 存入向量数据库 |
| 5. 检索 (Retrieval) | 相似性搜索 |
| 6. 生成 (Generation) | LLM 生成回答 |

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
cd spring-ai-rag-examples
mvn compile
# 运行主类
java com.example.rag.RagExamples
```

## 相关项目

本项目的进阶版本，使用真实的向量数据库：

| 项目 | 向量数据库 | 说明 |
|------|------------|------|
| `spring-ai-rag-chroma-examples` | Chroma | 开源嵌入数据库 |
| `spring-ai-rag-redis-examples` | Redis | 支持向量搜索的 Redis Stack |

## RAG 优势

1. **最新信息**：可接入实时更新的知识库
2. **可溯源**：答案可追溯到具体文档
3. **减少幻觉**：基于检索内容生成，降低虚构概率
4. **成本效益**：无需微调即可使用新知识

## 扩展方向

如需进一步完善 RAG 功能，可参考：

- 添加文档加载器（PDF、Word、HTML 等）
- 实现更智能的文本分割策略
- 配置元数据过滤
- 添加重排序（Re-Ranker）模块