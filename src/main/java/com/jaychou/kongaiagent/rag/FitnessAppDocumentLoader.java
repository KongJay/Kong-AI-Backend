package com.jaychou.kongaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 文档加载器组件，用于从类路径加载并解析 Markdown 文件为 Spring AI 的 Document 对象
 */
@Component
@Slf4j
class FitnessAppDocumentLoader {

    // Spring 资源模式解析器，用于获取匹配指定模式的资源文件
    private final ResourcePatternResolver resourcePatternResolver;

    // 通过构造函数注入 ResourcePatternResolver 依赖
    FitnessAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载所有匹配的 Markdown 文件并转换为 Document 列表
     * @return 包含所有解析后文档的列表
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 使用通配符模式加载 classpath:document/ 目录下所有 .md 文件
            // 实际使用时可以根据需求修改路径模式（例如：加载多级目录下的文件）
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                log.debug("正在处理 Markdown 文件: {}", fileName);

                // 配置 Markdown 解析参数：
                // - 将水平分割线（---）视为文档分隔符（创建新 Document）
                // - 排除代码块和引用块内容
                // - 添加文件名作为元数据
                assert fileName != null;
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)  // 用水平线分割文档
                        .withIncludeCodeBlock(false)             // 不包含代码块
                        .withIncludeBlockquote(false)           // 不包含引用块
                        .withAdditionalMetadata("filename", fileName) // 添加文件名元数据
                        .build();

                // 创建 Markdown 阅读器实例并解析文档
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            // 捕获并记录文件加载异常（例如：路径不存在或文件读取错误）
            log.error("Markdown 文档加载失败", e);
            // 生产环境中可考虑添加重试机制或抛出业务异常
        }
        return allDocuments;
    }
}