package com.example.wiki.model;

/**
 * 知识库条目模型
 */
public record KnowledgeEntry(String id, String title, String content, String category) {}