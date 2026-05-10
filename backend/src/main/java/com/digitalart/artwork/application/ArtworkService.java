package com.digitalart.artwork.application;

import com.digitalart.artwork.application.dto.*;
import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.domain.Comment;
import com.digitalart.artwork.domain.Favorite;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.artwork.infrastructure.CommentRepository;
import com.digitalart.artwork.infrastructure.FavoriteRepository;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Transactional
    public ArtworkDto createArtwork(CreateArtworkRequest request, MultipartFile imageFile, String userEmail) throws IOException {
        User user = userService.getUserByEmail(userEmail);

        // Check if user has ARTIST role
        boolean isArtist = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ARTIST"));
        
        if (!isArtist) {
            throw new BusinessException("Only artists can create artworks");
        }

        // Read original image
        byte[] originalImageData = imageFile.getBytes();

        // Generate preview with watermark
        byte[] previewImageData = generatePreviewWithWatermark(originalImageData);

        Artwork artwork = Artwork.builder()
                .authorId(user.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl("/api/artworks/" + "temp" + "/image") // Will be updated after save
                .previewUrl("/api/artworks/" + "temp" + "/preview")
                .imageData(originalImageData)
                .previewData(previewImageData)
                .isSold(false)
                .build();

        artwork = artworkRepository.save(artwork);

        // Update URLs with actual ID
        artwork.setImageUrl("/api/artworks/" + artwork.getId() + "/image");
        artwork.setPreviewUrl("/api/artworks/" + artwork.getId() + "/preview");
        artwork = artworkRepository.save(artwork);

        return mapToArtworkDto(artwork, user.getUsername(), null);
    }

    public List<ArtworkDto> getAllArtworks(String userEmail) {
        System.out.println("=== Getting all artworks ===");
        List<Artwork> artworks = artworkRepository.findAll();
        System.out.println("Found " + artworks.size() + " artworks");
        Long userId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;

        return artworks.stream()
                .map(artwork -> {
                    try {
                        System.out.println("Processing artwork: " + artwork.getId() + " - " + artwork.getTitle());
                        System.out.println("Author ID: " + artwork.getAuthorId());
                        User author = getUserById(artwork.getAuthorId());
                        System.out.println("Author found: " + author.getUsername());
                        ArtworkDto dto = mapToArtworkDto(artwork, author.getUsername(), userId);
                        System.out.println("DTO authorName: " + dto.getAuthorName());
                        return dto;
                    } catch (Exception e) {
                        System.err.println("Failed to get author for artwork " + artwork.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        return mapToArtworkDto(artwork, "Unknown Artist", userId);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ArtworkDetailDto getArtworkDetail(Long id, String userEmail) {
        try {
            System.out.println("=== Getting artwork detail for id: " + id + " ===");
            Artwork artwork = artworkRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

            System.out.println("Artwork found: " + artwork.getTitle());
            System.out.println("Author ID: " + artwork.getAuthorId());
            System.out.println("Views count: " + artwork.getViewsCount());

            // Increment views count
            if (artwork.getViewsCount() == null) {
                System.out.println("WARNING: viewsCount is null, setting to 0");
                artwork.setViewsCount(0L);
            }
            artwork.setViewsCount(artwork.getViewsCount() + 1);
            artworkRepository.save(artwork);
            System.out.println("Views count updated to: " + artwork.getViewsCount());

            System.out.println("Fetching author with ID: " + artwork.getAuthorId());
            
            User author = null;
            try {
                author = getUserById(artwork.getAuthorId());
                System.out.println("Author found: " + author.getUsername());
            } catch (Exception e) {
                System.err.println("ERROR: Failed to get author: " + e.getMessage());
                e.printStackTrace();
                throw new ResourceNotFoundException("Author not found for artwork");
            }
            
            Long userId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;

            // Check if user purchased this artwork
            boolean isPurchased = userId != null && 
                    orderRepository.existsByUserIdAndArtworkIdAndStatus(userId, id, OrderStatus.COMPLETED);

            // Get comments
            System.out.println("Getting comments...");
            List<Comment> comments = commentRepository.findByArtworkIdOrderByCreatedAtDesc(id);
            System.out.println("Found " + comments.size() + " comments");
            
            List<CommentDto> commentDtos = comments.stream()
                    .map(this::mapToCommentDto)
                    .collect(Collectors.toList());

            // Check if favorited
            boolean isFavorited = userId != null && 
                    favoriteRepository.findByIdUserIdAndIdArtworkId(userId, id).isPresent();

            long favoritesCount = favoriteRepository.countByIdArtworkId(id);

            String imageUrl = isPurchased ? artwork.getImageUrl() : artwork.getPreviewUrl();

            System.out.println("Building response DTO...");
            ArtworkDetailDto result = ArtworkDetailDto.builder()
                    .id(artwork.getId())
                    .authorId(artwork.getAuthorId())
                    .authorName(author.getUsername())
                    .title(artwork.getTitle())
                    .description(artwork.getDescription())
                    .price(artwork.getPrice())
                    .imageUrl(imageUrl)
                    .isSold(artwork.getIsSold())
                    .isPurchased(isPurchased)
                    .favoritesCount(favoritesCount)
                    .viewsCount(artwork.getViewsCount())
                    .isFavorited(isFavorited)
                    .comments(commentDtos)
                    .createdAt(artwork.getCreatedAt())
                    .build();
            
            System.out.println("=== Successfully built artwork detail ===");
            return result;
        } catch (Exception e) {
            System.err.println("=== ERROR in getArtworkDetail ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    public byte[] getArtworkImage(Long id, String userEmail) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        User user = userService.getUserByEmail(userEmail);

        // Check if user purchased this artwork
        boolean isPurchased = orderRepository.existsByUserIdAndArtworkIdAndStatus(
                user.getId(), id, OrderStatus.COMPLETED);

        if (!isPurchased) {
            throw new BusinessException("You must purchase this artwork to access the original image");
        }

        return artwork.getImageData();
    }

    public byte[] getArtworkPreview(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        return artwork.getPreviewData();
    }

    @Transactional
    public CommentDto addComment(Long artworkId, CreateCommentRequest request, String userEmail) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        User user = userService.getUserByEmail(userEmail);

        Comment comment = Comment.builder()
                .userId(user.getId())
                .artworkId(artworkId)
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);

        return mapToCommentDto(comment);
    }

    @Transactional
    public void toggleFavorite(Long artworkId, String userEmail) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        User user = userService.getUserByEmail(userEmail);

        Favorite.FavoriteId favoriteId = new Favorite.FavoriteId(user.getId(), artworkId);
        
        if (favoriteRepository.findByIdUserIdAndIdArtworkId(user.getId(), artworkId).isPresent()) {
            favoriteRepository.deleteById(favoriteId);
        } else {
            Favorite favorite = Favorite.builder()
                    .id(favoriteId)
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    public List<ArtworkDto> getFavoriteArtworks(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Favorite> favorites = favoriteRepository.findByIdUserId(user.getId());

        return favorites.stream()
                .map(favorite -> {
                    Artwork artwork = artworkRepository.findById(favorite.getId().getArtworkId())
                            .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));
                    User author = getUserById(artwork.getAuthorId());
                    return mapToArtworkDto(artwork, author.getUsername(), user.getId());
                })
                .collect(Collectors.toList());
    }
    
    public List<ArtworkDto> getArtworksByArtist(Long artistId, String userEmail) {
        List<Artwork> artworks = artworkRepository.findByAuthorId(artistId);
        Long userId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;
        
        String artistName = "Unknown Artist";
        try {
            User artist = getUserById(artistId);
            artistName = artist.getUsername();
        } catch (Exception e) {
            System.err.println("Failed to get artist " + artistId + ": " + e.getMessage());
        }
        
        final String finalArtistName = artistName;
        return artworks.stream()
                .map(artwork -> mapToArtworkDto(artwork, finalArtistName, userId))
                .collect(Collectors.toList());
    }

    private byte[] generatePreviewWithWatermark(byte[] originalImageData) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageData));

        // Resize to preview size (max 800px width)
        BufferedImage resizedImage = Thumbnails.of(originalImage)
                .size(800, 800)
                .asBufferedImage();

        // Add watermark
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set watermark properties
        String watermarkText = "PREVIEW";
        Font font = new Font("Arial", Font.BOLD, 60);
        g2d.setFont(font);
        g2d.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white

        // Calculate position (center)
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int x = (resizedImage.getWidth() - fontMetrics.stringWidth(watermarkText)) / 2;
        int y = (resizedImage.getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();

        // Draw watermark
        g2d.drawString(watermarkText, x, y);
        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        return baos.toByteArray();
    }

    private ArtworkDto mapToArtworkDto(Artwork artwork, String authorName, Long currentUserId) {
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

    private CommentDto mapToCommentDto(Comment comment) {
        User user = null;
        try {
            user = getUserById(comment.getUserId());
        } catch (Exception e) {
            System.err.println("Failed to get user for comment: " + e.getMessage());
            // Return comment with unknown user
            return CommentDto.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .username("Unknown User")
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
        
        return CommentDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .username(user.getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private User getUserById(Long userId) {
        return userService.getUserById(userId);
    }
}
