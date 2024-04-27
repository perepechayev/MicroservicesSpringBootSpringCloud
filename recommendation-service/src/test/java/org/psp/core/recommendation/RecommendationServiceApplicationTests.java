package org.psp.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psp.api.core.event.Event;
import org.psp.api.core.recommendation.Recommendation;
import org.psp.core.recommendation.persistence.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceApplicationTests extends MongoDbTestBase {
    @Autowired
    WebTestClient client;
    @Autowired
    RecommendationRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    public void getRecomendationsByProductId() {
        int productId = 1;
        sendCreateRecommendationEvent(productId, 1);
        sendCreateRecommendationEvent(productId, 2);
        sendCreateRecommendationEvent(productId, 3);

        assertEquals(3, repository.findByProductId(productId).count().block());

        getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    public void deleteRecommendations() {
        int productId = 1;
        int recommendationId = 1;

        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
        assertEquals(0, repository.findByProductId(productId).count().block());
    }

    @Test
    public void getRecommendationsNotFound() {
        getAndVerifyRecommendationsByProductId(113, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsByProductId("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/recommendation" + productIdQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateRecommendationEvent(int productId, int recommendationId) {
        Recommendation recommendation = new Recommendation(productId, recommendationId,
                "Author + " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        Event<Integer, Recommendation> event = new Event<>(Event.Type.CREATE, productId, recommendation);
        messageProcessor.accept(event);
    }

    private void sendDeleteRecommendationEvent(int productId) {
        Event<Integer, Recommendation> event = new Event<>(Event.Type.DELETE, productId, null);
        messageProcessor.accept(event);
    }
}
