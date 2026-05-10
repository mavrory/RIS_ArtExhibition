package com.digitalart.wallet.application;

import com.digitalart.shared.exception.BusinessException;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.UserRepository;
import com.digitalart.wallet.application.dto.DepositRequest;
import com.digitalart.wallet.application.dto.WalletTransactionDto;
import com.digitalart.wallet.domain.TransactionType;
import com.digitalart.wallet.domain.WalletTransaction;
import com.digitalart.wallet.infrastructure.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWalletService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public WalletTransactionDto deposit(Long userId, DepositRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be positive");
        }

        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            throw new BusinessException("Maximum deposit amount is 10000");
        }

        BigDecimal balanceBefore = BigDecimal.valueOf(user.getBalance());
        BigDecimal newBalance = balanceBefore.add(request.getAmount());
        
        user.setBalance(newBalance.doubleValue());
        userRepository.save(user);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .description("Deposit via " + request.getPaymentMethod())
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .build();

        transaction = walletTransactionRepository.save(transaction);

        log.info("User {} deposited {} to wallet. New balance: {}", userId, request.getAmount(), newBalance);

        return mapToDto(transaction);
    }

    @Transactional
    public WalletTransactionDto deductForPurchase(Long userId, BigDecimal amount, Long orderId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        BigDecimal balanceBefore = BigDecimal.valueOf(user.getBalance());

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance. Current balance: " + balanceBefore + ", Required: " + amount);
        }

        BigDecimal newBalance = balanceBefore.subtract(amount);
        user.setBalance(newBalance.doubleValue());
        userRepository.save(user);

        WalletTransaction transaction = WalletTransaction.builder()
                .userId(userId)
                .amount(amount.negate())
                .transactionType(TransactionType.PURCHASE)
                .description(description)
                .orderId(orderId)
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .build();

        transaction = walletTransactionRepository.save(transaction);

        log.info("User {} purchased for {}. New balance: {}", userId, amount, newBalance);

        return mapToDto(transaction);
    }

    @Transactional
    public WalletTransactionDto addArtistEarning(Long artistId, BigDecimal amount, Long orderId, String description) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new BusinessException("Artist not found"));

        BigDecimal balanceBefore = BigDecimal.valueOf(artist.getBalance());
        BigDecimal newBalance = balanceBefore.add(amount);
        
        artist.setBalance(newBalance.doubleValue());
        userRepository.save(artist);

        WalletTransaction transaction = WalletTransaction.builder()
                .userId(artistId)
                .amount(amount)
                .transactionType(TransactionType.ARTIST_EARNING)
                .description(description)
                .orderId(orderId)
                .balanceBefore(balanceBefore)
                .balanceAfter(newBalance)
                .build();

        transaction = walletTransactionRepository.save(transaction);

        log.info("Artist {} earned {}. New balance: {}", artistId, amount, newBalance);

        return mapToDto(transaction);
    }

    public List<WalletTransactionDto> getTransactionHistory(Long userId) {
        List<WalletTransaction> transactions = walletTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<WalletTransactionDto> getRecentTransactions(Long userId, int limit) {
        List<WalletTransaction> transactions = walletTransactionRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(userId);

        return transactions.stream()
                .limit(limit)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BigDecimal getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return BigDecimal.valueOf(user.getBalance());
    }

    private WalletTransactionDto mapToDto(WalletTransaction transaction) {
        return WalletTransactionDto.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .description(transaction.getDescription())
                .orderId(transaction.getOrderId())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
