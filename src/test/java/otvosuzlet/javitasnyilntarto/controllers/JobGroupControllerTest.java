package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;

@WebMvcTest(JobGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class JobGroupControllerTest {
    @Autowired
    private JobGroupService jobGroupService;    

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(jobGroupService);
    }

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        JWTService jwtService() {
            return Mockito.mock(JWTService.class);
        }

        @Bean
        JwtFilter jwtFilter() {
            return Mockito.mock(JwtFilter.class);
        }

        @Bean
        JobGroupService jobGroupService() {
            return Mockito.mock(JobGroupService.class);
        }                
    }
    @Test
    void changeToPickedUpShouldReturnOk() throws Exception {
        JobPickedUpDTO input = new JobPickedUpDTO();
        Integer id = 123;

        Mockito.doNothing().when(jobGroupService).setDoneJobsToPickedUp(Mockito.eq(id), Mockito.any(JobPickedUpDTO.class));

        mockMvc.perform(patch("/api/jobgroup/{id}/change-donejobs-to-pickedup", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk());
    }

    @Test
    void changeToPickedUpShouldHandleValidationException() throws Exception {
        JobPickedUpDTO input = new JobPickedUpDTO();
        Integer id = 123;

        Mockito.doThrow(new ValidationException("date","validation.incorrect.date")).when(jobGroupService).setDoneJobsToPickedUp(Mockito.anyInt(), Mockito.any(JobPickedUpDTO.class));

        mockMvc.perform(patch("/api/jobgroup/{id}/change-donejobs-to-pickedup", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void downloadReceiptShouldReturnPdf() throws Exception {
        Integer id = 123;
        byte[] pdfContent = "PDF content".getBytes();
        Mockito.when(jobGroupService.getReceipt(id)).thenReturn(pdfContent);

        mockMvc.perform(get("/api/jobgroup/{id}/get-receipt", id))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=sample.pdf"))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(content().bytes(pdfContent));
    }
}