package org.psp.core.review.service;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.psp.api.core.review.Review;
import org.psp.core.review.persistence.ReviewEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapperTests {
    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);

        Review api = new Review(1, 2, "a", "s", "C", "adr");
        ReviewEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());

        Review api2 = mapper.entityToApi(entity);
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getSubject(), api2.getSubject());
        assertEquals(api.getContent(), api2.getContent());
    }
}
