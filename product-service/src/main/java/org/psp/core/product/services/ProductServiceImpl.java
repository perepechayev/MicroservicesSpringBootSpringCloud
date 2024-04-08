package org.psp.core.product.services;

import com.mongodb.DuplicateKeyException;
import org.psp.api.core.product.Product;
import org.psp.api.core.product.ProductService;
import org.psp.api.exceptions.InvalidInputException;
import org.psp.api.exceptions.NotFoundException;
import org.psp.core.product.persistence.ProductEntity;
import org.psp.core.product.persistence.ProductRepository;
import org.psp.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

import static java.util.logging.Level.FINE;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, product id: " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));
        LOG.debug("createProduct: entity created for product: {}", body.getProductId());
        return newEntity;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get product info for id {}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(LOG.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid product id: " + productId);
        }
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .log(LOG.getName(), FINE)
                .map(e -> repository.delete(e))
                .flatMap(e -> e);
    }

    private Product setServiceAddress(Product product) {
        product.setServiceAddress(serviceUtil.getServiceAddress());
        return product;
    }
}
