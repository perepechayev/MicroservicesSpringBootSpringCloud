package org.psp.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "ProductComposite",
        description = "REST API for composite product information")
public interface ProductCompositeService {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/product-composite")
    Mono<Void> createProduct(@RequestBody ProductAggregate body);

    @Operation(
            summary = "Product composite",
            description = "Интегральный сервис"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Bad request")
            }
    )
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getProduct(@PathVariable("productId") int productId);

    @DeleteMapping("/product-composite/{productId}")
    Mono<Void> deleteProduct(@PathVariable("productId") int productId);
}
