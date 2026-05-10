package com.digitalart.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopArtworkDto {
    private Long artworkId;
    private String title;
    private String artistName;
    private BigDecimal price;
    private Long viewsCount;
    private Long favoritesCount;
    private Boolean isSold;
}
