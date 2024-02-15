package org.psp.core.recommendation.service;

import com.mongodb.DuplicateKeyException;
import org.psp.api.core.recommendation.Recommendation;
import org.psp.api.core.recommendation.RecommendationService;
import org.psp.api.exceptions.InvalidInputException;
import org.psp.core.recommendation.persistence.RecommendationEntity;
import org.psp.core.recommendation.persistence.RecommendationRepository;
import org.psp.util.http.ServiceUtil;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);
        } catch(DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key, product Id: " + body.getProductId()
                    + ", Recommendation id: " + body.getRecommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendations: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteRecommendation(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}",
                productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
