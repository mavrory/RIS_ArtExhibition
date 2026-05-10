package com.digitalart.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformStatisticsDto {
    private Integer totalUsers;
    private Integer totalArtists;
    private Integer totalArtworks;
    private Integer totalExhibitions;
    private Double totalRevenue;
}
