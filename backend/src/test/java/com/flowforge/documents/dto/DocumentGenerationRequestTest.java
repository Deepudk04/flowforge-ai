package com.flowforge.documents.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentGenerationRequestTest {
    @Test
    void normalizesMissingTagsToEmptyList() {
        var request = new DocumentDtos.DocumentGenerationRequest(
                "Synthetic SOP",
                "sop",
                "Synthetic process context.",
                null
        );

        assertThat(request.tags()).isEmpty();
    }

    @Test
    void limitsTagCountAndRejectsBlankTags() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            var tooManyTags = new DocumentDtos.DocumentGenerationRequest(
                    "Synthetic SOP",
                    "sop",
                    "Synthetic process context.",
                    java.util.stream.IntStream.range(0, 21)
                            .mapToObj(index -> "tag-" + index)
                            .toList()
            );
            var blankTag = new DocumentDtos.DocumentGenerationRequest(
                    "Synthetic SOP",
                    "sop",
                    "Synthetic process context.",
                    List.of(" ")
            );

            assertThat(validator.validate(tooManyTags)).hasSize(1);
            assertThat(validator.validate(blankTag)).hasSize(1);
        }
    }
}
