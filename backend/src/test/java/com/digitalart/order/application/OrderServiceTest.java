package com.digitalart.order.application;

import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.order.application.dto.CreateOrderRequest;
import com.digitalart.order.application.dto.OrderDto;
import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.UserRepository;
import com.digitalart.wallet.application.UserWalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ArtworkRepository artworkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserWalletService walletService;

    @InjectMocks
    private OrderService orderService;

    private User createBuyer() {
        return User.builder()
                .id(1L)
                .email("buyer@example.com")
                .username("buyer")
                .balance(500.0)
                .build();
    }

    private User createArtist() {
        return User.builder()
                .id(2L)
                .email("artist@example.com")
                .username("artist")
                .balance(100.0)
                .build();
    }

    private Artwork createArtwork(Long id, Long authorId, boolean isSold) {
        return Artwork.builder()
                .id(id)
                .authorId(authorId)
                .title("Test Artwork")
                .price(new BigDecimal("100.00"))
                .isSold(isSold)
                .build();
    }

    @Test
    void createOrder_shouldSucceed() {
        User buyer = createBuyer();
        Artwork artwork = createArtwork(1L, 2L, false);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setArtworkId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(orderRepository.existsByUserIdAndArtworkIdAndStatus(1L, 1L, OrderStatus.COMPLETED)).thenReturn(false);

        Order savedOrder = Order.builder()
                .id(10L)
                .userId(1L)
                .artworkId(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = orderService.createOrder(request, "buyer@example.com");

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING.name(), result.getStatus());
        assertEquals("Test Artwork", result.getArtworkTitle());
    }

    @Test
    void createOrder_withSoldArtwork_shouldThrowException() {
        User buyer = createBuyer();
        Artwork artwork = createArtwork(1L, 2L, true);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setArtworkId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, "buyer@example.com"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_withAlreadyPurchased_shouldThrowException() {
        User buyer = createBuyer();
        Artwork artwork = createArtwork(1L, 2L, false);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setArtworkId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(orderRepository.existsByUserIdAndArtworkIdAndStatus(1L, 1L, OrderStatus.COMPLETED)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, "buyer@example.com"));
    }

    @Test
    void getMyOrders_shouldReturnList() {
        User buyer = createBuyer();
        Order order1 = Order.builder().id(1L).userId(1L).artworkId(1L).status(OrderStatus.PENDING).totalPrice(new BigDecimal("100")).createdAt(LocalDateTime.now()).build();
        Order order2 = Order.builder().id(2L).userId(1L).artworkId(1L).status(OrderStatus.COMPLETED).totalPrice(new BigDecimal("100")).createdAt(LocalDateTime.now()).build();
        Artwork artwork = createArtwork(1L, 2L, false);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order1, order2));
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));

        List<OrderDto> result = orderService.getMyOrders("buyer@example.com");

        assertEquals(2, result.size());
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        User buyer = createBuyer();
        Order order = Order.builder().id(1L).userId(1L).artworkId(1L).status(OrderStatus.PENDING).totalPrice(new BigDecimal("100")).createdAt(LocalDateTime.now()).build();
        Artwork artwork = createArtwork(1L, 2L, false);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));

        OrderDto result = orderService.getOrderById(1L, "buyer@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_fromOtherUser_shouldThrowException() {
        User buyer = createBuyer();
        User otherUser = User.builder().id(3L).email("other@example.com").username("other").build();
        Order order = Order.builder().id(1L).userId(1L).artworkId(1L).status(OrderStatus.PENDING).build();

        when(userService.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.getOrderById(1L, "other@example.com"));
    }

    @Test
    void completeOrder_shouldTransferFundsAndMarkSold() {
        User artist = createArtist();
        Artwork artwork = createArtwork(1L, 2L, false);
        Order order = Order.builder()
                .id(1L).userId(1L).artworkId(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(userService.getUserById(1L)).thenReturn(createBuyer());
        when(userService.getUserById(2L)).thenReturn(artist);

        orderService.completeOrder(1L);

        verify(walletService).addArtistEarning(2L, new BigDecimal("100.00"), 1L,
                "Sale of artwork: Test Artwork (Order #1)");
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(artwork.getIsSold());
        verify(orderRepository).save(order);
        verify(artworkRepository).save(artwork);
    }

    @Test
    void completeOrder_withNonPendingStatus_shouldThrowException() {
        Order order = Order.builder().id(1L).status(OrderStatus.COMPLETED).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.completeOrder(1L));
    }
}
