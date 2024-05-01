package org.psp.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewService {

    Mono<Review> createReview(@RequestBody Review body);

    @GetMapping(
            value = "/review/{productId}",
            produces = "application/json")
    Flux<Review> getReviews(@PathVariable("productId") int productId);

    Mono<Void> deleteReview(@PathVariable("productId") int productId);
}
