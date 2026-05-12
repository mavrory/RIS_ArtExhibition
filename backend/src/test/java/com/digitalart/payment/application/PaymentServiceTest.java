package com.digitalart.payment.application;

import com.digitalart.order.application.OrderService;
import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.payment.application.dto.PaymentDto;
import com.digitalart.payment.application.dto.ProcessPaymentRequest;
import com.digitalart.payment.domain.Payment;
import com.digitalart.payment.domain.PaymentStatus;
import com.digitalart.payment.infrastructure.PaymentRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import com.digitalart.wallet.application.UserWalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserWalletService walletService;

    @InjectMocks
    private PaymentService paymentService;

    private User createBuyer() {
        return User.builder()
                .id(1L)
                .email("buyer@example.com")
                .username("buyer")
                .build();
    }

    private Order createOrder(Long id, Long userId, Long artworkId, BigDecimal price) {
        return Order.builder()
                .id(id)
                .userId(userId)
                .artworkId(artworkId)
                .status(OrderStatus.PENDING)
                .totalPrice(price)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void processPayment_shouldSucceed() {
        User buyer = createBuyer();
        Order order = createOrder(1L, 1L, 10L, new BigDecimal("100.00"));
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(walletService.getBalance(1L)).thenReturn(new BigDecimal("200.00"));

        Payment savedPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod("WALLET")
                .transactionId("TXN-test")
                .amount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentDto result = paymentService.processPayment(request, "buyer@example.com");

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS.name(), result.getStatus());
        verify(walletService).deductForPurchase(eq(1L), eq(new BigDecimal("100.00")), eq(1L), anyString());
        verify(orderService).completeOrder(1L);
    }

    @Test
    void processPayment_withWrongUser_shouldThrowException() {
        User buyer = createBuyer();
        Order order = createOrder(1L, 2L, 10L, new BigDecimal("100.00"));
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(request, "buyer@example.com"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_withDuplicate_shouldThrowException() {
        User buyer = createBuyer();
        Order order = createOrder(1L, 1L, 10L, new BigDecimal("100.00"));
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(mock(Payment.class)));

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(request, "buyer@example.com"));
    }

    @Test
    void processPayment_withInsufficientBalance_shouldThrowException() {
        User buyer = createBuyer();
        Order order = createOrder(1L, 1L, 10L, new BigDecimal("100.00"));
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(walletService.getBalance(1L)).thenReturn(new BigDecimal("50.00"));

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(request, "buyer@example.com"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_withNonExistentOrder_shouldThrowException() {
        User buyer = createBuyer();
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(999L);

        when(userService.getUserByEmail("buyer@example.com")).thenReturn(buyer);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processPayment(request, "buyer@example.com"));
    }
}
