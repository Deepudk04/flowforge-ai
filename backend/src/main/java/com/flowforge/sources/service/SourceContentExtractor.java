package com.flowforge.sources.service;

import com.flowforge.common.BadRequestException;
import com.flowforge.config.FlowForgeProperties;
import com.flowforge.sources.model.SourceType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class SourceContentExtractor {
    private final FlowForgeProperties properties;

    public SourceContentExtractor(FlowForgeProperties properties) {
        this.properties = properties;
    }

    public ExtractedSource extract(String filename, String contentType, byte[] bytes) {
        if (bytes.length == 0) {
            throw new BadRequestException("Source file is empty");
        }
        if (bytes.length > properties.sources().maxFileSizeBytes()) {
            throw new BadRequestException("Source file exceeds the configured size limit");
        }

        SourceType sourceType = detectType(filename);
        String content = switch (sourceType) {
            case TEXT, MARKDOWN -> decodeUtf8(bytes);
            case PDF -> extractPdf(bytes);
        };
        content = normalize(content);
        if (content.isBlank()) {
            throw new BadRequestException("Source contains no extractable text");
        }
        return new ExtractedSource(sourceType, normalizedContentType(contentType, sourceType), content);
    }

    private SourceType detectType(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("Source filename is required");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".txt")) {
            return SourceType.TEXT;
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return SourceType.MARKDOWN;
        }
        if (lower.endsWith(".pdf")) {
            return SourceType.PDF;
        }
        throw new BadRequestException("Only .txt, .md, .markdown, and .pdf sources are supported");
    }

    private String decodeUtf8(byte[] bytes) {
        try {
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException exception) {
            throw new BadRequestException("Text and Markdown sources must use valid UTF-8");
        }
    }

    private String extractPdf(byte[] bytes) {
        try (var document = Loader.loadPDF(bytes)) {
            if (document.getNumberOfPages() > properties.sources().maxPdfPages()) {
                throw new BadRequestException("PDF exceeds the configured page limit");
            }
            return new PDFTextStripper().getText(document);
        } catch (BadRequestException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new BadRequestException("PDF is malformed or cannot be read");
        }
    }

    private String normalize(String content) {
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1);
        }
        return normalized.strip();
    }

    private String normalizedContentType(String contentType, SourceType sourceType) {
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equals(contentType)) {
            return contentType;
        }
        return switch (sourceType) {
            case TEXT -> "text/plain";
            case MARKDOWN -> "text/markdown";
            case PDF -> "application/pdf";
        };
    }

    public record ExtractedSource(SourceType sourceType, String contentType, String content) {
    }
}
