package org.psp.core.recommendation;

import org.psp.core.recommendation.persistence.RecommendationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication(
        exclude = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class}
)
@ComponentScan("org.psp")
public class RecommendationServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

    @Autowired
    ReactiveMongoOperations mongoTemplate;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationServiceApplication.class, args);
        String mongoDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongoDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
        LOG.info("Connected to nongodb: " + mongoDbHost + ":" + mongoDbPort);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
                mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(RecommendationEntity.class);
        resolver.resolveIndexFor(RecommendationEntity.class).forEach(e -> indexOps.ensureIndex(e));
    }
}