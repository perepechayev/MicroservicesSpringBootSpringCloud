package org.psp.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "application/json"
    )
    Review createReview(@RequestBody Review body);

    @GetMapping(
            value = "/review/{productId}",
            produces = "application/json")
    List<Review> getReviews(@PathVariable("productId") int productId);

    @DeleteMapping("/review")
    void deleteReview(@RequestParam(value = "productId", required = true) int productId);
}
