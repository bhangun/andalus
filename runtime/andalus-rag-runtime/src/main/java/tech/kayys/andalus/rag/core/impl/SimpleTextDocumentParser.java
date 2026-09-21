package tech.kayys.andalus.rag.core.impl;

import tech.kayys.andalus.rag.core.RagDocument;
import tech.kayys.andalus.rag.core.spi.DocumentParser;

import java.util.Map;

public class SimpleTextDocumentParser implements DocumentParser {

    @Override
    public RagDocument parse(String source, String rawContent, Map<String, Object> metadata) {
        String normalized = rawContent == null ? "" : rawContent.trim();
        return RagDocument.of(source, normalized, metadata);
    }
}
