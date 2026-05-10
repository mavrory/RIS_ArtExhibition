package com.digitalart.exhibition.application.dto;

import com.digitalart.artwork.application.dto.ArtworkDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExhibitionDetailDto {

    private Long id;
    private String title;
    private String description;
    private Long createdBy;
    private String creatorName;
    private List<ArtworkDto> artworks;
    private LocalDateTime createdAt;
}
