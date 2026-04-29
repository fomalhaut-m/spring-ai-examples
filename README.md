# spring-ai-examples


# Spring AI 完整学习路径总结（昨日整理版）
## 一、核心定位
Spring AI 是**Spring 生态官方AI框架**，对标 LangChain，适配 Java 后端技术栈，主打**大模型调用、RAG 知识库、Tool 工具调用、流式问答、多租户、可观测性**，适合企业级 AI Agent、智能客服、知识库问答、业务流程AI编排开发。

## 二、前置基础
1. Spring Boot 基础（自动配置、依赖注入、Web 开发）
2. Maven/Gradle 工程化、Spring 事务、配置中心
3. 大模型基础概念：Prompt、上下文、Token、向量库、Embedding、RAG
4. 基础向量库概念：向量嵌入、相似度检索、分片 Chunk

## 三、学习阶段路径
### 阶段1：环境搭建 & 入门调用
- 引入 Spring AI 官方依赖
- 对接主流大模型：OpenAI、通义千问、文心一言、本地 Ollama
- 基础功能：单轮问答、多轮上下文对话、参数调优（温度、最大Token）
- 掌握：application.yml 配置模型密钥、代理、超时、模型版本

### 阶段2：Prompt 工程
- 系统提示词、用户提示词分离
- Prompt Template 模板化
- 角色设定、约束规则、输出格式指定（JSON/结构化返回）
-  Few-Shot 示例提示、思维链 CoT

### 阶段3：RAG 检索增强生成（重点核心）
1. 文档处理：PDF/Word/TXT 加载、文本分片 Chunk 策略
2. Embedding 向量嵌入生成
3. 向量数据库集成：Redis Vector、Milvus、Chroma、PgVector
4. 检索策略：相似度匹配、阈值过滤、TopK 召回
5. 检索结果拼接进 Prompt，实现**私有知识库问答**
6. 优化：分片大小、重叠度、重排序 Rerank

### 阶段4：Tool 工具调用 & 函数调用
- Spring AI 声明式 Tool 定义
- 大模型自动识别并调用自定义业务工具
- 实战：查数据库、调用第三方接口、订单查询、天气查询等业务函数
- 多工具链式调用、权限与参数校验

### 阶段5：Stream 流式响应
- SSE 流式输出、逐字返回对话
- 适配前端聊天界面打字机效果
- WebFlux 响应式整合、Reactor 基础使用

### 阶段6：AI Agent 开发
- 智能体编排、任务拆解、自主规划
- 记忆机制：短期会话记忆、长期持久化记忆
- Agent 路由、决策、循环思考执行

### 阶段7：工程化 & 企业级落地
- 多租户隔离、会话隔离
- Token 统计与限流、计费估算
- 日志、链路追踪、可观测性
- 配置中心、动态模型切换、灰度接入
- 私有化部署、本地 Ollama 离线方案

## 四、关联技术栈必学
- Reactor 响应式编程（WebFlux、SSE 必备）
- 向量数据库基础（Milvus/Redis Vector）
- DDD 领域驱动设计（AI Agent 业务拆分）
- Spring Cloud 微服务整合（AI 能力下沉为微服务）
- LangChain 概念对照理解，便于选型对比

## 五、实战项目方向
1. 企业内部知识库问答系统
2. 智能客服 + 工单自动处理 Agent
3. 文档解析、总结、改写、代码生成工具
4. 个人私有化本地 AI 助手（Ollama + Spring AI）

需要我把这份路径整理成**可直接照着学的每日打卡计划表**，带知识点+实战任务吗？