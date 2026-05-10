package com.digitalart.wallet.presentation;

import com.digitalart.wallet.application.UserWalletService;
import com.digitalart.wallet.application.dto.DepositRequest;
import com.digitalart.wallet.application.dto.WalletTransactionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final UserWalletService walletService;
    private final com.digitalart.user.application.UserService userService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        BigDecimal balance = walletService.getBalance(userId);
        
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletTransactionDto> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        
        WalletTransactionDto transaction = walletService.deposit(userId, request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionDto>> getTransactions(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        
        List<WalletTransactionDto> transactions = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<WalletTransactionDto>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        
        List<WalletTransactionDto> transactions = walletService.getRecentTransactions(userId, limit);
        return ResponseEntity.ok(transactions);
    }
}
