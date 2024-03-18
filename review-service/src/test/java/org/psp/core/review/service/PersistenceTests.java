package org.psp.core.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psp.core.review.persistence.ReviewEntity;
import org.psp.core.review.persistence.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends MySqlTestBase {
    @Autowired
    private ReviewRepository repository;
    private ReviewEntity savedEntity;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll();
        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        savedEntity = repository.save(entity);
        assertEqualsReview(entity, savedEntity);
    }

    @Test
    public void create() {
        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
        repository.save(newEntity);
        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    public void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);
        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByProductId() {
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());
        assertEquals(1, entityList.size());
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    public void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
            repository.save(entity);
        });
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
