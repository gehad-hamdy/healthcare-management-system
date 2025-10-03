package com.healthcare.ai;

import com.healthcare.ai.impl.LocalAIService;
import com.healthcare.ai.impl.OpenAIService;
import com.healthcare.dto.ChatRequest;
import com.healthcare.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIChatOrchestrator {

    private final List<AIService> aiServices;
    private final LocalAIService localAIService;

    public ChatResponse processQuery(ChatRequest chatRequest) {
        log.info("Processing chat query: {}", chatRequest.getQuery());

        // Try services in priority order
        for (AIService service : getServicesByPriority()) {
            if (!service.isEnabled() || service.getHealth() != ServiceHealth.HEALTHY) {
                log.debug("Skipping service: {} - Enabled: {}, Health: {}",
                    service.getServiceName(), service.isEnabled(), service.getHealth());
                continue;
            }

            try {
                log.info("Attempting to use service: {}", service.getServiceName());
                ChatResponse response = service.processQuery(chatRequest);

                if (!response.isError()) {
                    log.info("Successfully processed with: {}", service.getServiceName());
                    return response;
                }

            } catch (Exception e) {
                log.warn("Service {} failed: {}", service.getServiceName(), e.getMessage());
            }
        }

        // All preferred services failed, use local fallback
        log.info("Using local fallback service");
        return localAIService.processQuery(chatRequest);
    }

    public Map<String, ServiceInfo> getServiceStatus() {
        return aiServices.stream()
            .collect(Collectors.toMap(
                AIService::getServiceName,
                service -> new ServiceInfo(
                    service.isEnabled(),
                    service.getHealth(),
                    service.getServiceName()
                )
            ));
    }

    private List<AIService> getServicesByPriority() {
        return aiServices.stream()
            .sorted(Comparator.comparing(this::getServicePriority).reversed())
            .collect(Collectors.toList());
    }

    private int getServicePriority(AIService service) {
        if (service instanceof OpenAIService) return 100; // Highest priority
        if (service instanceof LocalAIService) return 10;  // Fallback priority
        return 0; // Other services
    }

    public static class ServiceInfo {
        private final boolean enabled;
        private final ServiceHealth health;
        private final String name;

        public ServiceInfo(boolean enabled, ServiceHealth health, String name) {
            this.enabled = enabled;
            this.health = health;
            this.name = name;
        }

        public boolean isEnabled() { return enabled; }
        public ServiceHealth getHealth() { return health; }
        public String getName() { return name; }
    }
}