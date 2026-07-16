package com.flowforge.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.common.CorrelationIdFilter;
import com.flowforge.config.FlowForgeProperties;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.client.RestClient;

class HttpAiServiceClientTest {
    @Test
    void generateDocumentForwardsCorrelationIdAndParsesResponse() throws IOException {
        var correlationId = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/documents/generate", exchange -> {
            correlationId.set(exchange.getRequestHeaders().getFirst(CorrelationIdFilter.HEADER_NAME));
            writeJson(exchange, 200, """
                    {
                      "content": "# SampleDocument\\n\\nGenerated content",
                      "model": "flowforge-local-mock",
                      "retrievedSourceIds": ["sample-document-guide"]
                    }
                    """);
        });
        server.start();

        try {
            MDC.put(CorrelationIdFilter.MDC_KEY, "test-correlation-id");
            var client = new HttpAiServiceClient(propertiesFor(server, 1), RestClient.builder());

            var result = client.generateDocument(new AiServiceClient.DocumentGenerationCommand(
                    "SampleDocument",
                    "document",
                    "DemoClient provided input.",
                    List.of("sample")
            ));

            assertThat(correlationId.get()).isEqualTo("test-correlation-id");
            assertThat(result.content()).contains("Generated content");
            assertThat(result.model()).isEqualTo("flowforge-local-mock");
            assertThat(result.retrievedSourceIds()).containsExactly("sample-document-guide");
        } finally {
            MDC.remove(CorrelationIdFilter.MDC_KEY);
            server.stop(0);
        }
    }

    @Test
    void generateWorkflowDiagramRetriesTransientFailure() throws IOException {
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/workflows/diagram", exchange -> {
            if (requests.incrementAndGet() == 1) {
                writeJson(exchange, 503, "{\"detail\":\"temporarily unavailable\"}");
                return;
            }
            writeJson(exchange, 200, """
                    {
                      "mermaid": "flowchart TD\\n    intake[Receive intake]",
                      "warnings": []
                    }
                    """);
        });
        server.start();

        try {
            var client = new HttpAiServiceClient(propertiesFor(server, 2), RestClient.builder());

            var result = client.generateWorkflowDiagram(new AiServiceClient.WorkflowDiagramCommand(
                    "SampleWorkflow",
                    List.of(new AiServiceClient.WorkflowStep("intake", "Receive intake", null))
            ));

            assertThat(requests.get()).isEqualTo(2);
            assertThat(result.mermaid()).contains("flowchart TD");
            assertThat(result.warnings()).isEmpty();
        } finally {
            server.stop(0);
        }
    }

    private FlowForgeProperties propertiesFor(HttpServer server, int retryAttempts) {
        return new FlowForgeProperties(
                "/api/v1",
                "http://localhost:5173",
                new FlowForgeProperties.Security(false, "flowforge-api"),
                new FlowForgeProperties.AiService(
                        "http",
                        "http://localhost:" + server.getAddress().getPort(),
                        1000,
                        retryAttempts,
                        0
                )
        );
    }

    private void writeJson(com.sun.net.httpserver.HttpExchange exchange, int status, String json) throws IOException {
        var bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }
}
