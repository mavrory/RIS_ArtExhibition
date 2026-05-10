package com.digitalart.exhibition.presentation;

import com.digitalart.exhibition.application.ExhibitionService;
import com.digitalart.exhibition.application.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @PostMapping
    public ResponseEntity<ExhibitionDto> createExhibition(
            @Valid @RequestBody CreateExhibitionRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ExhibitionDto exhibition = exhibitionService.createExhibition(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(exhibition);
    }

    @GetMapping
    public ResponseEntity<List<ExhibitionDto>> getAllExhibitions() {
        List<ExhibitionDto> exhibitions = exhibitionService.getAllExhibitions();
        return ResponseEntity.ok(exhibitions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionDetailDto> getExhibitionDetail(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        ExhibitionDetailDto exhibition = exhibitionService.getExhibitionDetail(id, email);
        return ResponseEntity.ok(exhibition);
    }

    @PostMapping("/{id}/artworks")
    public ResponseEntity<Void> addArtworkToExhibition(
            @PathVariable Long id,
            @Valid @RequestBody AddArtworkRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        exhibitionService.addArtworkToExhibition(id, request, email);
        return ResponseEntity.ok().build();
    }
}
