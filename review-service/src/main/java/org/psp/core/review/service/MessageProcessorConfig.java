package org.psp.core.review.service;

import lombok.AllArgsConstructor;
import org.psp.api.core.event.Event;
import org.psp.api.core.review.Review;
import org.psp.api.core.review.ReviewService;
import org.psp.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ReviewService reviewService;

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());
            switch(event.getEventType()) {
                case CREATE -> {
                    Review review = event.getData();
                    LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                    reviewService.createReview(review).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    LOG.info("Delete reviews with product ID: {}", productId);
                    reviewService.deleteReview(productId).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected CREATE/DELETE";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOG.info("Message processing done!");
        };
    }
}