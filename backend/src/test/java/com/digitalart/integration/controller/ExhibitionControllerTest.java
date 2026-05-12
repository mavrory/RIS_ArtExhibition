package com.digitalart.integration.controller;

import com.digitalart.exhibition.application.ExhibitionService;
import com.digitalart.exhibition.application.dto.AddArtworkRequest;
import com.digitalart.exhibition.application.dto.CreateExhibitionRequest;
import com.digitalart.exhibition.application.dto.ExhibitionDetailDto;
import com.digitalart.exhibition.application.dto.ExhibitionDto;
import com.digitalart.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExhibitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExhibitionService exhibitionService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExhibitionDto createDto(Long id) {
        return ExhibitionDto.builder()
                .id(id).title("Exhibition").description("Desc")
                .createdBy(1L).creatorName("artist")
                .artworksCount(0).createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ARTIST")
    void createExhibition_shouldReturn201() throws Exception {
        CreateExhibitionRequest request = new CreateExhibitionRequest();
        request.setTitle("New Exhibition");
        request.setDescription("Description");

        when(exhibitionService.createExhibition(any(CreateExhibitionRequest.class), anyString()))
                .thenReturn(createDto(1L));

        mockMvc.perform(post("/api/exhibitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Exhibition"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createExhibition_withoutArtistRole_shouldReturn400() throws Exception {
        CreateExhibitionRequest request = new CreateExhibitionRequest();
        request.setTitle("Exhibition");
        request.setDescription("Desc");

        when(exhibitionService.createExhibition(any(CreateExhibitionRequest.class), anyString()))
                .thenThrow(new BusinessException("Only artists can create exhibitions"));

        mockMvc.perform(post("/api/exhibitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAllExhibitions_shouldReturnList() throws Exception {
        when(exhibitionService.getAllExhibitions()).thenReturn(List.of(createDto(1L), createDto(2L)));

        mockMvc.perform(get("/api/exhibitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getExhibitionDetail_shouldReturnDetail() throws Exception {
        ExhibitionDetailDto detail = ExhibitionDetailDto.builder()
                .id(1L).title("Exhibition").description("Desc")
                .createdBy(1L).creatorName("artist")
                .artworks(List.of()).createdAt(LocalDateTime.now())
                .build();

        when(exhibitionService.getExhibitionDetail(eq(1L), any())).thenReturn(detail);

        mockMvc.perform(get("/api/exhibitions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"));
    }

    @Test
    @WithMockUser
    void addArtworkToExhibition_shouldSucceed() throws Exception {
        AddArtworkRequest request = new AddArtworkRequest();
        request.setArtworkId(1L);

        mockMvc.perform(post("/api/exhibitions/1/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
