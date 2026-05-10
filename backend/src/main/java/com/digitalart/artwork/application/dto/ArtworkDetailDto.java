package com.digitalart.artwork.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkDetailDto {

    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isSold;
    private Boolean isPurchased;
    private Long favoritesCount;
    private Long viewsCount;
    private Boolean isFavorited;
    private List<CommentDto> comments;
    private LocalDateTime createdAt;
}
