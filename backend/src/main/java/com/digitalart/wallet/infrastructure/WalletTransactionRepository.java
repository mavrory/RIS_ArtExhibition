package com.digitalart.wallet.infrastructure;

import com.digitalart.wallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<WalletTransaction> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
