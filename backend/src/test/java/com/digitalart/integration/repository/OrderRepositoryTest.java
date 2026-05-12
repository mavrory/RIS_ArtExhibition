package com.digitalart.integration.repository;

import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order createOrder(Long userId, Long artworkId, OrderStatus status) {
        return Order.builder()
                .userId(userId)
                .artworkId(artworkId)
                .status(status)
                .totalPrice(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnOrders() {
        orderRepository.save(createOrder(1L, 1L, OrderStatus.PENDING));
        orderRepository.save(createOrder(1L, 2L, OrderStatus.COMPLETED));
        orderRepository.save(createOrder(2L, 3L, OrderStatus.PENDING));

        List<Order> result = orderRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertEquals(2, result.size());
    }

    @Test
    void existsByUserIdAndArtworkIdAndStatus_shouldReturnTrue_whenExists() {
        orderRepository.save(createOrder(1L, 1L, OrderStatus.COMPLETED));

        boolean exists = orderRepository.existsByUserIdAndArtworkIdAndStatus(1L, 1L, OrderStatus.COMPLETED);

        assertTrue(exists);
    }

    @Test
    void existsByUserIdAndArtworkIdAndStatus_shouldReturnFalse_whenNotExists() {
        orderRepository.save(createOrder(1L, 1L, OrderStatus.PENDING));

        boolean exists = orderRepository.existsByUserIdAndArtworkIdAndStatus(1L, 1L, OrderStatus.COMPLETED);

        assertFalse(exists);
    }

    @Test
    void saveAndFindById_shouldWork() {
        Order saved = orderRepository.save(createOrder(1L, 1L, OrderStatus.PENDING));

        Order found = orderRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(OrderStatus.PENDING, found.getStatus());
    }
}
