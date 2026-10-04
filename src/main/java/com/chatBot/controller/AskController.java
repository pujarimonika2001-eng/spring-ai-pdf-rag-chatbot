package com.chatBot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatBot.dto.AskRequest;
import com.chatBot.dto.AskResponse;
import com.chatBot.service.AskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AskController {

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest req) {
        return askService.ask(req.docId(), req.question());
    }
}