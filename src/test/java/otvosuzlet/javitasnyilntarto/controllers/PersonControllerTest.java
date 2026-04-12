package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.projections.JobGroupFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;
import otvosuzlet.javitasnyilntarto.service.PersonService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
class PersonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonService personService;

    @Autowired
    JobGroupService jobGroupService;

    @Autowired
    ConversionService conversionService;

    @BeforeEach
    void setUp() {
        Mockito.reset(personService);
        Mockito.reset(jobGroupService);
        Mockito.reset(conversionService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        JobGroupService jobGroupService() {
            return Mockito.mock(JobGroupService.class);
        }        
        
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

        @Bean
        ConversionService conversionService() {
            return Mockito.mock(ConversionService.class);
        }
    }

    @Test
    void findPersonFromIdShouldCallWithCorrectId() throws Exception {
        Integer id = 123;
        mockMvc.perform(get("/api/person/{id}", id));
        Mockito.verify(personService).findByIdFullInfoProjection(id);
    }
    @Test
    void findPersonFromIdShouldReturnOkWhenFound() throws Exception {
        Integer id = 123;
        Optional<PersonFullInfoProjection> expected = Optional.empty();
        Mockito.when(personService.findByIdFullInfoProjection(id)).thenReturn(expected);

        mockMvc.perform(get("/api/person/{id}", id))
            .andExpect(status().isOk());

        Mockito.verify(personService).findByIdFullInfoProjection(id);
    }

    @Test
    void findPersonFromIdShouldReturnDataFromService() throws Exception {
        Integer id = 123;

        PersonFullInfoProjection personData = Mockito.spy(new PersonFullInfoProjection() {
            @Override
            public Integer getId() { return id; }
            @Override
            public String getName() { return "John Doe"; }
            @Override
            public String getPhone() { return "123-456-7890"; }
            @Override
            public String getAddress() { return "123 Main St"; }
            @Override
            public Set<JobGroupFullInfoProjection> getJobGroups() { return new HashSet<JobGroupFullInfoProjection>(); }
        });

        Optional<PersonFullInfoProjection> expected = Optional.of(personData);
        Mockito.when(personService.findByIdFullInfoProjection(id)).thenReturn(expected);

        mockMvc.perform(get("/api/person/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.phone").value("123-456-7890"))
            .andExpect(jsonPath("$.address").value("123 Main St"));

        Mockito.verify(personService).findByIdFullInfoProjection(id);
    }

    // Tests for createPerson endpoint
    @Test
    void createPersonShouldReturn400AndValidationErrorsOnInvalidData() throws Exception {
        PersonRequest request = new PersonRequest();
        // Missing required fields: name and address

        mockMvc.perform(post("/api/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void createPersonShouldReturn200OnSuccess() throws Exception {
        PersonRequest request = new PersonRequest();
        request.setName("Jane Smith");
        request.setAddress("456 Oak Ave");
        request.setPhone("555-1234");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/api/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void createPersonShouldCallServiceMethodWithCorrectData() throws Exception {
        PersonRequest request = new PersonRequest();
        request.setName("Jane Smith");
        request.setAddress("456 Oak Ave");
        request.setPhone("555-1234");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/api/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        ArgumentCaptor<PersonRequest> requestCaptor = ArgumentCaptor.forClass(PersonRequest.class);
        Mockito.verify(personService).createPersonWithJobGroup(requestCaptor.capture());

        PersonRequest capturedRequest = requestCaptor.getValue();
        assert capturedRequest.getName().equals("Jane Smith");
        assert capturedRequest.getAddress().equals("456 Oak Ave");
        assert capturedRequest.getPhone().equals("555-1234");
    }


    // Tests for addJobGroupToPerson endpoint
    @Test
    void addJobGroupToPersonShouldReturn400AndValidationErrorsOnInvalidData() throws Exception {
        JobGroupDto request = new JobGroupDto();
        // Missing required field: deadline
        Integer personId = 1;

        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("testuser");

        mockMvc.perform(post("/api/person/{personId}/job-groups", personId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorType").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void addJobGroupToPersonShouldReturn200OnSuccess() throws Exception {
        JobGroupDto request = new JobGroupDto();
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setBringin(LocalDateTime.now());
        Integer personId = 1;

        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("testuser");

        JobGroupUploadResponse response = new JobGroupUploadResponse(5);
        Mockito.when(jobGroupService.addJobGroupToPerson(
            Mockito.any(JobGroupDto.class), 
            Mockito.eq(personId), 
            Mockito.eq("testuser")))
            .thenReturn(response);

        mockMvc.perform(post("/api/person/{personId}/job-groups", personId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groupId").value(5));
    }

    @Test
    void addJobGroupToPersonShouldCallServiceWithCorrectParameters() throws Exception {
        JobGroupDto request = new JobGroupDto();
        request.setDeadline(LocalDateTime.now().plusDays(7));
        Integer personId = 42;

        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("testuser");

        JobGroupUploadResponse response = new JobGroupUploadResponse(5);
        Mockito.when(jobGroupService.addJobGroupToPerson(
            Mockito.any(JobGroupDto.class), 
            Mockito.anyInt(), 
            Mockito.anyString()))
            .thenReturn(response);

        mockMvc.perform(post("/api/person/{personId}/job-groups", personId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        ArgumentCaptor<JobGroupDto> requestCaptor = ArgumentCaptor.forClass(JobGroupDto.class);
        ArgumentCaptor<Integer> personIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(jobGroupService).addJobGroupToPerson(
            requestCaptor.capture(), 
            personIdCaptor.capture(), 
            usernameCaptor.capture());

        assert personIdCaptor.getValue().equals(personId);
        assert usernameCaptor.getValue().equals("testuser");
    }


    // Tests for getActiveJobs endpoint
    @Test
    void getActiveJobsShouldCallServiceMethod() throws Exception {
        mockMvc.perform(get("/api/person/getActiveJobs"));
        Mockito.verify(jobGroupService).getActiveJobsGroups();
    }

    @Test
    void getActiveJobsShouldReturn200() throws Exception {
        ActiveJobsRequestDTO response = new ActiveJobsRequestDTO();
        Mockito.when(jobGroupService.getActiveJobsGroups()).thenReturn(response);

        mockMvc.perform(get("/api/person/getActiveJobs"))
            .andExpect(status().isOk());
    }

    @Test
    void getActiveJobsShouldReturnDataFromService() throws Exception {
        ActiveJobsRequestDTO response = new ActiveJobsRequestDTO();
        Mockito.when(jobGroupService.getActiveJobsGroups()).thenReturn(response);

        mockMvc.perform(get("/api/person/getActiveJobs"))
            .andExpect(status().isOk());

        Mockito.verify(jobGroupService).getActiveJobsGroups();
    }

    // Tests for deletePerson endpoint
    @Test
    void deletePersonShouldCallServiceWithCorrectId() throws Exception {
        Integer id = 123;

        mockMvc.perform(delete("/api/person/{id}", id));

        Mockito.verify(personService).deletePerson(id);
    }

    @Test
    void deletePersonShouldReturn204NoContent() throws Exception {
        Integer id = 123;

        mockMvc.perform(delete("/api/person/{id}", id))
            .andExpect(status().isNoContent());

        Mockito.verify(personService).deletePerson(id);
    }


    // Tests for searchPersons endpoint
    @Test
    void searchPersonsShouldCallServiceWithCorrectParameters() throws Exception {
        Page<PersonInfoDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        PageResponse<PersonInfoDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());

        Mockito.when(personService.searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any()))
            .thenReturn(page);
        Mockito.when(conversionService.convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class)))
            .thenReturn(pageResponse);

        mockMvc.perform(get("/api/person/search")
                .param("name", "John")
                .param("address", "Main")
                .param("phone", "555"));

        ArgumentCaptor<PersonSearchRequest> searchCaptor = ArgumentCaptor.forClass(PersonSearchRequest.class);
        Mockito.verify(personService).searchForPerson(searchCaptor.capture(), Mockito.any());

        PersonSearchRequest capturedSearch = searchCaptor.getValue();
        assert capturedSearch.getName().equals("John");
        assert capturedSearch.getAddress().equals("Main");
        assert capturedSearch.getPhone().equals("555");
    }

    @Test
    void searchPersonsShouldReturn200OnSuccess() throws Exception {
        Page<PersonInfoDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        PageResponse<PersonInfoDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());

        Mockito.when(personService.searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any()))
            .thenReturn(page);
        Mockito.when(conversionService.convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class)))
            .thenReturn(pageResponse);

        mockMvc.perform(get("/api/person/search")
                .param("name", "John"))
            .andExpect(status().isOk());
    }

    @Test
    void searchPersonsShouldCallConversionService() throws Exception {
        Page<PersonInfoDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        PageResponse<PersonInfoDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());

        Mockito.when(personService.searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any()))
            .thenReturn(page);
        Mockito.when(conversionService.convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class)))
            .thenReturn(pageResponse);

        mockMvc.perform(get("/api/person/search")
                .param("name", "Jane"))
            .andExpect(status().isOk());

        Mockito.verify(conversionService).convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class));
    }

    @Test
    void searchPersonsShouldWorkWithEmptyParameters() throws Exception {
        Page<PersonInfoDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        PageResponse<PersonInfoDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());
        pageResponse.setTotalElements(0L);

        Mockito.when(personService.searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any()))
            .thenReturn(page);
        Mockito.when(conversionService.convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class)))
            .thenReturn(pageResponse);

        mockMvc.perform(get("/api/person/search"))
            .andExpect(status().isOk());

        Mockito.verify(personService).searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any());
    }

    @Test
    void searchPersonsShouldHandlePagination() throws Exception {
        Page<PersonInfoDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(2, 5), 20);
        PageResponse<PersonInfoDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());
        pageResponse.setTotalElements(20L);
        pageResponse.setTotalPages(4);
        pageResponse.setSize(5);
        pageResponse.setPage(2);

        Mockito.when(personService.searchForPerson(Mockito.any(PersonSearchRequest.class), Mockito.any()))
            .thenReturn(page);
        Mockito.when(conversionService.convert(Mockito.any(Page.class), Mockito.eq(PageResponse.class)))
            .thenReturn(pageResponse);

        mockMvc.perform(get("/api/person/search")
                .param("name", "Test")
                .param("page", "2")
                .param("size", "5"))
            .andExpect(status().isOk());

        ArgumentCaptor<PersonSearchRequest> searchCaptor = ArgumentCaptor.forClass(PersonSearchRequest.class);
        Mockito.verify(personService).searchForPerson(searchCaptor.capture(), Mockito.any());
    }


}
