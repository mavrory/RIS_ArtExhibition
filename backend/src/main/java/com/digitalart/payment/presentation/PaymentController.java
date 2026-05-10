package com.digitalart.payment.presentation;

import com.digitalart.payment.application.PaymentService;
import com.digitalart.payment.application.dto.PaymentDto;
import com.digitalart.payment.application.dto.ProcessPaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PaymentDto payment = paymentService.processPayment(request, email);
        return ResponseEntity.ok(payment);
    }
}
