package org.psp.composite.product.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.psp.api.core.event.Event;
import org.psp.api.core.product.Product;
import org.psp.api.core.product.ProductService;
import org.psp.api.core.recommendation.Recommendation;
import org.psp.api.core.recommendation.RecommendationService;
import org.psp.api.core.review.Review;
import org.psp.api.core.review.ReviewService;
import org.psp.api.exceptions.InvalidInputException;
import org.psp.api.exceptions.NotFoundException;
import org.psp.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static java.util.logging.Level.FINE;
import static org.psp.api.core.event.Event.Type.CREATE;
import static org.psp.api.core.event.Event.Type.DELETE;
import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    public ProductCompositeIntegration(@Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
                                       WebClient.Builder webClient,
                                       ObjectMapper mapper,
                                       StreamBridge streamBridge) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteReview(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    public Mono<Health> getProductHealth() {
        return getHealth(PRODUCT_SERVICE_URL);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(RECOMMENDATION_SERVICE_URL);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(REVIEW_SERVICE_URL);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.info("------> Will call actuator health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> {
                            System.out.println("++++++++++++++++++++HEALTH: " + ex);
                            return Mono.just(new Health.Builder().down().build());
                        }
                )
                .log(LOG.getName(), FINE);
    }

    public Mono<Product> getProduct(int productId) {

        String url = PRODUCT_SERVICE_URL + "/product/" + productId;
        LOG.debug("Will call getProduct API on URL: {}", url);
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (JsonProcessingException e) {
            return ex.getMessage();
        }
    }

    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL + "/recommendation/" + productId;
        LOG.debug("Will call getRecommendations API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a message {} to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    public Flux<Review> getReviews(int productId) {
        String url = REVIEW_SERVICE_URL + "/review/" + productId;
        LOG.debug("Will call getReviews API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            return ex;
        }
        WebClientResponseException wcre = (WebClientResponseException)ex;
        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                throw new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                throw new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                throw wcre;
        }
    }
}
