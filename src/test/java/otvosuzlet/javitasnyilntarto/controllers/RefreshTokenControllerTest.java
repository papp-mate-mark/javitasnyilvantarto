package otvosuzlet.javitasnyilntarto.controllers;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import otvosuzlet.javitasnyilntarto.dto.RefreshTokenDataDto;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.RefreshTokenService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RefreshTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class RefreshTokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        Mockito.reset(refreshTokenService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RefreshTokenService refreshTokenService() {
            return Mockito.mock(RefreshTokenService.class);
        }

        @Bean
        JWTService jwtService() {
            return Mockito.mock(JWTService.class);
        }

        @Bean
        JwtFilter jwtFilter() {
            return Mockito.mock(JwtFilter.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
    }

    @Test
    void getActiveTokensShouldReturnActiveTokenData() throws Exception {
        List<RefreshTokenDataDto> data = List.of(
            new RefreshTokenDataDto("Browser A", "192.168.1.10", 1),
            new RefreshTokenDataDto("Browser B", "10.0.0.5", 2)
        );

        Mockito.when(refreshTokenService.getActiveTokens("john")).thenReturn(data);

        mockMvc.perform(get("/api/refresh-tokens/active")
                .principal(() -> "john"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userAgent").value("Browser A"))
            .andExpect(jsonPath("$[0].ipAddress").value("192.168.1.10"))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].userAgent").value("Browser B"))
            .andExpect(jsonPath("$[1].ipAddress").value("10.0.0.5"))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void invalidateTokenShouldReturn204AndCallService() throws Exception {
        mockMvc.perform(post("/api/refresh-tokens/3/invalidate")
                .principal(() -> "john"))
            .andExpect(status().isNoContent());

        Mockito.verify(refreshTokenService).invalidateToken(3, "john");
    }
}
