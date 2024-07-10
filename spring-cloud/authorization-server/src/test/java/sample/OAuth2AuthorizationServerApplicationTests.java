package sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
@AutoConfigureMockMvc
public class OAuth2AuthorizationServerApplicationTests {
    @Autowired
    MockMvc mvc;

    @Test
    void requestTokenUsingClientCredentialsGrantType() throws Exception {
        String base64Credentials = Base64.getEncoder().encodeToString("writer:secret-writer".getBytes());
        mvc.perform(post("/oauth2/token")
                .param("grant_type", "client_credentials")
                .header("Authorization", "Basic " + base64Credentials))
                .andExpect(status().isOk());
    }

    @Test
    void requestOpenIdConfiguration() throws Exception {
        mvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk());
    }

    @Test
    void requestJwkSet() throws Exception {
        mvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk());
    }

    @Test
    void healthy() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
