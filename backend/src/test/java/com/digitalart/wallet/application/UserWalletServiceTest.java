package com.digitalart.wallet.application;

import com.digitalart.shared.exception.BusinessException;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.UserRepository;
import com.digitalart.wallet.application.dto.DepositRequest;
import com.digitalart.wallet.application.dto.WalletTransactionDto;
import com.digitalart.wallet.domain.TransactionType;
import com.digitalart.wallet.domain.WalletTransaction;
import com.digitalart.wallet.infrastructure.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserWalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private UserWalletService userWalletService;

    private User createUser(double balance) {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .balance(balance)
                .build();
    }

    private WalletTransaction createTransaction(Long id, Long userId, BigDecimal amount,
                                                 TransactionType type, BigDecimal before, BigDecimal after) {
        return WalletTransaction.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .transactionType(type)
                .description("test")
                .balanceBefore(before)
                .balanceAfter(after)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void deposit_shouldIncreaseBalance() {
        User user = createUser(100.0);
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setPaymentMethod("CREDIT_CARD");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransactionDto result = userWalletService.deposit(1L, request);

        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT.name(), result.getTransactionType());
        assertEquals(0, new BigDecimal("50.00").compareTo(result.getAmount()));

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(captor.capture());
        WalletTransaction saved = captor.getValue();
        assertEquals(0, new BigDecimal("100.00").compareTo(saved.getBalanceBefore()));
        assertEquals(0, new BigDecimal("150.00").compareTo(saved.getBalanceAfter()));
    }

    @Test
    void deposit_withNegativeAmount_shouldThrowException() {
        User user = createUser(100.0);
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("-50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> userWalletService.deposit(1L, request));
        verify(walletTransactionRepository, never()).save(any());
    }

    @Test
    void deposit_withZeroAmount_shouldThrowException() {
        User user = createUser(100.0);
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> userWalletService.deposit(1L, request));
    }

    @Test
    void deposit_exceedingMaxLimit_shouldThrowException() {
        User user = createUser(100.0);
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("10001.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> userWalletService.deposit(1L, request));
    }

    @Test
    void deductForPurchase_shouldDecreaseBalance() {
        User user = createUser(200.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransactionDto result = userWalletService.deductForPurchase(1L, new BigDecimal("50.00"), 1L, "Test purchase");

        assertNotNull(result);
        assertEquals(TransactionType.PURCHASE.name(), result.getTransactionType());
        assertEquals(0, new BigDecimal("-50.00").compareTo(result.getAmount()));

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(captor.capture());
        WalletTransaction saved = captor.getValue();
        assertEquals(0, new BigDecimal("200.00").compareTo(saved.getBalanceBefore()));
        assertEquals(0, new BigDecimal("150.00").compareTo(saved.getBalanceAfter()));
        assertEquals(1L, saved.getOrderId());
    }

    @Test
    void deductForPurchase_withInsufficientBalance_shouldThrowException() {
        User user = createUser(10.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class,
                () -> userWalletService.deductForPurchase(1L, new BigDecimal("50.00"), 1L, "Test"));
    }

    @Test
    void addArtistEarning_shouldIncreaseArtistBalance() {
        User artist = createUser(500.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(userRepository.save(any(User.class))).thenReturn(artist);
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletTransactionDto result = userWalletService.addArtistEarning(1L, new BigDecimal("100.00"), 1L, "Artwork sale");

        assertNotNull(result);
        assertEquals(TransactionType.ARTIST_EARNING.name(), result.getTransactionType());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getAmount()));

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(captor.capture());
        WalletTransaction saved = captor.getValue();
        assertEquals(0, new BigDecimal("600.00").compareTo(saved.getBalanceAfter()));
        assertEquals(1L, saved.getOrderId());
    }

    @Test
    void getBalance_shouldReturnCurrentBalance() {
        User user = createUser(250.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BigDecimal balance = userWalletService.getBalance(1L);

        assertEquals(0, new BigDecimal("250.00").compareTo(balance));
    }

    @Test
    void getTransactionHistory_shouldReturnAllTransactions() {
        List<WalletTransaction> transactions = List.of(
                createTransaction(1L, 1L, new BigDecimal("100"), TransactionType.DEPOSIT, BigDecimal.ZERO, new BigDecimal("100")),
                createTransaction(2L, 1L, new BigDecimal("-50"), TransactionType.PURCHASE, new BigDecimal("100"), new BigDecimal("50"))
        );

        when(walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(transactions);

        List<WalletTransactionDto> result = userWalletService.getTransactionHistory(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getRecentTransactions_shouldReturnLimitedTransactions() {
        List<WalletTransaction> transactions = List.of(
                createTransaction(1L, 1L, new BigDecimal("100"), TransactionType.DEPOSIT, BigDecimal.ZERO, new BigDecimal("100")),
                createTransaction(2L, 1L, new BigDecimal("-50"), TransactionType.PURCHASE, new BigDecimal("100"), new BigDecimal("50")),
                createTransaction(3L, 1L, new BigDecimal("200"), TransactionType.ARTIST_EARNING, new BigDecimal("50"), new BigDecimal("250"))
        );

        when(walletTransactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(transactions);

        List<WalletTransactionDto> result = userWalletService.getRecentTransactions(1L, 2);

        assertEquals(2, result.size());
    }
}
