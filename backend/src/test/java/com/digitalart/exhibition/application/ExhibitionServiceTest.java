package com.digitalart.exhibition.application;

import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.artwork.infrastructure.FavoriteRepository;
import com.digitalart.exhibition.application.dto.AddArtworkRequest;
import com.digitalart.exhibition.application.dto.CreateExhibitionRequest;
import com.digitalart.exhibition.application.dto.ExhibitionDetailDto;
import com.digitalart.exhibition.application.dto.ExhibitionDto;
import com.digitalart.exhibition.domain.Exhibition;
import com.digitalart.exhibition.infrastructure.ExhibitionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExhibitionServiceTest {

    @Mock
    private ExhibitionRepository exhibitionRepository;

    @Mock
    private ArtworkRepository artworkRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExhibitionService exhibitionService;

    private User createArtist() {
        Role artistRole = new Role();
        artistRole.setName("ARTIST");
        return User.builder()
                .id(1L)
                .email("artist@example.com")
                .username("artistuser")
                .roles(Set.of(artistRole))
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

    private Exhibition createExhibition(Long id, Long createdBy) {
        Exhibition exhibition = Exhibition.builder()
                .id(id)
                .title("Test Exhibition")
                .description("Exhibition description")
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .artworks(new HashSet<>())
                .build();
        return exhibition;
    }

    @Test
    void createExhibition_withArtistRole_shouldSucceed() {
        User artist = createArtist();
        CreateExhibitionRequest request = new CreateExhibitionRequest();
        request.setTitle("New Exhibition");
        request.setDescription("Description");

        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);

        Exhibition savedExhibition = createExhibition(1L, 1L);
        when(exhibitionRepository.save(any(Exhibition.class))).thenAnswer(invocation -> {
            Exhibition saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        ExhibitionDto result = exhibitionService.createExhibition(request, "artist@example.com");

        assertNotNull(result);
        assertEquals("New Exhibition", result.getTitle());
        assertEquals("artistuser", result.getCreatorName());
    }

    @Test
    void createExhibition_withoutArtistRole_shouldThrowException() {
        User regularUser = createRegularUser();
        CreateExhibitionRequest request = new CreateExhibitionRequest();

        when(userService.getUserByEmail("user@example.com")).thenReturn(regularUser);

        assertThrows(BusinessException.class,
                () -> exhibitionService.createExhibition(request, "user@example.com"));
        verify(exhibitionRepository, never()).save(any());
    }

    @Test
    void getAllExhibitions_shouldReturnList() {
        Exhibition exhibition1 = createExhibition(1L, 1L);
        Exhibition exhibition2 = createExhibition(2L, 1L);
        User artist = createArtist();

        when(exhibitionRepository.findAll()).thenReturn(List.of(exhibition1, exhibition2));
        when(userService.getUserById(1L)).thenReturn(artist);

        List<ExhibitionDto> result = exhibitionService.getAllExhibitions();

        assertEquals(2, result.size());
    }

    @Test
    void getExhibitionDetail_shouldReturnDetail() {
        Exhibition exhibition = createExhibition(1L, 1L);
        User artist = createArtist();

        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(exhibition));
        when(userService.getUserById(1L)).thenReturn(artist);
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);

        ExhibitionDetailDto result = exhibitionService.getExhibitionDetail(1L, "artist@example.com");

        assertNotNull(result);
        assertEquals("Test Exhibition", result.getTitle());
        assertEquals("artistuser", result.getCreatorName());
    }

    @Test
    void getExhibitionDetail_withNonExistentId_shouldThrowException() {
        when(exhibitionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exhibitionService.getExhibitionDetail(999L, "artist@example.com"));
    }

    @Test
    void addArtworkToExhibition_byCreator_shouldSucceed() {
        Exhibition exhibition = createExhibition(1L, 1L);
        User artist = createArtist();
        Artwork artwork = Artwork.builder()
                .id(10L)
                .authorId(1L)
                .title("Artwork")
                .price(new BigDecimal("50"))
                .build();
        AddArtworkRequest request = new AddArtworkRequest();
        request.setArtworkId(10L);

        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(exhibition));
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);
        when(artworkRepository.findById(10L)).thenReturn(Optional.of(artwork));

        exhibitionService.addArtworkToExhibition(1L, request, "artist@example.com");

        assertTrue(exhibition.getArtworks().contains(artwork));
        verify(exhibitionRepository).save(exhibition);
    }

    @Test
    void addArtworkToExhibition_byNonCreator_shouldThrowException() {
        Exhibition exhibition = createExhibition(1L, 1L);
        User otherUser = createRegularUser();
        AddArtworkRequest request = new AddArtworkRequest();
        request.setArtworkId(10L);

        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(exhibition));
        when(userService.getUserByEmail("user@example.com")).thenReturn(otherUser);

        assertThrows(BusinessException.class,
                () -> exhibitionService.addArtworkToExhibition(1L, request, "user@example.com"));
        verify(exhibitionRepository, never()).save(any());
    }

    @Test
    void addArtworkToExhibition_withNonExistentArtwork_shouldThrowException() {
        Exhibition exhibition = createExhibition(1L, 1L);
        User artist = createArtist();
        AddArtworkRequest request = new AddArtworkRequest();
        request.setArtworkId(999L);

        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(exhibition));
        when(userService.getUserByEmail("artist@example.com")).thenReturn(artist);
        when(artworkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exhibitionService.addArtworkToExhibition(1L, request, "artist@example.com"));
    }
}
