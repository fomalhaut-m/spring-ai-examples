# spring-ai-chat-model-examples

Spring AI 多模型聊天示例集合，涵盖各种聊天模型、嵌入模型和工具调用的实践。

## 项目简介

本项目演示了 Spring AI 框架对多种大语言模型的支持，包括：
- 主流云厂商模型（DeepSeek、MiniMax、OpenAI 等）
- 本地部署模型（Ollama）
- 以及嵌入模型和工具调用功能

## 核心示例

### 1. MiniMax 模型示例

#### MiniMaxChatExample

**功能特点：**
- 展示 MiniMax 模型的各种参数配置
- 支持流式和非流式响应
- 演示系统消息、温度参数、最大令牌数等高级功能

**演示场景：**

| 示例 | 说明 |
|------|------|
| 基本对话 | 简单问答 |
| 温度参数 | 控制生成随机性 (temperature) |
| 最大令牌数 | 限制响应长度 (maxTokens) |
| TopP采样 | 核采样策略 (topP) |
| 系统消息 | 设置助手角色和风格 |
| 流式响应 | 实时流式输出 (stream) |
| 存在惩罚 | 避免重复提及 (presencePenalty) |
| 频率惩罚 | 避免重复词句 (frequencyPenalty) |
| 停止序列 | 自定义生成停止条件 |

#### MiniMaxEmbeddingExample

**功能特点：**
- 文本向量化（Embedding）功能
- 批量嵌入多文本
- 余弦相似度计算
- 自定义模型选择

**嵌入模型：** `embo-01`

### 2. DeepSeek 模型示例

#### DeepSeekChatExample

**功能特点：**
- 实体提取（Entity）功能
- 返回结构化数据
- 支持多种生成参数组合

### 3. 工具调用示例

#### ToolExamples

**功能特点：**
- 注解式工具 `@Tool`
- 编程式工具 `MethodToolCallback`
- 函数式工具 `Function<T, R>`
- returnDirect 模式

**工具类：**
- `DateTimeTools` - 获取当前时间和日期
- `StaticDateTimeTools` - 静态工具方法

## 环境配置

```properties
# MiniMax API 密钥
MINIMAX_API_KEY2=your-minimax-api-key

# DeepSeek API 密钥
DEEPSEEK_API_KEY=your-deepseek-api-key
```

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.4
- Java 17+

## 支持的模型

| 模型 | 依赖 | 用途 |
|------|------|------|
| OpenAI | spring-ai-starter-model-openai | GPT-3.5/GPT-4 |
| 智谱 AI | spring-ai-starter-model-zhipuai | GLM-3/GLM-4 |
| Ollama | spring-ai-starter-model-ollama | 本地模型 |
| MiniMax | spring-ai-starter-model-minimax | abab 系列 |
| Mistral AI | spring-ai-starter-model-mistral-ai | Mistral 系列 |
| HuggingFace | spring-ai-starter-model-huggingface | HF 模型 |
| DeepSeek | spring-ai-starter-model-deepseek | DeepSeek 系列 |
| Anthropic | spring-ai-starter-model-anthropic | Claude 系列 |

## 运行方式

```bash
cd spring-ai-chat-model-examples
mvn compile
# 运行各个示例
```