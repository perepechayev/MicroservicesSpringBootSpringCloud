package org.psp.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psp.api.core.event.Event;
import org.psp.api.core.product.Product;
import org.psp.api.exceptions.InvalidInputException;
import org.psp.core.product.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
public class ProductServiceApplicationTests extends MongoDbTestBase {
    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductRepository repository;
    @Qualifier("messageProcessor")
    @Autowired
    private Consumer<Event<Integer, Product>> messageProcessor;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    public void getProductById() {
        int productId = 1;
        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long)repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long)repository.count().block());

        getAndVerifyProduct(productId, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }

//    @Test
    public void duplicateError() {
        int productId = 1;
        assertNull(repository.findByProductId(productId).block());
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateProductEvent(productId),
                "Expected a InvalidInputException here!"
        );
        assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
    }

    @Test
    public void deleteProduct() {
        int productId = 1;
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());
        sendDeleteProductEvent(productId);
    }

    @Test
    public void getProductNotFound() {
        int productNotFound = 13;
        getAndVerifyProduct(productNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productNotFound);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateProductEvent(int productId) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        Event<Integer, Product> event = new Event<>(Event.Type.CREATE, productId, product);
        messageProcessor.accept(event);
    }

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event<>(Event.Type.DELETE, productId, null);
        messageProcessor.accept(event);
    }
}
