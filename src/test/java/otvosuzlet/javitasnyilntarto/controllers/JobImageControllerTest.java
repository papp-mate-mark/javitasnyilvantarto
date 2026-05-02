package otvosuzlet.javitasnyilntarto.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import otvosuzlet.javitasnyilntarto.exceptions.GlobalExceptionHandler;
import otvosuzlet.javitasnyilntarto.filter.JwtFilter;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.service.JWTService;
import otvosuzlet.javitasnyilntarto.service.JobImageService;

@WebMvcTest(JobImageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost")
public class JobImageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JobImageService jobImageService;

    @BeforeEach
    void setUp() {
        Mockito.reset(jobImageService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        JobImageService jobImageService() {
            return Mockito.mock(JobImageService.class);
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
    void getFullImageByIdShouldReturnImageBytesAndHeaders() throws Exception {
        byte[] imageBytes = "full-image".getBytes();
        Mockito.when(jobImageService.getFullImageById(10)).thenReturn(imageBytes);

        mockMvc.perform(get("/api/jobimage/10"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=image.jpg"))
            .andExpect(header().string("Content-Type", "image/jpg"))
            .andExpect(content().bytes(imageBytes));
    }

    @Test
    void getThumbnailByIdShouldReturnImageBytesAndHeaders() throws Exception {
        byte[] imageBytes = "thumbnail-image".getBytes();
        Mockito.when(jobImageService.getThumbnailById(10)).thenReturn(imageBytes);

        mockMvc.perform(get("/api/jobimage/10/thumbnail"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=image.jpg"))
            .andExpect(header().string("Content-Type", "image/jpg"))
            .andExpect(content().bytes(imageBytes));
    }

    @Test
    void uploadImageShouldReturnCreatedAndImageId() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "img-bytes".getBytes());

        JobImage savedImage = new JobImage();
        savedImage.setId(77);
        Mockito.when(jobImageService.uploadImage(Mockito.any())).thenReturn(savedImage);

        mockMvc.perform(multipart("/api/jobimage").file(file))
            .andExpect(status().isCreated())
            .andExpect(content().string("77"));

        Mockito.verify(jobImageService).uploadImage(Mockito.any());
    }

    @Test
    void deleteImageShouldCallServiceAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/jobimage/55"))
            .andExpect(status().isNoContent());

        Mockito.verify(jobImageService).deleteImage(55);
    }
}
