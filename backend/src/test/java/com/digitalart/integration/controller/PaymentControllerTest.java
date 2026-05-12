package com.digitalart.integration.controller;

import com.digitalart.payment.application.PaymentService;
import com.digitalart.payment.application.dto.PaymentDto;
import com.digitalart.payment.application.dto.ProcessPaymentRequest;
import com.digitalart.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void processPayment_shouldSucceed() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        PaymentDto dto = PaymentDto.builder()
                .id(1L).orderId(1L)
                .status("SUCCESS").paymentMethod("WALLET")
                .transactionId("TXN-test")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class), anyString()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser
    void processPayment_withInsufficientFunds_shouldReturn400() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setOrderId(1L);

        when(paymentService.processPayment(any(ProcessPaymentRequest.class), anyString()))
                .thenThrow(new BusinessException("Insufficient wallet balance"));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
