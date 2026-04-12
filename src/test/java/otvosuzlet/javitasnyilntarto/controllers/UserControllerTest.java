package otvosuzlet.javitasnyilntarto.controllers;

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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

import otvosuzlet.javitasnyilntarto.dto.RegisterUserRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetOwnPasswordRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetPasswordResponse;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
class UserControllerTest {

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
    void createShouldReturn400AndValidationErrorsOnInvalidData() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists())            
            .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
            .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.required"));
    }
    @Test
    void createShouldCallServiceMethod() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        ResetPasswordResponse expected = new ResetPasswordResponse("generatedPass123");
        Mockito.when(userService.registerUser(Mockito.any(RegisterUserRequest.class))).thenReturn(expected);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newPassword").value("generatedPass123"));
    }

    @Test
    void createShouldReturn400WhenServiceThrowsValidationException() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        ValidationException validationException = new ValidationException("username", "validation.username.already.exists");
        Mockito.when(userService.registerUser(Mockito.any(RegisterUserRequest.class)))
            .thenThrow(validationException);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
            .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.username.already.exists"));
    }

    @Test
    void updateShouldReturn400AndValidationErrorsOnInvalidData() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists())
            .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
            .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.required"));
    }

    @Test
    void updateShouldReturn204OnSuccess() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("updateduser");

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateShouldCallServiceMethodWithCorrectUserId() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("updateduser");
        Integer userId = 42;

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<RegisterUserRequest> requestCaptor = ArgumentCaptor.forClass(RegisterUserRequest.class);

        Mockito.verify(userService).updateUser(idCaptor.capture(), requestCaptor.capture());

        assert idCaptor.getValue() == userId;
        assert requestCaptor.getValue().getUsername().equals(request.getUsername());
    }

    @Test
    void updateShouldReturn400WhenServiceThrowsValidationException() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        ValidationException validationException = new ValidationException("username", "validation.username.already.exists");
        Mockito.doThrow(validationException)
            .when(userService).updateUser(Mockito.anyInt(), Mockito.any(RegisterUserRequest.class));

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
            .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.username.already.exists"));
    }

    @Test
    void resetOwnPasswordShouldReturn204OnSuccess() throws Exception {
        ResetOwnPasswordRequest request = new ResetOwnPasswordRequest("newStrongPassword123", "someRefreshToken");

        mockMvc.perform(put("/api/users/me/password")
                .principal(() -> "john")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        Mockito.verify(userService).resetOwnPassword(
            Mockito.eq("john"),
            Mockito.argThat(dto -> "newStrongPassword123".equals(dto.getNewPassword()))
        );
    }

    @Test
    void resetOwnPasswordShouldReturn400OnInvalidData() throws Exception {
        ResetOwnPasswordRequest request = new ResetOwnPasswordRequest("", "someRefreshToken");

        mockMvc.perform(put("/api/users/me/password")
                .principal(() -> "john")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists())
            .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("newPassword"));
    }
}
