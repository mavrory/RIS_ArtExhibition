package com.digitalart.artwork.application;

import com.digitalart.artwork.application.dto.ArtworkDetailDto;
import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.artwork.application.dto.CommentDto;
import com.digitalart.artwork.application.dto.CreateArtworkRequest;
import com.digitalart.artwork.application.dto.CreateCommentRequest;
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
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtworkServiceTest {

    @Mock
    private ArtworkRepository artworkRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ArtworkService artworkService;

    private User createArtistUser() {
        Role artistRole = new Role();
        artistRole.setName("ARTIST");
        Role userRole = new Role();
        userRole.setName("USER");
        return User.builder()
                .id(1L)
                .email("artist@example.com")
                .username("artistuser")
                .roles(Set.of(artistRole, userRole))
                .build();
    }

    private User createRegularUser() {
        Role userRole = new Role();
        userRole.setName("USER");
        return User.builder()
                .id(2L)
                .email("user@example.com")
                .username("regularuser")
                .roles(Set.of(userRole))
                .build();
    }

    private Artwork createArtwork(Long id, Long authorId, boolean isSold) {
        return Artwork.builder()
                .id(id)
                .authorId(authorId)
                .title("Test Artwork")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .imageUrl("/api/artworks/" + id + "/image")
                .previewUrl("/api/artworks/" + id + "/preview")
                .isSold(isSold)
                .viewsCount(0L)
                .createdAt(LocalDateTime.now())
                .imageData(new byte[]{1, 2, 3})
                .previewData(new byte[]{4, 5, 6})
                .build();
    }

    @Test
    void createArtwork_withArtistRole_shouldSucceed() throws IOException {
        CreateArtworkRequest request = new CreateArtworkRequest();
        request.setTitle("Test Artwork");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("100.00"));

        User artist = createArtistUser();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", baos.toByteArray());
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);

        Artwork savedArtwork = createArtwork(1L, 1L, false);
        when(artworkRepository.save(any(Artwork.class))).thenReturn(savedArtwork, savedArtwork);

        ArtworkDto result = artworkService.createArtwork(request, imageFile, "artist@example.com");

        assertNotNull(result);
        assertEquals("Test Artwork", result.getTitle());
        assertEquals("artistuser", result.getAuthorName());
        verify(artworkRepository, times(2)).save(any(Artwork.class));
    }

    @Test
    void createArtwork_withoutArtistRole_shouldThrowException() {
        CreateArtworkRequest request = new CreateArtworkRequest();
        User regularUser = createRegularUser();
        MultipartFile imageFile = mock(MultipartFile.class);

        when(userService.getUserByEmail("user@example.com")).thenReturn(regularUser);

        assertThrows(BusinessException.class,
                () -> artworkService.createArtwork(request, imageFile, "user@example.com"));
        verify(artworkRepository, never()).save(any());
    }

    @Test
    void getAllArtworks_shouldReturnList() {
        Artwork artwork1 = createArtwork(1L, 1L, false);
        Artwork artwork2 = createArtwork(2L, 1L, true);
        when(artworkRepository.findAll()).thenReturn(List.of(artwork1, artwork2));
        when(userService.getUserByEmail("artist@example.com")).thenReturn(createArtistUser());

        User artist = createArtistUser();
        when(userService.getUserById(1L)).thenReturn(artist);
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(any(), any())).thenReturn(Optional.empty());
        when(favoriteRepository.countByIdArtworkId(any())).thenReturn(0L);

        List<ArtworkDto> result = artworkService.getAllArtworks("artist@example.com");

        assertEquals(2, result.size());
    }

    @Test
    void getArtworkDetail_shouldIncrementViews() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User artist = createArtistUser();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserById(1L)).thenReturn(artist);
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);
        when(orderRepository.existsByUserIdAndArtworkIdAndStatus(1L, 1L, OrderStatus.COMPLETED)).thenReturn(false);
        when(commentRepository.findByArtworkIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(1L, 1L)).thenReturn(Optional.empty());
        when(favoriteRepository.countByIdArtworkId(1L)).thenReturn(0L);

        ArtworkDetailDto result = artworkService.getArtworkDetail(1L, "artist@example.com");

        assertEquals(1L, result.getViewsCount());
        verify(artworkRepository).save(any(Artwork.class));
    }

    @Test
    void getArtworkDetail_withNonExistentId_shouldThrowException() {
        when(artworkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> artworkService.getArtworkDetail(999L, "artist@example.com"));
    }

    @Test
    void getArtworkImage_withoutPurchase_shouldThrowException() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User user = createRegularUser();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(orderRepository.existsByUserIdAndArtworkIdAndStatus(2L, 1L, OrderStatus.COMPLETED)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> artworkService.getArtworkImage(1L, "user@example.com"));
    }

    @Test
    void getArtworkImage_withPurchase_shouldReturnImageData() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User user = createRegularUser();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(orderRepository.existsByUserIdAndArtworkIdAndStatus(2L, 1L, OrderStatus.COMPLETED)).thenReturn(true);

        byte[] result = artworkService.getArtworkImage(1L, "user@example.com");

        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void getArtworkPreview_shouldReturnPreviewData() {
        Artwork artwork = createArtwork(1L, 1L, false);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));

        byte[] result = artworkService.getArtworkPreview(1L);

        assertArrayEquals(new byte[]{4, 5, 6}, result);
    }

    @Test
    void addComment_shouldSucceed() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User user = createRegularUser();
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Nice artwork!");

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);

        Comment savedComment = Comment.builder()
                .id(1L)
                .userId(2L)
                .artworkId(1L)
                .content("Nice artwork!")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(userService.getUserById(2L)).thenReturn(user);

        CommentDto result = artworkService.addComment(1L, request, "user@example.com");

        assertNotNull(result);
        assertEquals("Nice artwork!", result.getContent());
        assertEquals("regularuser", result.getUsername());
    }

    @Test
    void toggleFavorite_whenNotFavorited_shouldAdd() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User user = createRegularUser();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(2L, 1L)).thenReturn(Optional.empty());

        artworkService.toggleFavorite(1L, "user@example.com");

        verify(favoriteRepository).save(any(Favorite.class));
        verify(favoriteRepository, never()).deleteById(any());
    }

    @Test
    void toggleFavorite_whenAlreadyFavorited_shouldRemove() {
        Artwork artwork = createArtwork(1L, 1L, false);
        User user = createRegularUser();
        Favorite.FavoriteId favId = new Favorite.FavoriteId(2L, 1L);
        Favorite favorite = Favorite.builder().id(favId).build();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(2L, 1L)).thenReturn(Optional.of(favorite));

        artworkService.toggleFavorite(1L, "user@example.com");

        verify(favoriteRepository).deleteById(favId);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void getFavoriteArtworks_shouldReturnList() {
        User user = createRegularUser();
        Artwork artwork = createArtwork(1L, 1L, false);
        User artist = createArtistUser();
        Favorite.FavoriteId favId = new Favorite.FavoriteId(2L, 1L);
        Favorite favorite = Favorite.builder().id(favId).build();

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(favoriteRepository.findByIdUserId(2L)).thenReturn(List.of(favorite));
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserById(1L)).thenReturn(artist);
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(2L, 1L)).thenReturn(Optional.of(favorite));
        when(favoriteRepository.countByIdArtworkId(1L)).thenReturn(1L);

        List<ArtworkDto> result = artworkService.getFavoriteArtworks("user@example.com");

        assertEquals(1, result.size());
    }

    @Test
    void getArtworksByArtist_shouldReturnList() {
        Artwork artwork1 = createArtwork(1L, 1L, false);
        Artwork artwork2 = createArtwork(2L, 1L, false);
        User artist = createArtistUser();

        when(artworkRepository.findByAuthorId(1L)).thenReturn(List.of(artwork1, artwork2));
        when(userService.getUserById(1L)).thenReturn(artist);
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);
        when(favoriteRepository.findByIdUserIdAndIdArtworkId(any(), any())).thenReturn(Optional.empty());
        when(favoriteRepository.countByIdArtworkId(any())).thenReturn(0L);

        List<ArtworkDto> result = artworkService.getArtworksByArtist(1L, "artist@example.com");

        assertEquals(2, result.size());
    }
}
