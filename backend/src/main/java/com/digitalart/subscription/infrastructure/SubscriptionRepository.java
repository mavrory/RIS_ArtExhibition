package com.digitalart.subscription.infrastructure;

import com.digitalart.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    Optional<Subscription> findBySubscriberIdAndArtistId(Long subscriberId, Long artistId);
    
    List<Subscription> findBySubscriberId(Long subscriberId);
    
    List<Subscription> findByArtistId(Long artistId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.artistId = :artistId")
    Long countByArtistId(@Param("artistId") Long artistId);
    
    boolean existsBySubscriberIdAndArtistId(Long subscriberId, Long artistId);
    
    void deleteBySubscriberIdAndArtistId(Long subscriberId, Long artistId);
}
