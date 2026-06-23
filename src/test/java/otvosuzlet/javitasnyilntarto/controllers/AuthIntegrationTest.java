package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthIntegrationTest.TestController.class)
class AuthIntegrationTest {

    @TestConfiguration
    static class TestController {
        @RestController
        @RequestMapping("/api/auth")
        static class TestEndpoint {
            @GetMapping("/test")
            public ResponseEntity<String> test() {
                return ResponseEntity.ok("OK");
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private static final String TEST_USERNAME = "integration-test-user";
    private static final String TEST_PASSWORD = "testPassword123";

    @BeforeEach
    void setUp() {
        createUser(TEST_USERNAME);
    }

    private void createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(TEST_PASSWORD));
        userRepository.save(user);
        userRepository.flush();
    }

    @Test
    void loginWithValidCredentialsThenAccessTestEndpointShouldReturnOk() throws Exception {
        String accessToken = login().accessToken;

        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void accessTestEndpointWithInvalidTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshShouldReturnNewTokensAndInvalidateOldRefreshToken() throws Exception {
        TokenPair tokens = login();
        // Refresh with the current refresh token (text/plain — same as real frontend)
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(tokens.refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokens.refreshToken").isNotEmpty())
                .andReturn();

        String newAccessToken = extractToken(refreshResult, "accessToken");

        // New access token should work
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());

        // Old refresh token should be invalidated — second refresh fails
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(tokens.refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutShouldInvalidateAccessAndRefreshTokens() throws Exception {
        TokenPair tokens = login();

        // Access token works before logout
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isOk());

        // Logout (requires auth)
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(tokens.refreshToken))
                .andExpect(status().isOk());

        // Access token should be rejected after logout
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isUnauthorized());

        // Refresh token should be rejected after logout
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(tokens.refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutOthersShouldInvalidateAllOtherSessions() throws Exception {
        int sessionCount = 3;
        List<TokenPair> sessions = new ArrayList<>();

        for (int i = 0; i < sessionCount; i++) {
            sessions.add(login());
            // Brief pause so each login produces a different refresh token (different iat)
            if (i < sessionCount - 1) {
                Thread.sleep(1100);
            }
        }

        // Pick the first session as the "keeper"
        TokenPair keeper = sessions.get(0);

        // Call logout-others with the keeper's access token
        mockMvc.perform(post("/api/auth/logout-others")
                        .header("Authorization", "Bearer " + keeper.accessToken)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(keeper.refreshToken))
                .andExpect(status().isOk());


        // All other sessions' access tokens should be rejected
        for (int i = 0; i < sessionCount; i++) {
            TokenPair other = sessions.get(i);
            mockMvc.perform(get("/api/auth/test")
                            .header("Authorization", "Bearer " + other.accessToken))
                    .andExpect(status().isUnauthorized());
        }

        // All other sessions' refresh tokens should be rejected
        for (int i = 1; i < sessionCount; i++) {
            TokenPair other = sessions.get(i);
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(other.refreshToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    // --- helpers ---

    private record TokenPair(String accessToken, String refreshToken) {}

    private TokenPair login() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString()).get("tokens");
        Thread.sleep(1000); // Ensure that subsequent logins have different issued-at times for refresh tokens
        return new TokenPair(
                root.get("accessToken").asText(),
                root.get("refreshToken").asText()
        );
    }

    private String extractToken(MvcResult result, String field) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("tokens")
                .get(field)
                .asText();
    }
}
