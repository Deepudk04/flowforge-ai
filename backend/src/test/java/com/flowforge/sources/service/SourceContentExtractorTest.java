package com.flowforge.sources.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowforge.common.BadRequestException;
import com.flowforge.config.FlowForgeProperties;
import com.flowforge.sources.model.SourceType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.junit.jupiter.api.Test;

class SourceContentExtractorTest {
    @Test
    void extractsAndNormalizesUtf8Markdown() {
        var extractor = extractor(1024, 10);

        var result = extractor.extract(
                "guide.md",
                "application/octet-stream",
                "\uFEFF# Guide\r\n\r\nSynthetic content  ".getBytes(StandardCharsets.UTF_8)
        );

        assertThat(result.sourceType()).isEqualTo(SourceType.MARKDOWN);
        assertThat(result.contentType()).isEqualTo("text/markdown");
        assertThat(result.content()).isEqualTo("# Guide\n\nSynthetic content");
    }

    @Test
    void rejectsUnsupportedOversizedAndEmptySources() {
        var extractor = extractor(4, 10);

        assertThatThrownBy(() -> extractor.extract("guide.docx", null, "text".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("supported");
        assertThatThrownBy(() -> extractor.extract("guide.txt", null, "12345".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("size limit");
        assertThatThrownBy(() -> extractor.extract("guide.txt", null, new byte[0]))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsMalformedAndTextlessPdfs() {
        var extractor = extractor(10_000, 10);

        assertThatThrownBy(() -> extractor.extract(
                "broken.pdf",
                "application/pdf",
                "not a PDF".getBytes(StandardCharsets.UTF_8)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("malformed");
        assertThatThrownBy(() -> extractor.extract("blank.pdf", "application/pdf", pdf(1, false)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no extractable text");
    }

    @Test
    void enforcesPdfPageLimitAndExtractsPdfText() {
        var extractor = extractor(20_000, 1);

        assertThatThrownBy(() -> extractor.extract("long.pdf", "application/pdf", pdf(2, true)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("page limit");

        var result = extractor.extract("brief.pdf", "application/pdf", pdf(1, true));
        assertThat(result.sourceType()).isEqualTo(SourceType.PDF);
        assertThat(result.content()).contains("Synthetic source");
    }

    private static SourceContentExtractor extractor(long maxFileSize, int maxPdfPages) {
        var properties = new FlowForgeProperties(
                "/api/v1",
                "http://localhost:5173",
                new FlowForgeProperties.Security(false, "flowforge-api"),
                new FlowForgeProperties.AiService("local", "http://localhost:8000", 1000, 1, 0),
                new FlowForgeProperties.Sources(Path.of(".flowforge/storage"), maxFileSize, maxPdfPages)
        );
        return new SourceContentExtractor(properties);
    }

    private static byte[] pdf(int pages, boolean includeText) {
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            for (int index = 0; index < pages; index++) {
                var page = new PDPage();
                document.addPage(page);
                if (includeText) {
                    try (var stream = new PDPageContentStream(document, page)) {
                        stream.beginText();
                        stream.setFont(new PDType1Font(FontName.HELVETICA), 12);
                        stream.newLineAtOffset(72, 720);
                        stream.showText("Synthetic source page " + (index + 1));
                        stream.endText();
                    }
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new AssertionError("Unable to create test PDF", exception);
        }
    }
}
