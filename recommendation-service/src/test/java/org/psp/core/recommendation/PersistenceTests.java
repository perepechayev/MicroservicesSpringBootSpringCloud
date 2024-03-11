package org.psp.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psp.core.recommendation.persistence.RecommendationEntity;
import org.psp.core.recommendation.persistence.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {
    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    public void setUpDb() {
        repository.deleteAll();
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "C");
        savedEntity = repository.save(entity);
        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    public void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);
        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());
        assertEquals(entityList.size(), 1);
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
