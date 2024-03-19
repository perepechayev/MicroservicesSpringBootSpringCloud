package org.psp.api.composite.product;

import lombok.Data;

@Data
public class ReviewSummary {
    private final int reviewId;
    private final String author;
    private final String subject;
    private final String content;
}
