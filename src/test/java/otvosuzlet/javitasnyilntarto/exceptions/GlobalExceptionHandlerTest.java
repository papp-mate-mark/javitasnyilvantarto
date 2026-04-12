package otvosuzlet.javitasnyilntarto.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldHandleBindException() throws Exception {
        mockMvc.perform(get("/test/bind"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorKey").value("error.validation.failed"))
                .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.required"))
                .andExpect(jsonPath("$.path").value("/test/bind"));
    }

    @Test
    void shouldHandleConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorKey").value("error.validation.failed"))
                .andExpect(jsonPath("$.fieldErrors[0].fieldName").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].messageKey").value("validation.required"))
                .andExpect(jsonPath("$.path").value("/test/constraint-violation"));
    }

    @Test
    void shouldHandleHandlerMethodValidationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HandlerMethodValidationException exception = Mockito.mock(HandlerMethodValidationException.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRequestURI()).thenReturn("/test/method-validation");
        Mockito.doReturn(List.of(
                new DefaultMessageSourceResolvable(new String[]{"validation.method.min"}, "validation.method.min")
        )).when(exception).getAllErrors();

        ResponseEntity<?> response = handler.handleHandlerMethodValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Object body = response.getBody();
        assertNotNull(body);
        assertEquals("CONSTRAINT_VIOLATION_ERROR", ((otvosuzlet.javitasnyilntarto.model.MethodViolationErrorResponse) body).getErrorType().toString());
        assertEquals("error.method.violation", ((otvosuzlet.javitasnyilntarto.model.MethodViolationErrorResponse) body).getErrorKey());
        assertEquals("/test/method-validation", ((otvosuzlet.javitasnyilntarto.model.MethodViolationErrorResponse) body).getPath());
        assertEquals("validation.method.min", ((otvosuzlet.javitasnyilntarto.model.MethodViolationErrorResponse) body).getViolationErrors().get(0));
    }

    @Test
    void shouldHandleRuntimeExceptionWithCode() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorType").value("GENERAL_ERROR"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorKey").value("error.runtime"))
                .andExpect(jsonPath("$.errorCode").value("409 CONFLICT"))
                .andExpect(jsonPath("$.message").value("Runtime failure"))
                .andExpect(jsonPath("$.path").value("/test/runtime"));
    }

    @Test
    void shouldHandleInvalidCredentialsException() throws Exception {
        mockMvc.perform(get("/test/invalid-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType").value("GENERAL_ERROR"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorKey").value("error.invalid.credentials"))
                .andExpect(jsonPath("$.errorCode").value("401 UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials provided"))
                .andExpect(jsonPath("$.path").value("/test/invalid-credentials"));
    }

    @Test
    void shouldHandleAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorType").value("GENERAL_ERROR"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorKey").value("error.access.denied"))
                .andExpect(jsonPath("$.errorCode").value("403 FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/test/access-denied"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/bind")
        void throwBindException() throws BindException {
            BindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(), "request");
            bindingResult.rejectValue("username", "", "validation.required");
            throw new BindException(bindingResult);
        }

        @GetMapping("/test/constraint-violation")
        void throwConstraintViolationException() {
            ConstraintBean bean = new ConstraintBean();
            Set<ConstraintViolation<ConstraintBean>> violations = Validation
                    .buildDefaultValidatorFactory()
                    .getValidator()
                    .validate(bean);
            throw new ConstraintViolationException(Set.copyOf(violations));
        }

        @GetMapping("/test/runtime")
        void throwRuntimeExceptionWithCode() {
            throw new RuntimeExceptionWithCode("Runtime failure", "error.runtime", HttpStatus.CONFLICT);
        }

        @GetMapping("/test/invalid-credentials")
        void throwInvalidCredentialsException() {
            throw new InvalidCredentialsException("bad credentials");
        }

        @GetMapping("/test/access-denied")
        void throwAccessDeniedException() {
            throw new AccessDeniedException("denied");
        }

        static class DummyRequest {
            private String username;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }

        static class ConstraintBean {
            @NotBlank(message = "validation.required")
            private String username;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }
    }
}
