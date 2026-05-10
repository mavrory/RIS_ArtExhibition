package com.digitalart.exhibition.application;

import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.artwork.infrastructure.FavoriteRepository;
import com.digitalart.exhibition.application.dto.*;
import com.digitalart.exhibition.domain.Exhibition;
import com.digitalart.exhibition.infrastructure.ExhibitionRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ArtworkRepository artworkRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;

    @Transactional
    public ExhibitionDto createExhibition(CreateExhibitionRequest request, String userEmail) {
        User user = userService.getUserByEmail(userEmail);

        // Check if user has ARTIST role
        boolean isArtist = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ARTIST"));
        
        if (!isArtist) {
            throw new BusinessException("Only artists can create exhibitions");
        }

        Exhibition exhibition = Exhibition.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(user.getId())
                .build();

        exhibition = exhibitionRepository.save(exhibition);

        return mapToExhibitionDto(exhibition, user.getUsername());
    }

    public List<ExhibitionDto> getAllExhibitions() {
        List<Exhibition> exhibitions = exhibitionRepository.findAll();

        return exhibitions.stream()
                .map(exhibition -> {
                    try {
                        User creator = getUserById(exhibition.getCreatedBy());
                        return mapToExhibitionDto(exhibition, creator.getUsername());
                    } catch (Exception e) {
                        System.err.println("Failed to get creator for exhibition " + exhibition.getId() + ": " + e.getMessage());
                        return mapToExhibitionDto(exhibition, "Unknown Artist");
                    }
                })
                .collect(Collectors.toList());
    }

    public ExhibitionDetailDto getExhibitionDetail(Long id, String userEmail) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exhibition not found"));

        String creatorName = "Unknown Artist";
        try {
            User creator = getUserById(exhibition.getCreatedBy());
            creatorName = creator.getUsername();
        } catch (Exception e) {
            System.err.println("Failed to get creator for exhibition " + exhibition.getId() + ": " + e.getMessage());
        }
        
        Long userId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;

        List<ArtworkDto> artworkDtos = exhibition.getArtworks().stream()
                .map(artwork -> mapToArtworkDto(artwork, userId))
                .collect(Collectors.toList());

        return ExhibitionDetailDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .description(exhibition.getDescription())
                .createdBy(exhibition.getCreatedBy())
                .creatorName(creatorName)
                .artworks(artworkDtos)
                .createdAt(exhibition.getCreatedAt())
                .build();
    }

    @Transactional
    public void addArtworkToExhibition(Long exhibitionId, AddArtworkRequest request, String userEmail) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Exhibition not found"));

        User user = userService.getUserByEmail(userEmail);

        // Check if user is the creator of the exhibition
        if (!exhibition.getCreatedBy().equals(user.getId())) {
            throw new BusinessException("Only the creator can add artworks to this exhibition");
        }

        Artwork artwork = artworkRepository.findById(request.getArtworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        exhibition.getArtworks().add(artwork);
        exhibitionRepository.save(exhibition);
    }

    private ExhibitionDto mapToExhibitionDto(Exhibition exhibition, String creatorName) {
        String coverImageUrl = null;
        if (!exhibition.getArtworks().isEmpty()) {
            // Use the first artwork's preview as cover
            Long firstArtworkId = exhibition.getArtworks().iterator().next().getId();
            coverImageUrl = "/api/artworks/" + firstArtworkId + "/preview";
        }
        
        return ExhibitionDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .description(exhibition.getDescription())
                .createdBy(exhibition.getCreatedBy())
                .creatorName(creatorName)
                .artworksCount(exhibition.getArtworks().size())
                .coverImageUrl(coverImageUrl)
                .createdAt(exhibition.getCreatedAt())
                .build();
    }

    private ArtworkDto mapToArtworkDto(Artwork artwork, Long currentUserId) {
        String authorName = "Unknown Artist";
        try {
            User author = getUserById(artwork.getAuthorId());
            authorName = author.getUsername();
        } catch (Exception e) {
            System.err.println("Failed to get author for artwork " + artwork.getId() + ": " + e.getMessage());
        }
        
        boolean isFavorited = currentUserId != null && 
                favoriteRepository.findByIdUserIdAndIdArtworkId(currentUserId, artwork.getId()).isPresent();
        long favoritesCount = favoriteRepository.countByIdArtworkId(artwork.getId());

        return ArtworkDto.builder()
                .id(artwork.getId())
                .authorId(artwork.getAuthorId())
                .authorName(authorName)
                .title(artwork.getTitle())
                .description(artwork.getDescription())
                .price(artwork.getPrice())
                .previewUrl(artwork.getPreviewUrl())
                .isSold(artwork.getIsSold())
                .favoritesCount(favoritesCount)
                .viewsCount(artwork.getViewsCount())
                .isFavorited(isFavorited)
                .createdAt(artwork.getCreatedAt())
                .build();
    }

    private User getUserById(Long userId) {
        return userService.getUserById(userId);
    }
}
