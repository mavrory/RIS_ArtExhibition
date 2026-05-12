package com.digitalart.subscription.application;

import com.digitalart.subscription.domain.Subscription;
import com.digitalart.subscription.infrastructure.SubscriptionRepository;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User createUser(Long id, String email, String username) {
        return User.builder()
                .id(id)
                .email(email)
                .username(username)
                .build();
    }

    private Subscription createSubscription(Long id, Long subscriberId, Long artistId) {
        Subscription subscription = new Subscription(subscriberId, artistId);
        try {
            var idField = Subscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, id);
            var createdAtField = Subscription.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(subscription, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subscription;
    }

    @Test
    void subscribe_shouldSucceed() {
        User subscriber = createUser(1L, "sub@example.com", "subscriber");
        User artist = createUser(2L, "artist@example.com", "artistuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionDTO result = subscriptionService.subscribe(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getSubscriberId());
        assertEquals(2L, result.getArtistId());
        assertEquals("subscriber", result.getSubscriberUsername());
        assertEquals("artistuser", result.getArtistUsername());
    }

    @Test
    void subscribe_toSelf_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.subscribe(1L, 1L));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_whenAlreadySubscribed_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "a@b.com", "sub")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "c@d.com", "art")));
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.subscribe(1L, 2L));
    }

    @Test
    void subscribe_withNonExistentSubscriber_shouldThrowException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "artist@test.com", "artist")));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.subscribe(1L, 2L));
    }

    @Test
    void subscribe_withNonExistentArtist_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.subscribe(1L, 999L));
    }

    @Test
    void unsubscribe_shouldSucceed() {
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(true);

        subscriptionService.unsubscribe(1L, 2L);

        verify(subscriptionRepository).deleteBySubscriberIdAndArtistId(1L, 2L);
    }

    @Test
    void unsubscribe_whenNotSubscribed_shouldThrowException() {
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.unsubscribe(1L, 2L));
    }

    @Test
    void getSubscriptionsBySubscriber_shouldReturnList() {
        Subscription sub1 = createSubscription(1L, 1L, 2L);
        Subscription sub2 = createSubscription(2L, 1L, 3L);

        when(subscriptionRepository.findBySubscriberId(1L)).thenReturn(List.of(sub1, sub2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "s@e.com", "sub")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "a@e.com", "artist1")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "b@e.com", "artist2")));

        List<SubscriptionDTO> result = subscriptionService.getSubscriptionsBySubscriber(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getSubscribersByArtist_shouldReturnList() {
        Subscription sub1 = createSubscription(1L, 2L, 1L);
        Subscription sub2 = createSubscription(2L, 3L, 1L);

        when(subscriptionRepository.findByArtistId(1L)).thenReturn(List.of(sub1, sub2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "a@e.com", "artist")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "s1@e.com", "sub1")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(createUser(3L, "s2@e.com", "sub2")));

        List<SubscriptionDTO> result = subscriptionService.getSubscribersByArtist(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getSubscriberCount_shouldReturnCount() {
        when(subscriptionRepository.countByArtistId(1L)).thenReturn(5L);

        Long count = subscriptionService.getSubscriberCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void isSubscribed_shouldReturnTrue_whenSubscribed() {
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(true);

        assertTrue(subscriptionService.isSubscribed(1L, 2L));
    }

    @Test
    void isSubscribed_shouldReturnFalse_whenNotSubscribed() {
        when(subscriptionRepository.existsBySubscriberIdAndArtistId(1L, 2L)).thenReturn(false);

        assertFalse(subscriptionService.isSubscribed(1L, 2L));
    }
}
