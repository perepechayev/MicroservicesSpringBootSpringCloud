package org.psp.springcloud.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EurekaServerApplicationTests {
    @Autowired
    @Value("${app.eureka-username}")
    private String username;

    @Autowired
    @Value("${app.eureka-password}")
    private String password;

    private TestRestTemplate testRestTemplate;

    @Autowired
    public void setTestRestTemplate(TestRestTemplate restTemplate) {
        testRestTemplate = restTemplate.withBasicAuth(username, password);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void catalogLoads() {
        String expectedResponseBody = "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/eureka/apps", String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(expectedResponseBody, entity.getBody());
    }

    @Test
    void healthy() {
        String expectedResponseBody = "{\"status\":\"UP\"}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(expectedResponseBody, entity.getBody());
    }
}
