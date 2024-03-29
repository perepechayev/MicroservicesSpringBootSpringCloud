package org.psp.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psp.api.core.recommendation.Recommendation;
import org.psp.core.recommendation.persistence.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceApplicationTests extends MongoDbTestBase {
    @Autowired
    WebTestClient client;
    @Autowired
    RecommendationRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getRecomendationsByProductId() {
        int productId = 1;
        postAndVerifyRecommendation(productId, 1, HttpStatus.OK);
        postAndVerifyRecommendation(productId, 2, HttpStatus.OK);
        postAndVerifyRecommendation(productId, 3, HttpStatus.OK);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

//    @Test
    public void duplicateError() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId);

        assertEquals(1, repository.count());

        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id: 1");
        assertEquals(1, repository.count());
    }

    @Test
    public void deleteRecommendations() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendationsByProductId(productId, HttpStatus.OK);
        assertEquals(0, repository.findByProductId(productId).size());
    }

//    @Test
    public void getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductId("/", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }

//    @Test
    public void getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductId("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getRecommendationsNotFound() {
        getAndVerifyRecommendationsByProductId(113, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

//    @Test
    public void getRecommendationsInvalidParameterNegativeValue() {
        int productId = -1;

        getAndVerifyRecommendationsByProductId(productId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productId);
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

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId,
                "Author + " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        return client.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/recommendation/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
