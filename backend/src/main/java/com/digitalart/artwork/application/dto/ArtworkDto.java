package com.digitalart.artwork.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkDto {

    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String description;
    private BigDecimal price;
    private String previewUrl;
    private Boolean isSold;
    private Long favoritesCount;
    private Long viewsCount;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
