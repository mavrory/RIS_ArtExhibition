package com.digitalart.artwork.presentation;

import com.digitalart.artwork.application.ArtworkService;
import com.digitalart.artwork.application.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtworkDto> createArtwork(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") String price,
            @RequestParam("image") MultipartFile image,
            Authentication authentication) throws IOException {
        
        CreateArtworkRequest request = new CreateArtworkRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(new java.math.BigDecimal(price));
        
        String email = authentication.getName();
        ArtworkDto artwork = artworkService.createArtwork(request, image, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(artwork);
    }

    @GetMapping
    public ResponseEntity<List<ArtworkDto>> getAllArtworks(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        List<ArtworkDto> artworks = artworkService.getAllArtworks(email);
        return ResponseEntity.ok(artworks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtworkDetailDto> getArtworkDetail(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        ArtworkDetailDto artwork = artworkService.getArtworkDetail(id, email);
        return ResponseEntity.ok(artwork);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> getArtworkPreview(@PathVariable Long id) {
        byte[] imageData = artworkService.getArtworkPreview(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getArtworkImage(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        byte[] imageData = artworkService.getArtworkImage(id, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("attachment", "artwork-" + id + ".jpg");
        
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        CommentDto comment = artworkService.addComment(id, request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        artworkService.toggleFavorite(id, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ArtworkDto>> getFavoriteArtworks(Authentication authentication) {
        String email = authentication.getName();
        List<ArtworkDto> artworks = artworkService.getFavoriteArtworks(email);
        return ResponseEntity.ok(artworks);
    }
    
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<ArtworkDto>> getArtworksByArtist(
            @PathVariable Long artistId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        List<ArtworkDto> artworks = artworkService.getArtworksByArtist(artistId, email);
        return ResponseEntity.ok(artworks);
    }
}
