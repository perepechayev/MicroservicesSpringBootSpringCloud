package org.psp.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(@RequestBody Product body);

    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Mono<Product> getProduct(@PathVariable("productId") int productId);

    Mono<Void> deleteProduct(@PathVariable("productId") int productId);
}
