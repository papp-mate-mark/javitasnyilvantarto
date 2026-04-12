package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingDto;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;
import otvosuzlet.javitasnyilntarto.dto.UpdateSystemSettingRequest;
import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.repository.SystemSettingRepository;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.SystemSettingService;

@WebMvcTest(SystemSettingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class SystemSettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SystemSettingService systemSettingService;

    @Autowired
    ConversionService conversionService;

    @BeforeEach
    void setUp() {
        Mockito.reset(systemSettingService);
        Mockito.reset(conversionService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        SystemSettingService systemSettingService() {
            return Mockito.mock(SystemSettingService.class);
        }

        @Bean
        SystemSettingRepository systemSettingRepository() {
            return Mockito.mock(SystemSettingRepository.class);
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
    void listShouldReturnConvertedDataAndCallService() throws Exception {
        @SuppressWarnings("unchecked")
        Page<SystemSettingDto> mockPageResponse = Mockito.mock(Page.class);

        SystemSettingDto setting1 = new SystemSettingDto();
        setting1.setKey("receipt.title");
        setting1.setDescriptionKey("receipt.title.description");
        setting1.setValue("Some Description");

        SystemSettingDto setting2 = new SystemSettingDto();
        setting2.setKey("receipt.store_data");
        setting2.setDescriptionKey("receipt.store_data.description");
        setting2.setValue("Some store data");

        PageResponse<SystemSettingDto> convertedResponse = new PageResponse<>(
                List.of(setting1, setting2),
                2,
                1,
                0,
                20,
                true,
                true);

        Mockito.when(systemSettingService.fetchAll(Mockito.any(Pageable.class))).thenReturn(mockPageResponse);
        Mockito.when(conversionService.convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class))).thenReturn(convertedResponse);

        mockMvc.perform(get("/api/admin/system-settings")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].key").value(setting1.getKey()))
                .andExpect(jsonPath("$.content[0].value").value(setting1.getValue()))
                .andExpect(jsonPath("$.content[1].key").value(setting2.getKey()))
                .andExpect(jsonPath("$.content[1].value").value(setting2.getValue()));

        Mockito.verify(systemSettingService).fetchAll(Mockito.any(Pageable.class));
        Mockito.verify(conversionService).convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class));
    }

    @Test
    void searchShouldReturnConvertedDataAndCallServiceWithKey() throws Exception {
        @SuppressWarnings("unchecked")
        Page<SystemSettingDto> mockPageResponse = Mockito.mock(Page.class);

        SystemSettingDto setting = new SystemSettingDto();
        setting.setKey("receipt.store_data");
        setting.setDescriptionKey("receipt.store_data.description");
        setting.setValue("Some store data");

        String searchKey = "receipt";

        PageResponse<SystemSettingDto> convertedResponse = new PageResponse<>(
                List.of(setting),
                1,
                1,
                0,
                20,
                true,
                true);

        Mockito.when(systemSettingService.searchByKey(Mockito.any(SystemSettingSearchRequest.class), Mockito.any(Pageable.class))).thenReturn(mockPageResponse);
        Mockito.when(conversionService.convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class))).thenReturn(convertedResponse);

        mockMvc.perform(get("/api/admin/system-settings/search")
                .param("key", searchKey)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].key").value(setting.getKey()))
                .andExpect(jsonPath("$.content[0].value").value(setting.getValue()));

            Mockito.verify(systemSettingService).searchByKey(
                Mockito.argThat(request -> request != null && searchKey.equals(request.getKey())),
                Mockito.any(Pageable.class));
        Mockito.verify(conversionService).convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class));
    }

    @Test
    void searchShouldUseDefaultEmptyKeyWhenMissing() throws Exception {
        @SuppressWarnings("unchecked")
        Page<SystemSettingDto> mockPageResponse = Mockito.mock(Page.class);

        PageResponse<SystemSettingDto> convertedResponse = new PageResponse<>(
                List.of(),
                0,
                0,
                0,
                20,
                true,
                true);

        Mockito.when(systemSettingService.searchByKey(Mockito.any(SystemSettingSearchRequest.class), Mockito.any(Pageable.class))).thenReturn(mockPageResponse);
        Mockito.when(conversionService.convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class))).thenReturn(convertedResponse);

        mockMvc.perform(get("/api/admin/system-settings/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        Mockito.verify(systemSettingService).searchByKey(
            Mockito.argThat(request -> request != null && request.getKey() == null),
            Mockito.any(Pageable.class));
        Mockito.verify(conversionService).convert(Mockito.eq(mockPageResponse), Mockito.eq(PageResponse.class));
    }

    @Test
    void updateShouldCallServiceAndReturnNoContent() throws Exception {
        String key = "receipt.title";
        UpdateSystemSettingRequest request = new UpdateSystemSettingRequest();
        request.setValue("Some new title");

        mockMvc.perform(patch("/api/admin/system-settings/{key}", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Mockito.verify(systemSettingService).updateValue(Mockito.eq(key), Mockito.eq(request.getValue()));
    }
}
