package org.psp.api.core.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {
    Mono<Recommendation> createRecommendation(Recommendation body);

    @GetMapping(
            value = "/recommendation/{productId}",
            produces = "application/json")
    Flux<Recommendation> getRecommendations(@PathVariable("productId") int productId);

    Mono<Void> deleteRecommendations( int productId);
}
