package com.flowforge.integration;

import com.flowforge.common.CorrelationIdFilter;
import com.flowforge.config.FlowForgeProperties;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(prefix = "flowforge.ai-service", name = "mode", havingValue = "http", matchIfMissing = true)
public class HttpAiServiceClient implements AiServiceClient {
    private final RestClient restClient;
    private final int retryAttempts;
    private final long retryBackoffMs;

    public HttpAiServiceClient(FlowForgeProperties properties, RestClient.Builder restClientBuilder) {
        var aiService = properties.aiService();
        var requestFactory = new SimpleClientHttpRequestFactory();
        var timeout = Duration.ofMillis(aiService.timeoutMs());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = restClientBuilder.clone()
                .baseUrl(aiService.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.retryAttempts = Math.max(1, aiService.retryAttempts());
        this.retryBackoffMs = Math.max(0, aiService.retryBackoffMs());
    }

    @Override
    public DocumentGenerationResult generateDocument(DocumentGenerationCommand command) {
        return executeWithRetry("document generation", () -> {
            var request = new DocumentGenerationRequest(
                    command.title(),
                    command.documentType(),
                    command.inputContext(),
                    command.tags() == null ? List.of() : command.tags()
            );
            var response = post("/v1/documents/generate", request, DocumentGenerationResponse.class);
            if (response == null || response.content() == null || response.content().isBlank()) {
                throw new AiServiceException(
                        HttpStatus.BAD_GATEWAY,
                        "AI_SERVICE_INVALID_RESPONSE",
                        "AI service returned an empty document generation response"
                );
            }
            return new DocumentGenerationResult(
                    response.content(),
                    response.model(),
                    response.retrievedSourceIds() == null ? List.of() : response.retrievedSourceIds()
            );
        });
    }

    @Override
    public WorkflowDiagramResult generateWorkflowDiagram(WorkflowDiagramCommand command) {
        return executeWithRetry("workflow diagram generation", () -> {
            var steps = command.steps().stream()
                    .map(step -> new WorkflowStepRequest(step.id(), step.label(), step.nextStepId()))
                    .toList();
            var response = post(
                    "/v1/workflows/diagram",
                    new WorkflowDiagramRequest(command.title(), steps),
                    WorkflowDiagramResponse.class
            );
            if (response == null || response.mermaid() == null || response.mermaid().isBlank()) {
                throw new AiServiceException(
                        HttpStatus.BAD_GATEWAY,
                        "AI_SERVICE_INVALID_RESPONSE",
                        "AI service returned an empty workflow diagram response"
                );
            }
            return new WorkflowDiagramResult(
                    response.mermaid(),
                    response.warnings() == null ? List.of() : response.warnings()
            );
        });
    }

    private <T> T post(String path, Object request, Class<T> responseType) {
        var spec = restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request);
        var correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            spec.header(CorrelationIdFilter.HEADER_NAME, correlationId);
        }
        return spec.retrieve().body(responseType);
    }

    private <T> T executeWithRetry(String operation, Supplier<T> supplier) {
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (RestClientResponseException exception) {
                if (!isRetryable(exception.getStatusCode()) || attempt == retryAttempts) {
                    throw mapResponseException(operation, exception);
                }
            } catch (ResourceAccessException exception) {
                if (attempt == retryAttempts) {
                    throw new AiServiceException(
                            HttpStatus.GATEWAY_TIMEOUT,
                            "AI_SERVICE_TIMEOUT",
                            "AI service timed out during " + operation,
                            exception
                    );
                }
            } catch (AiServiceException exception) {
                throw exception;
            } catch (RestClientException exception) {
                if (attempt == retryAttempts) {
                    throw new AiServiceException(
                            HttpStatus.BAD_GATEWAY,
                            "AI_SERVICE_UNAVAILABLE",
                            "AI service failed during " + operation,
                            exception
                    );
                }
            }
            sleepBeforeRetry();
        }
        throw new AiServiceException(
                HttpStatus.BAD_GATEWAY,
                "AI_SERVICE_UNAVAILABLE",
                "AI service failed during " + operation
        );
    }

    private boolean isRetryable(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError() || statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private AiServiceException mapResponseException(String operation, RestClientResponseException exception) {
        var status = exception.getStatusCode();
        if (status.value() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return new AiServiceException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "AI_SERVICE_TIMEOUT",
                    "AI service timed out during " + operation,
                    exception
            );
        }
        return new AiServiceException(
                HttpStatus.BAD_GATEWAY,
                "AI_SERVICE_UNAVAILABLE",
                "AI service returned HTTP " + status.value() + " during " + operation,
                exception
        );
    }

    private void sleepBeforeRetry() {
        if (retryBackoffMs == 0) {
            return;
        }
        try {
            Thread.sleep(retryBackoffMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "AI_SERVICE_RETRY_INTERRUPTED",
                    "Interrupted while retrying AI service request",
                    exception
            );
        }
    }

    private record DocumentGenerationRequest(
            String title,
            String documentType,
            String inputContext,
            List<String> tags
    ) {
    }

    private record DocumentGenerationResponse(
            String content,
            String model,
            List<String> retrievedSourceIds
    ) {
    }

    private record WorkflowDiagramRequest(
            String title,
            List<WorkflowStepRequest> steps
    ) {
    }

    private record WorkflowStepRequest(
            String id,
            String label,
            String nextStepId
    ) {
    }

    private record WorkflowDiagramResponse(
            String mermaid,
            List<String> warnings
    ) {
    }
}
