package org.psp.core.review;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication(
        exclude = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class}
)
@ComponentScan("org.psp")
public class ReviewServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);
    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    public ReviewServiceApplication(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler jdbcScheduler() {
        LOG.info("Creates a scheduler with a thread pool size = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.uri");
        LOG.info("Connected to MySqL: {}", mysqlUri);
    }
}