package com.example.wiki.config;

import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Wiki 应用配置类
 * 
 * 【关键点】
 * 1. ChatMemory 使用 MongoDB 存储
 * 2. VectorStore 使用 Chroma 向量数据库（自动配置）
 */
@EnableAsync
public class WikiConfig {
    // Chroma VectorStore 等通过 yml 自动配置
}