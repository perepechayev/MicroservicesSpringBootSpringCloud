package org.psp.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "ProductComposite",
        description = "REST API for composite product information")
public interface ProductCompositeService {

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
    ProductAggregate getProduct(@PathVariable("productId") int productId);
}
