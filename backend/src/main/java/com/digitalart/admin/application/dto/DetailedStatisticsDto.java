package com.digitalart.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedStatisticsDto {
    private Integer totalUsers;
    private Integer totalArtists;
    private Integer totalArtworks;
    private Integer totalExhibitions;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer pendingOrders;
    private Integer cancelledOrders;
    
    private BigDecimal totalRevenue;
    private BigDecimal totalSales;
    private BigDecimal averageArtworkPrice;
    
    private Long totalViews;
    private Long totalFavorites;
    private Long totalComments;
    private Long totalSubscriptions;
    
    private Integer newUsersThisMonth;
    private Integer newArtworksThisMonth;
    private Integer salesThisMonth;
    
    private List<TopArtistDto> topArtists;
    private List<TopArtworkDto> topArtworks;
    private Map<String, Integer> usersByRole;
    private Map<String, BigDecimal> revenueByMonth;
}
