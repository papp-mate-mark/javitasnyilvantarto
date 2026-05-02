package otvosuzlet.javitasnyilntarto.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.dto.LoginResponse;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() {
        Mockito.reset(userService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
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
    void loginShouldUseForwardedIpAndReturnServiceResponse() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("john");
        request.setPassword("password");

        LoginResponse response = new LoginResponse();
        response.setName("John Doe");

        Mockito.when(userService.verify(Mockito.any(LoginRequestDto.class), Mockito.eq("203.0.113.10"), Mockito.eq("JUnit-Agent")))
            .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", "203.0.113.10, 10.10.10.10")
                .header("User-Agent", "JUnit-Agent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));

        ArgumentCaptor<LoginRequestDto> loginCaptor = ArgumentCaptor.forClass(LoginRequestDto.class);
        Mockito.verify(userService).verify(loginCaptor.capture(), Mockito.eq("203.0.113.10"), Mockito.eq("JUnit-Agent"));
        assertEquals("john", loginCaptor.getValue().getUsername());
    }

    @Test
    void refreshShouldReturnUnauthorizedWhenServiceThrows() throws Exception {
        Mockito.when(userService.refresh(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/api/auth/refresh")
                .header("X-Real-IP", "198.51.100.20")
                .header("User-Agent", "JUnit-Agent")
                .contentType("application/json")
                .content("\"refresh-token-value\""))
            .andExpect(status().isUnauthorized());

        Mockito.verify(userService).refresh("\"refresh-token-value\"", "198.51.100.20", "JUnit-Agent");
    }

    @Test
    void logoutShouldCallInvalidateRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType("application/json")
                .content("\"refresh-token-value\""))
            .andExpect(status().isOk());

        Mockito.verify(userService).invalidateRefreshTokenString("\"refresh-token-value\"");
    }

    @Test
    void logoutOthersShouldCallServiceWithAuthenticatedUsername() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("john", "n/a"));

        mockMvc.perform(post("/api/auth/logout-others")
                .contentType("application/json")
                .content("\"refresh-token-value\""))
            .andExpect(status().isOk());

        Mockito.verify(userService).invalidateOtherRefreshTokensByUsername("\"refresh-token-value\"", "john");
    }
}
