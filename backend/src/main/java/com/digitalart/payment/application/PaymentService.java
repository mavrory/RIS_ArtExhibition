package com.digitalart.payment.application;

import com.digitalart.order.application.OrderService;
import com.digitalart.order.domain.Order;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.payment.application.dto.PaymentDto;
import com.digitalart.payment.application.dto.ProcessPaymentRequest;
import com.digitalart.payment.domain.Payment;
import com.digitalart.payment.domain.PaymentMethod;
import com.digitalart.payment.domain.PaymentStatus;
import com.digitalart.payment.infrastructure.PaymentRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import com.digitalart.wallet.application.UserWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final UserWalletService walletService;

    @Transactional
    public PaymentDto processPayment(ProcessPaymentRequest request, String userEmail) {
        // Verify order belongs to user
        User user = userService.getUserByEmail(userEmail);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("You don't have access to this order");
        }

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BusinessException("Payment already processed for this order");
        }

        BigDecimal amount = order.getTotalPrice();

        // Only allow payment from wallet
        BigDecimal currentBalance = walletService.getBalance(user.getId());
        
        if (currentBalance.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient wallet balance. Current balance: $" + 
                currentBalance + ", Required: $" + amount + ". Please add funds to your wallet.");
        }

        // Deduct from buyer's wallet
        walletService.deductForPurchase(
            user.getId(), 
            amount, 
            order.getId(), 
            "Purchase of artwork (Order #" + order.getId() + ")"
        );

        log.info("Payment via WALLET: User {} paid {} from wallet", user.getId(), amount);

        // Generate transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString();

        // Create payment record with SUCCESS status
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(PaymentMethod.WALLET.name())
                .transactionId(transactionId)
                .amount(amount)
                .build();

        payment = paymentRepository.save(payment);

        // Complete the order (this will also credit the artist)
        orderService.completeOrder(request.getOrderId());

        log.info("Payment processed successfully: {}", transactionId);

        return mapToPaymentDto(payment);
    }

    private PaymentDto mapToPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
