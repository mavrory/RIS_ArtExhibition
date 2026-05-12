package com.digitalart.admin.application;

import com.digitalart.admin.application.dto.DetailedStatisticsDto;
import com.digitalart.admin.application.dto.PlatformStatisticsDto;
import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.domain.Comment;
import com.digitalart.artwork.domain.Favorite;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.artwork.infrastructure.CommentRepository;
import com.digitalart.artwork.infrastructure.FavoriteRepository;
import com.digitalart.exhibition.infrastructure.ExhibitionRepository;
import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.subscription.infrastructure.SubscriptionRepository;
import com.digitalart.user.application.dto.UserDto;
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private ExhibitionRepository exhibitionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private AdminService adminService;

    private User createUser(Long id, String name, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return User.builder()
                .id(id)
                .email(name + "@test.com")
                .username(name)
                .balance(0.0)
                .roles(new HashSet<>(Set.of(role)))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Artwork createArtwork(Long id, Long authorId, BigDecimal price, boolean isSold, long views) {
        return Artwork.builder()
                .id(id)
                .authorId(authorId)
                .title("Artwork " + id)
                .price(price)
                .isSold(isSold)
                .viewsCount(views)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(
                createUser(1L, "user1", "USER"),
                createUser(2L, "user2", "ARTIST")
        ));

        List<UserDto> result = adminService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void getPlatformStatistics_shouldReturnStats() {
        User user1 = createUser(1L, "user1", "USER");
        User artist1 = createUser(2L, "artist1", "ARTIST");
        artist1.setBalance(100.0);
        User artist2 = createUser(3L, "artist2", "ARTIST");
        artist2.setBalance(200.0);

        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(List.of(user1, artist1, artist2));
        when(artworkRepository.count()).thenReturn(5L);
        when(exhibitionRepository.count()).thenReturn(2L);

        PlatformStatisticsDto stats = adminService.getPlatformStatistics();

        assertEquals(3, stats.getTotalUsers());
        assertEquals(2, stats.getTotalArtists());
        assertEquals(5, stats.getTotalArtworks());
        assertEquals(2, stats.getTotalExhibitions());
        assertEquals(300.0, stats.getTotalRevenue(), 0.01);
    }

    @Test
    void deleteUser_withAdminRole_shouldThrowException() {
        User admin = createUser(1L, "admin", "ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(RuntimeException.class, () -> adminService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_withNonAdmin_shouldSucceed() {
        User user = createUser(1L, "user", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void toggleUserRole_shouldAddRole_whenNotPresent() {
        User user = createUser(1L, "user", "USER");
        Role artistRole = new Role();
        artistRole.setName("ARTIST");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ARTIST")).thenReturn(Optional.of(artistRole));

        UserDto result = adminService.toggleUserRole(1L, "ARTIST");

        assertTrue(result.getRoles().contains("ARTIST"));
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserRole_shouldRemoveRole_whenPresent() {
        User user = createUser(1L, "artist", "ARTIST");
        Role artistRole = new Role();
        artistRole.setName("ARTIST");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ARTIST")).thenReturn(Optional.of(artistRole));

        UserDto result = adminService.toggleUserRole(1L, "ARTIST");

        assertFalse(result.getRoles().contains("ARTIST"));
    }

    @Test
    void deleteArtwork_withOrders_shouldThrowException() {
        Artwork artwork = createArtwork(1L, 1L, new BigDecimal("100"), false, 0);
        Order order = Order.builder().id(1L).artworkId(1L).build();

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(commentRepository.findByArtworkIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(favoriteRepository.findAll()).thenReturn(List.of());
        when(orderRepository.findAll()).thenReturn(List.of(order));

        assertThrows(BusinessException.class, () -> adminService.deleteArtwork(1L));
        verify(artworkRepository, never()).delete(any());
    }

    @Test
    void deleteArtwork_withoutOrders_shouldSucceed() {
        Artwork artwork = createArtwork(1L, 1L, new BigDecimal("100"), false, 0);

        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(commentRepository.findByArtworkIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(favoriteRepository.findAll()).thenReturn(List.of());
        when(orderRepository.findAll()).thenReturn(List.of());

        adminService.deleteArtwork(1L);

        verify(artworkRepository).delete(artwork);
    }

    @Test
    void getDetailedStatistics_shouldReturnFullStats() {
        User regular = createUser(1L, "user", "USER");
        regular.setCreatedAt(LocalDateTime.now());
        User artist = createUser(2L, "artist", "ARTIST");
        artist.setCreatedAt(LocalDateTime.now().minusDays(10));

        Artwork artwork = createArtwork(1L, 2L, new BigDecimal("100"), true, 50);

        Order completedOrder = Order.builder()
                .id(1L).userId(1L).artworkId(1L)
                .status(OrderStatus.COMPLETED)
                .totalPrice(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findAll()).thenReturn(List.of(regular, artist));
        when(artworkRepository.findAll()).thenReturn(List.of(artwork));
        when(orderRepository.findAll()).thenReturn(List.of(completedOrder));
        when(favoriteRepository.count()).thenReturn(5L);
        when(commentRepository.count()).thenReturn(10L);
        when(subscriptionRepository.count()).thenReturn(3L);
        when(exhibitionRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(2L);
        when(artworkRepository.count()).thenReturn(1L);

        DetailedStatisticsDto stats = adminService.getDetailedStatistics();

        assertEquals(2, stats.getTotalUsers());
        assertEquals(1, stats.getTotalArtists());
        assertEquals(1, stats.getTotalArtworks());
        assertEquals(1, stats.getCompletedOrders());
        assertEquals(0, new BigDecimal("100").compareTo(stats.getTotalRevenue()));
    }

    @Test
    void generateUsersReport_shouldReturnCsv() {
        User user = createUser(1L, "user", "USER");

        when(userRepository.findAll()).thenReturn(List.of(user));

        String csv = adminService.generateUsersReport();

        assertTrue(csv.contains("ID,Username,Email,Balance,Roles,Created At"));
        assertTrue(csv.contains("user"));
    }

    @Test
    void generateArtworksReport_shouldReturnCsv() {
        Artwork artwork = createArtwork(1L, 1L, new BigDecimal("100"), false, 10);
        User user = createUser(1L, "user", "USER");

        when(artworkRepository.findAll()).thenReturn(List.of(artwork));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String csv = adminService.generateArtworksReport();

        assertTrue(csv.contains("ID,Title,Artist,Price,Views,Is Sold,Created At"));
        assertTrue(csv.contains("Artwork 1"));
    }

    @Test
    void generateOrdersReport_shouldReturnCsv() {
        Order order = Order.builder()
                .id(1L).userId(1L).artworkId(1L)
                .status(OrderStatus.COMPLETED)
                .totalPrice(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();
        User user = createUser(1L, "user", "USER");
        Artwork artwork = createArtwork(1L, 1L, new BigDecimal("100"), false, 0);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));

        String csv = adminService.generateOrdersReport();

        assertTrue(csv.contains("ID,User,Artwork,Status,Total Price,Created At"));
    }

    @Test
    void updateUser_shouldUpdateBalance() {
        User user = createUser(1L, "user", "USER");
        Map<String, Object> updates = new HashMap<>();
        updates.put("balance", 250.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = adminService.updateUser(1L, updates);

        assertEquals(250.0, result.getBalance());
    }
}
