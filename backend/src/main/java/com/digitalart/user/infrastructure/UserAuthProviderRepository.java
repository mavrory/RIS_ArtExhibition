package com.digitalart.user.infrastructure;

import com.digitalart.user.domain.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
    
    Optional<UserAuthProvider> findByProviderAndProviderUserId(
        UserAuthProvider.AuthProvider provider, 
        String providerUserId
    );
    
    Optional<UserAuthProvider> findByUserId(Long userId);
}
