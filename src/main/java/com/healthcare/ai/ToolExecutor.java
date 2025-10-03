package com.healthcare.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;

public interface ToolExecutor {
    String execute(String functionName, Map<String, Object> arguments) throws JsonProcessingException;
    boolean supports(String functionName);
    List<String> getSupportedFunctions();
}