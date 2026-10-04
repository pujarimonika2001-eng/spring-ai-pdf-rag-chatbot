package com.chatBot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chatBot.service.IngestService;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final IngestService ingestService;

    public DocumentController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Please upload a valid PDF");
        }
        String docId = UUID.randomUUID().toString();
        int count = ingestService.ingest(file, docId);
        return Map.of("docId", docId, "chunks", count);
    }
}