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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserWalletService walletService;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, String userEmail) {
        User user = userService.getUserByEmail(userEmail);

        Artwork artwork = artworkRepository.findById(request.getArtworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        // Check if artwork is already sold
        if (artwork.getIsSold()) {
            throw new BusinessException("This artwork is already sold");
        }

        // Check if user already purchased this artwork
        boolean alreadyPurchased = orderRepository.existsByUserIdAndArtworkIdAndStatus(
                user.getId(), artwork.getId(), OrderStatus.COMPLETED);
        
        if (alreadyPurchased) {
            throw new BusinessException("You have already purchased this artwork");
        }

        Order order = Order.builder()
                .userId(user.getId())
                .artworkId(artwork.getId())
                .status(OrderStatus.PENDING)
                .totalPrice(artwork.getPrice())
                .build();

        order = orderRepository.save(order);

        return mapToOrderDto(order, artwork.getTitle());
    }

    public List<OrderDto> getMyOrders(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return orders.stream()
                .map(order -> {
                    Artwork artwork = artworkRepository.findById(order.getArtworkId())
                            .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));
                    return mapToOrderDto(order, artwork.getTitle());
                })
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id, String userEmail) {
        User user = userService.getUserByEmail(userEmail);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order belongs to user
        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("You don't have access to this order");
        }

        Artwork artwork = artworkRepository.findById(order.getArtworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        return mapToOrderDto(order, artwork.getTitle());
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order is not in pending status");
        }

        Artwork artwork = artworkRepository.findById(order.getArtworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        User buyer = userService.getUserById(order.getUserId());
        User artist = userService.getUserById(artwork.getAuthorId());

        // Transfer money to artist
        BigDecimal price = artwork.getPrice();
        
        // Credit artist's wallet
        walletService.addArtistEarning(
            artist.getId(),
            price,
            order.getId(),
            "Sale of artwork: " + artwork.getTitle() + " (Order #" + order.getId() + ")"
        );

        // Update order status
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // Mark artwork as sold
        artwork.setIsSold(true);
        artworkRepository.save(artwork);
    }

    private OrderDto mapToOrderDto(Order order, String artworkTitle) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .artworkId(order.getArtworkId())
                .artworkTitle(artworkTitle)
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
