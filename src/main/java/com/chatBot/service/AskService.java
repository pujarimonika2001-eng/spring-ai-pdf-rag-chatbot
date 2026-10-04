package com.chatBot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.chatBot.dto.AskResponse;

@Service
public class AskService {

    private static final String NOT_FOUND = "I couldn't find this in the document.";

    private final EmbeddingModel embeddingModel;
    private final ChunkStore store;
    private final ChatClient chatClient;

    public AskService(EmbeddingModel embeddingModel, ChunkStore store, ChatClient.Builder builder) {
        this.embeddingModel = embeddingModel;
        this.store = store;
        this.chatClient = builder.build();
    }

    public AskResponse ask(String docId, String question) {
        float[] q = embeddingModel.embed(question);
        List<ChunkStore.Hit> hits = store.search(docId, q, 4, 0.3);

        if (hits.isEmpty()) {
            return new AskResponse(NOT_FOUND, List.of());
        }

        String context = hits.stream()
            .map(ChunkStore.Hit::content)
            .collect(Collectors.joining("\n---\n"));

        String answer = chatClient.prompt()
            .system("""
                You answer questions ONLY using the provided context.
                If the answer is not in the context, reply exactly: "I couldn't find this in the document."
                """)
            .user(u -> u.text("Context:\n{context}\n\nQuestion: {question}")
                        .param("context", context)
                        .param("question", question))
            .call()
            .content();

        List<String> sources = hits.stream()
            .map(h -> "Page " + (h.page() != null ? h.page() : "?"))
            .distinct()
            .toList();

        return new AskResponse(answer, sources);
    }
}