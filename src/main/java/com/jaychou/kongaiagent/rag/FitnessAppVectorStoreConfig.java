package com.jaychou.kongaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration  // 标识这是一个 Spring 配置类，用于定义 Bean
public class FitnessAppVectorStoreConfig {

    // 注入自定义的文档加载器 Bean
    @Resource
    private FitnessAppDocumentLoader fitnessAppDocumentLoader;

    /**
     * 创建并配置向量存储(VectorStore) Bean
     * @param dashscopeEmbeddingModel 自动注入的嵌入模型(用于文本向量化)
     * @return 配置好的 VectorStore 实例
     */
    @Bean
    VectorStore fitnessAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 1. 使用嵌入模型构建简单的向量存储
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        // 2. 加载 Markdown 文档
        List<Document> documents = fitnessAppDocumentLoader.loadMarkdowns();

        // 3. 将文档添加到向量存储中（自动生成向量嵌入）
        simpleVectorStore.add(documents);

        return simpleVectorStore;
    }
}
