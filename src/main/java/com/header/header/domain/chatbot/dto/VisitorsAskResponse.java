package com.header.header.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VisitorsAskResponse(String answer, @JsonProperty("session_id") String sessionId) {}