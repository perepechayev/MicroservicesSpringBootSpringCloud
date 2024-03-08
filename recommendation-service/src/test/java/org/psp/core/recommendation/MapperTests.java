package org.psp.core.recommendation;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.psp.api.core.recommendation.Recommendation;
import org.psp.core.recommendation.persistence.RecommendationEntity;
import org.psp.core.recommendation.service.RecommendationMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapperTests {
    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "adr");

        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());

        Recommendation api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
    }

    @Test
    public void mapperListTests() {
        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "adr");
        List<Recommendation> apiList = Collections.singletonList(api);
        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());
        assertEquals(apiList.get(0).getProductId(), entityList.get(0).getProductId());
        assertEquals(apiList.get(0).getRecommendationId(), entityList.get(0).getRecommendationId());

        List<Recommendation> api2List = mapper.entityListToApiList(entityList);

        assertEquals(api2List.size(), entityList.size());
        assertEquals(apiList.get(0).getProductId(), api2List.get(0).getProductId());
        assertEquals(apiList.get(0).getRecommendationId(), api2List.get(0).getRecommendationId());
    }
}
