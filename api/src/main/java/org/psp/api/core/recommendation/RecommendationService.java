package org.psp.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {
    @PostMapping(
            value = "/recommendation",
            consumes = "application/json",
            produces = "application/json"
    )
    Recommendation createRecommendation(@RequestBody Recommendation body);

    @GetMapping(
            value = "/recommendation/{productId}",
            produces = "application/json")
    List<Recommendation> getRecommendations(@PathVariable("productId") int productId);

    @DeleteMapping(value = "/recommendation/{productId}")
    void deleteRecommendation(@PathVariable("productId") int productId);
}
