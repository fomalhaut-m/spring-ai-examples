package com.example.wiki.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.example.wiki.model.KnowledgeEntry;

/**
 * 知识库服务
 */
@Service
public class WikiService {

    private static final Logger log = LoggerFactory.getLogger(WikiService.class);
    
    // Chroma 向量存储（可选）
    @Nullable
    private final VectorStore vectorStore;
    
    // 内存存储（降级使用）
    private final Map<String, KnowledgeEntry> memoryStore = new ConcurrentHashMap<>();

    public WikiService(@Nullable VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        if (vectorStore == null) {
            log.info("Chroma 未启用，使用内存存储");
        } else {
            log.info("Chroma 已启用");
        }
    }

    public KnowledgeEntry addKnowledge(String title, String content, String category) {
        String id = "kb-" + System.currentTimeMillis();
        
        log.info("--- 添加知识 ---");
        
        // 存入内存
        KnowledgeEntry entry = new KnowledgeEntry(id, title, content, category);
        memoryStore.put(id, entry);
        
        // 如果 Chroma 可用，也存入向量库
        if (vectorStore != null) {
            Document doc = Document.builder()
                    .text(title + "\n\n" + content)
                    .metadata(Map.of("id", id, "title", title, "category", category, "content", content))
                    .build();
            vectorStore.add(List.of(doc));
        }
        
        return entry;
    }

    public List<KnowledgeEntry> searchKnowledge(String query) {
        // 空查询返回全部知识
        if (query == null || query.isEmpty() || "*".equals(query)) {
            return new java.util.ArrayList<>(memoryStore.values());
        }
        
        if (vectorStore != null) {
            return searchKnowledgeWithVector(query);
        }
        
        // 内存搜索
        String lowerQuery = query.toLowerCase();
        return memoryStore.values().stream()
                .filter(e -> e.title().toLowerCase().contains(lowerQuery) ||
                           e.content().toLowerCase().contains(lowerQuery))
                .toList();
    }

    public List<KnowledgeEntry> searchKnowledgeWithVector(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }

        List<Document> docs = vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(10).build()
        );

        return docs.stream()
                .map(doc -> {
                    var m = doc.getMetadata();
                    return new KnowledgeEntry(
                        String.valueOf(m.getOrDefault("id", "")),
                        String.valueOf(m.getOrDefault("title", "")),
                        String.valueOf(m.getOrDefault("content", doc.getText())),
                        String.valueOf(m.getOrDefault("category", "general"))
                    );
                })
                .toList();
    }

    public boolean deleteKnowledge(String id) {
        memoryStore.remove(id);
        if (vectorStore != null) {
            vectorStore.delete(List.of(id));
        }
        return true;
    }
}