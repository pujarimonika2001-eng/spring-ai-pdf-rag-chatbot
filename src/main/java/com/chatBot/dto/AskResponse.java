package com.chatBot.dto;

import java.util.List;

public record AskResponse(String answer, List<String> sources) {}