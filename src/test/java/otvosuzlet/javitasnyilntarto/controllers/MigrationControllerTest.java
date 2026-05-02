package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import otvosuzlet.javitasnyilntarto.dto.TransferRequest;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.PersonService;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(MigrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class MigrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonService personService;

    @BeforeEach
    void setUp() {
        Mockito.reset(personService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        PersonService personService() {
            return Mockito.mock(PersonService.class);
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
    void transferShouldCallServiceForAllEntries() throws Exception {
        TransferRequest first = new TransferRequest();
        first.setName("John Doe");
        first.setAddress("Main street");
        first.setPhone("123");
        first.setJobGroups(new ArrayList<>());

        TransferRequest second = new TransferRequest();
        second.setName("Jane Doe");
        second.setAddress("Oak avenue");
        second.setPhone("456");
        second.setJobGroups(new ArrayList<>());

        List<TransferRequest> payload = List.of(first, second);

        mockMvc.perform(post("/api/migration")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());

        ArgumentCaptor<TransferRequest> transferCaptor = ArgumentCaptor.forClass(TransferRequest.class);
        Mockito.verify(personService, Mockito.times(2)).transfer(transferCaptor.capture());
        List<TransferRequest> captured = transferCaptor.getAllValues();
        assertEquals("John Doe", captured.get(0).getName());
        assertEquals("Jane Doe", captured.get(1).getName());
    }

    @Test
    void deleteAllShouldCallServiceAndReturnOk() throws Exception {
        mockMvc.perform(delete("/api/migration"))
            .andExpect(status().isOk());

        Mockito.verify(personService).deleteAll();
    }

    @Test
    void downloadShouldReturnServiceData() throws Exception {
        TransferRequest response = new TransferRequest();
        response.setName("Exported Person");
        response.setAddress("Address 1");
        response.setPhone("555");
        response.setJobGroups(new ArrayList<>());

        Mockito.when(personService.downloadMigrationData()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/migration"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Exported Person"))
            .andExpect(jsonPath("$[0].address").value("Address 1"))
            .andExpect(jsonPath("$[0].phone").value("555"));

        Mockito.verify(personService).downloadMigrationData();
    }
}
