package com.digitalart.order.infrastructure;

import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    boolean existsByUserIdAndArtworkIdAndStatus(Long userId, Long artworkId, OrderStatus status);
}
