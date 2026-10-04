package com.chatBot.service;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestService {

    private final EmbeddingModel embeddingModel;
    private final ChunkStore store;

    public IngestService(EmbeddingModel embeddingModel, ChunkStore store) {
        this.embeddingModel = embeddingModel;
        this.store = store;
    }

    public int ingest(MultipartFile file, String docId) throws IOException {
        Resource resource = new ByteArrayResource(file.getBytes());
        List<Document> pages = new PagePdfDocumentReader(resource).get();
        List<Document> chunks = new TokenTextSplitter(500, 350, 5, 10000, true).apply(pages);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                "No text found in this PDF. It may be a scanned/image PDF.");
        }

        List<String> texts = chunks.stream().map(Document::getText).toList();
        List<float[]> embeddings = embeddingModel.embed(texts);

        String fileName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();

        for (int i = 0; i < chunks.size(); i++) {
            Object p = chunks.get(i).getMetadata().get("page_number");
            Integer page = (p instanceof Number n) ? n.intValue() : null;
            store.save(docId, fileName, page, texts.get(i), embeddings.get(i));
        }
        return chunks.size();
    }
}