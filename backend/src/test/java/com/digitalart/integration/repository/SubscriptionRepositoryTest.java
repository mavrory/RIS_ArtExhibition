package com.digitalart.integration.repository;

import com.digitalart.subscription.domain.Subscription;
import com.digitalart.subscription.infrastructure.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void saveAndFindBySubscriberIdAndArtistId() {
        Subscription saved = subscriptionRepository.save(new Subscription(1L, 2L));

        Optional<Subscription> found = subscriptionRepository.findBySubscriberIdAndArtistId(1L, 2L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getSubscriberId());
        assertEquals(2L, found.get().getArtistId());
    }

    @Test
    void existsBySubscriberIdAndArtistId_shouldReturnTrue() {
        subscriptionRepository.save(new Subscription(1L, 2L));

        boolean exists = subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L);

        assertTrue(exists);
    }

    @Test
    void existsBySubscriberIdAndArtistId_shouldReturnFalse() {
        boolean exists = subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L);

        assertFalse(exists);
    }

    @Test
    void findBySubscriberId_shouldReturnSubscriptions() {
        subscriptionRepository.save(new Subscription(1L, 2L));
        subscriptionRepository.save(new Subscription(1L, 3L));

        List<Subscription> result = subscriptionRepository.findBySubscriberId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findByArtistId_shouldReturnSubscribers() {
        subscriptionRepository.save(new Subscription(1L, 3L));
        subscriptionRepository.save(new Subscription(2L, 3L));

        List<Subscription> result = subscriptionRepository.findByArtistId(3L);

        assertEquals(2, result.size());
    }

    @Test
    void deleteBySubscriberIdAndArtistId_shouldDelete() {
        subscriptionRepository.save(new Subscription(1L, 2L));

        subscriptionRepository.deleteBySubscriberIdAndArtistId(1L, 2L);

        boolean exists = subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L);
        assertFalse(exists);
    }

    @Test
    void countByArtistId_shouldReturnCorrectCount() {
        subscriptionRepository.save(new Subscription(1L, 3L));
        subscriptionRepository.save(new Subscription(2L, 3L));
        subscriptionRepository.save(new Subscription(4L, 5L));

        Long count = subscriptionRepository.countByArtistId(3L);

        assertEquals(2L, count);
    }
}
