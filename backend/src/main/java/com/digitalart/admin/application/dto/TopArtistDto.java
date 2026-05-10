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
public class TopArtistDto {
    private Long artistId;
    private String artistName;
    private Integer artworksCount;
    private Integer salesCount;
    private BigDecimal totalRevenue;
    private Long subscribersCount;
}
