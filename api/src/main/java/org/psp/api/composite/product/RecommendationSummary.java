package org.psp.api.composite.product;

import lombok.Data;

@Data
public class RecommendationSummary {
    private final int recommendationId;
    private final String author;
    private final int rate;
    private final String content;
}
