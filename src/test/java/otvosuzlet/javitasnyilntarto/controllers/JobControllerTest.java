package otvosuzlet.javitasnyilntarto.controllers;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import otvosuzlet.javitasnyilntarto.dto.JobCompleteDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.dto.JobSearchJobDataDTO;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.JobService;
import otvosuzlet.javitasnyilntarto.service.PersonService;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class JobControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    private JobService jobService;

    @Autowired
    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        Mockito.reset(jobService);
        Mockito.reset(conversionService);
    }
  
    @TestConfiguration
    static class TestConfig {
        @Bean
        JobService jobService() {
            return Mockito.mock(JobService.class);
        }        
        
        @Bean
        PersonService personService() {
            return Mockito.mock(PersonService.class);
        }
        
        @Bean
        ConversionService conversionService() {
            return Mockito.mock(ConversionService.class);
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
    void fullSearchShouldReturnConvertedData() throws Exception {

        @SuppressWarnings("unchecked")
        Page<JobSearchJobDataDTO> mockPageResponse = Mockito.mock(Page.class);

        Mockito.when(jobService.searchForJob(Mockito.any(JobSearchDto.class), Mockito.any(Pageable.class))).thenReturn(mockPageResponse);
        Mockito.when(conversionService.convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class))).thenReturn(new PageResponse<JobSearchJobDataDTO>());
        mockMvc.perform(get("/api/jobs/fullsearch")
                .param("address", "test address")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<JobSearchDto> searchDtoCaptor = ArgumentCaptor.forClass(JobSearchDto.class);
        Mockito.verify(jobService).searchForJob(searchDtoCaptor.capture(), Mockito.any(Pageable.class));
        assertEquals("test address", searchDtoCaptor.getValue().getAddress());
    }

    @Test
    void changeToDoneShoulCallReturnNoContent() throws Exception {
        Integer id = 1;
        Mockito.doNothing().when(jobService).validateAndCompleteJob(Mockito.anyInt(), Mockito.any());

        mockMvc.perform(patch("/api/jobs/{id}/change-to-done", id)
                .content(objectMapper.writeValueAsString(new JobCompleteDTO()))
                .contentType("application/json"))
                .andExpect(status().isNoContent());
    }

    @Test
    void chageToDoneShouldHandleAndReThrowValidationErrors() throws Exception {
        Integer id = 1;
        JobCompleteDTO input = new JobCompleteDTO(-1, null, null, null);

        Mockito.doThrow(new ValidationException("date", "validation.invalid.date")).when(jobService).validateAndCompleteJob(Mockito.anyInt(), Mockito.any());

        mockMvc.perform(patch("/api/jobs/{id}/change-to-done", id)
                .content(objectMapper.writeValueAsString(input))
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'date')].messageKey").value("validation.invalid.date"));
        
    }

    @Test
    void changeToPickedUpShouldCallReturnNoContent() throws Exception {
        Integer id = 1;
        JobPickedUpDTO input = new JobPickedUpDTO(LocalDateTime.parse("2026-02-18T10:15:30"));
        Mockito.doNothing().when(jobService).pickedUpJob(Mockito.anyInt(), Mockito.any());

        mockMvc.perform(patch("/api/jobs/{id}/change-to-pickedup", id)
                .content(objectMapper.writeValueAsString(input))
                .contentType("application/json"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<JobPickedUpDTO> pickedUpCaptor = ArgumentCaptor.forClass(JobPickedUpDTO.class);
        Mockito.verify(jobService).pickedUpJob(Mockito.eq(id), pickedUpCaptor.capture());
        assertEquals(input.getDate(), pickedUpCaptor.getValue().getDate());
    }

    @Test
    void changeToPickedUpShouldHandleValidationErrors() throws Exception {
        Integer id = 1;
        JobPickedUpDTO input = new JobPickedUpDTO(LocalDateTime.parse("2026-02-18T10:15:30"));

        Mockito.doThrow(new ValidationException("date", "validation.invalid.date")).when(jobService)
                .pickedUpJob(Mockito.anyInt(), Mockito.any());

        mockMvc.perform(patch("/api/jobs/{id}/change-to-pickedup", id)
                .content(objectMapper.writeValueAsString(input))
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'date')].messageKey").value("validation.invalid.date"));

        Mockito.verify(jobService).pickedUpJob(Mockito.eq(id), Mockito.any(JobPickedUpDTO.class));
    }
    @Test
    void downloadSummaryShouldReturnPdf() throws Exception {
        Integer id = 123;
        byte[] pdfContent = "PDF content".getBytes();
        Mockito.when(jobService.getSummary(id)).thenReturn(pdfContent);

        mockMvc.perform(get("/api/jobs/{id}/summary", id))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=sample.pdf"))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(content().bytes(pdfContent));
    }
    @Test
    void getJobByIdShouldReturnJob() throws Exception {
        Integer id = 123;
        Job job = new Job();
        job.setId(id);
        Mockito.when(jobService.getJobById(id)).thenReturn(job);

        mockMvc.perform(get("/api/jobs/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }
    @Test
    void deleteJobByIdShouldReturnNoContent() throws Exception {
        Integer id = 123;

        Mockito.doNothing().when(jobService).deleteJob(id);

        mockMvc.perform(delete("/api/jobs/{id}", id))
            .andExpect(status().isNoContent());

        Mockito.verify(jobService).deleteJob(id);
    }
}