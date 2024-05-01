package org.psp.core.review.service;

import lombok.AllArgsConstructor;
import org.psp.api.core.review.Review;
import org.psp.api.core.review.ReviewService;
import org.psp.api.exceptions.InvalidInputException;
import org.psp.core.review.persistence.ReviewEntity;
import org.psp.core.review.persistence.ReviewRepository;
import org.psp.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler jdbcScheduler;

    @Override
    public Mono<Review> createReview(Review body) {
        LOG.debug("createReview: created a new review entity: {}/{}", body.getProductId(), body.getReviewId());
        return Mono.fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get reviews for product with id={}", productId);

        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), Level.FINE)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReview(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        return Mono.fromRunnable(() -> internalDeleteReviews(productId));
    }

    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new InvalidInputException("Duplicate key, product ID: " + body.getProductId() +
                    ", review id: " + body.getReviewId());
        }
    }

    private List<Review> internalGetReviews(int produtId) {
        List<ReviewEntity> entityList = repository.findByProductId(produtId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
        LOG.debug("Response size: {}", list.size());
        return list;
    }

    private void internalDeleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for products with product id: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
