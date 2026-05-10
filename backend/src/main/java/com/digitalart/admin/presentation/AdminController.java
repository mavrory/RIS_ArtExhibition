package com.digitalart.admin.presentation;

import com.digitalart.admin.application.AdminService;
import com.digitalart.admin.application.dto.DetailedStatisticsDto;
import com.digitalart.admin.application.dto.PlatformStatisticsDto;
import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.user.application.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/statistics")
    public ResponseEntity<PlatformStatisticsDto> getStatistics() {
        return ResponseEntity.ok(adminService.getPlatformStatistics());
    }

    @GetMapping("/statistics/detailed")
    public ResponseEntity<DetailedStatisticsDto> getDetailedStatistics() {
        return ResponseEntity.ok(adminService.getDetailedStatistics());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(adminService.updateUser(id, updates));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/toggle-role")
    public ResponseEntity<UserDto> toggleRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        return ResponseEntity.ok(adminService.toggleUserRole(id, role));
    }

    @GetMapping("/artworks")
    public ResponseEntity<List<ArtworkDto>> getAllArtworks() {
        return ResponseEntity.ok(adminService.getAllArtworks());
    }

    @DeleteMapping("/artworks/{id}")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long id) {
        adminService.deleteArtwork(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports/users")
    public ResponseEntity<String> exportUsersReport() {
        String csv = adminService.generateUsersReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "users_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/reports/artworks")
    public ResponseEntity<String> exportArtworksReport() {
        String csv = adminService.generateArtworksReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "artworks_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/reports/orders")
    public ResponseEntity<String> exportOrdersReport() {
        String csv = adminService.generateOrdersReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "orders_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<String> exportRevenueReport() {
        String csv = adminService.generateRevenueReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "revenue_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/reports/artists")
    public ResponseEntity<String> exportArtistsReport() {
        String csv = adminService.generateArtistsReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "artists_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
