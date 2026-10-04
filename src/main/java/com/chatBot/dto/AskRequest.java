package com.chatBot.dto;

import jakarta.validation.constraints.NotBlank;

public record AskRequest(@NotBlank String docId, @NotBlank String question) {}