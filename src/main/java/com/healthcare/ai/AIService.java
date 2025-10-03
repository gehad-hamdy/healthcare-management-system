package com.healthcare.ai;

import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;

public interface AIService {
    ChatResponse processQuery(ChatRequest chatRequest);
    boolean isEnabled();
    String getServiceName();
    ServiceHealth getHealth();
}