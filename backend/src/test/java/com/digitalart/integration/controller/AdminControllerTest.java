package com.digitalart.integration.controller;

import com.digitalart.admin.application.AdminService;
import com.digitalart.admin.application.dto.DetailedStatisticsDto;
import com.digitalart.admin.application.dto.PlatformStatisticsDto;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.application.dto.UserDto;
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

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_shouldReturn403_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnList() throws Exception {
        UserDto dto = UserDto.builder().id(1L).email("user@test.com")
                .username("user").balance(0.0).roles(Set.of("USER")).build();

        when(adminService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStatistics_shouldReturnStats() throws Exception {
        PlatformStatisticsDto stats = PlatformStatisticsDto.builder()
                .totalUsers(10).totalArtists(3).totalArtworks(25)
                .totalExhibitions(5).totalRevenue(1000.0)
                .build();

        when(adminService.getPlatformStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDetailedStatistics_shouldReturnDetails() throws Exception {
        DetailedStatisticsDto details = DetailedStatisticsDto.builder()
                .totalUsers(10).totalArtists(3).totalArtworks(25)
                .totalExhibitions(5).totalOrders(20)
                .completedOrders(15).pendingOrders(3).cancelledOrders(2)
                .totalRevenue(new BigDecimal("1000"))
                .totalSales(new BigDecimal("15"))
                .averageArtworkPrice(new BigDecimal("40"))
                .totalViews(500L).totalFavorites(50L).totalComments(30L).totalSubscriptions(20L)
                .newUsersThisMonth(2).newArtworksThisMonth(5).salesThisMonth(3)
                .topArtists(List.of())
                .topArtworks(List.of())
                .usersByRole(Map.of("USER", 7, "ARTIST", 3, "ADMIN", 1))
                .revenueByMonth(Map.of())
                .build();

        when(adminService.getDetailedStatistics()).thenReturn(details);

        mockMvc.perform(get("/api/admin/statistics/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldSucceed() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("balance", 500.0);

        UserDto dto = UserDto.builder().id(1L).email("user@test.com")
                .username("user").balance(500.0).roles(Set.of("USER")).build();

        when(adminService.updateUser(eq(1L), anyMap())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/admin/users/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserRole_shouldSucceed() throws Exception {
        UserDto dto = UserDto.builder().id(1L).email("user@test.com")
                .username("user").balance(0.0).roles(Set.of("USER", "ARTIST")).build();

        when(adminService.toggleUserRole(1L, "ARTIST")).thenReturn(dto);

        mockMvc.perform(post("/api/admin/users/1/toggle-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ARTIST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArtwork_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/admin/artworks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArtwork_withOrders_shouldReturn400() throws Exception {
        doThrow(new BusinessException("Cannot delete artwork with existing orders"))
                .when(adminService).deleteArtwork(1L);

        mockMvc.perform(delete("/api/admin/artworks/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateUsersReport_shouldReturnCsv() throws Exception {
        when(adminService.generateUsersReport()).thenReturn("ID,Username,Email\n1,user,user@test.com\n");

        mockMvc.perform(get("/api/admin/reports/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("ID,Username,Email")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateArtworksReport_shouldReturnCsv() throws Exception {
        when(adminService.generateArtworksReport()).thenReturn("ID,Title,Artist\n1,Artwork,artist\n");

        mockMvc.perform(get("/api/admin/reports/artworks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateOrdersReport_shouldReturnCsv() throws Exception {
        when(adminService.generateOrdersReport()).thenReturn("ID,User,Artwork\n1,user,artwork\n");

        mockMvc.perform(get("/api/admin/reports/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateRevenueReport_shouldReturnCsv() throws Exception {
        when(adminService.generateRevenueReport()).thenReturn("Month,Revenue\nJan,100.00\n");

        mockMvc.perform(get("/api/admin/reports/revenue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateArtistsReport_shouldReturnCsv() throws Exception {
        when(adminService.generateArtistsReport()).thenReturn("Artist ID,Artist Name\n1,artist\n");

        mockMvc.perform(get("/api/admin/reports/artists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
