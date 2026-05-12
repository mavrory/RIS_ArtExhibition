package com.digitalart.integration.controller;

import com.digitalart.artwork.application.ArtworkService;
import com.digitalart.artwork.application.dto.ArtworkDetailDto;
import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArtworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtworkService artworkService;

    private ArtworkDto createArtworkDto(Long id) {
        return ArtworkDto.builder()
                .id(id)
                .authorId(1L)
                .authorName("artist")
                .title("Test Artwork")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .previewUrl("/api/artworks/" + id + "/preview")
                .isSold(false)
                .favoritesCount(0L)
                .viewsCount(0L)
                .isFavorited(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void getAllArtworks_shouldReturnList() throws Exception {
        when(artworkService.getAllArtworks(any())).thenReturn(List.of(
                createArtworkDto(1L), createArtworkDto(2L)
        ));

        mockMvc.perform(get("/api/artworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getArtworkDetail_shouldReturnDetail() throws Exception {
        ArtworkDetailDto detail = ArtworkDetailDto.builder()
                .id(1L).authorId(1L).authorName("artist")
                .title("Test").description("Desc")
                .price(new BigDecimal("100")).imageUrl("/preview")
                .isSold(false).isPurchased(false)
                .favoritesCount(0L).viewsCount(10L)
                .isFavorited(false).comments(List.of())
                .createdAt(LocalDateTime.now())
                .build();

        when(artworkService.getArtworkDetail(eq(1L), any())).thenReturn(detail);

        mockMvc.perform(get("/api/artworks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser
    void getArtworkDetail_withNonExistentId_shouldReturn404() throws Exception {
        when(artworkService.getArtworkDetail(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Artwork not found"));

        mockMvc.perform(get("/api/artworks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void addComment_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/artworks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Nice!\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void toggleFavorite_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/artworks/1/favorite"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getFavorites_shouldReturnList() throws Exception {
        when(artworkService.getFavoriteArtworks(anyString())).thenReturn(List.of(createArtworkDto(1L)));

        mockMvc.perform(get("/api/artworks/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getArtworksByArtist_shouldReturnList() throws Exception {
        when(artworkService.getArtworksByArtist(eq(1L), any())).thenReturn(List.of(createArtworkDto(1L)));

        mockMvc.perform(get("/api/artworks/artist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
