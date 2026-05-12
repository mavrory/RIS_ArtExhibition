package com.digitalart.integration.controller;

import com.digitalart.shared.exception.BusinessException;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import com.digitalart.wallet.application.UserWalletService;
import com.digitalart.wallet.application.dto.DepositRequest;
import com.digitalart.wallet.application.dto.WalletTransactionDto;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserWalletService walletService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User createUser() {
        return User.builder().id(1L).email("user@example.com").username("user").build();
    }

    @Test
    @WithMockUser
    void getBalance_shouldReturnBalance() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(walletService.getBalance(1L)).thenReturn(new BigDecimal("250.00"));

        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250.0));
    }

    @Test
    @WithMockUser
    void deposit_shouldSucceed() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("CREDIT_CARD");

        WalletTransactionDto dto = WalletTransactionDto.builder()
                .id(1L).userId(1L)
                .amount(new BigDecimal("100.00"))
                .transactionType("DEPOSIT")
                .description("Deposit via CREDIT_CARD")
                .balanceBefore(new BigDecimal("0"))
                .balanceAfter(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(walletService.deposit(eq(1L), any(DepositRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));
    }

    @Test
    @WithMockUser
    void deposit_exceedingLimit_shouldReturn400() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("999999"));
        request.setPaymentMethod("CREDIT_CARD");

        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(walletService.deposit(eq(1L), any(DepositRequest.class)))
                .thenThrow(new BusinessException("Maximum deposit amount is 10000"));

        mockMvc.perform(post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getTransactions_shouldReturnList() throws Exception {
        WalletTransactionDto dto = WalletTransactionDto.builder()
                .id(1L).userId(1L)
                .amount(new BigDecimal("100"))
                .transactionType("DEPOSIT")
                .description("test")
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(walletService.getTransactionHistory(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/wallet/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getRecentTransactions_shouldReturnLimitedList() throws Exception {
        WalletTransactionDto dto = WalletTransactionDto.builder()
                .id(1L).userId(1L)
                .amount(new BigDecimal("100"))
                .transactionType("DEPOSIT")
                .description("test")
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("100"))
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(walletService.getRecentTransactions(eq(1L), eq(5))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/wallet/transactions/recent?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
