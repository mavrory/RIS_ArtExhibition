package com.digitalart.subscription.application;

import com.digitalart.subscription.domain.Subscription;
import com.digitalart.subscription.infrastructure.SubscriptionRepository;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
                              UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public SubscriptionDTO subscribe(Long subscriberId, Long artistId) {
        // Проверка что пользователь не подписывается на самого себя
        if (subscriberId.equals(artistId)) {
            throw new IllegalArgumentException("Cannot subscribe to yourself");
        }
        
        // Проверка что художник существует
        User artist = userRepository.findById(artistId)
            .orElseThrow(() -> new IllegalArgumentException("Artist not found"));
        
        // Проверка что пользователь существует
        User subscriber = userRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        
        // Проверка что подписка не существует
        if (subscriptionRepository.existsBySubscriberIdAndArtistId(subscriberId, artistId)) {
            throw new IllegalArgumentException("Already subscribed to this artist");
        }
        
        Subscription subscription = new Subscription(subscriberId, artistId);
        subscription = subscriptionRepository.save(subscription);
        
        return toDTO(subscription, subscriber.getUsername(), artist.getUsername());
    }
    
    @Transactional
    public void unsubscribe(Long subscriberId, Long artistId) {
        if (!subscriptionRepository.existsBySubscriberIdAndArtistId(subscriberId, artistId)) {
            throw new IllegalArgumentException("Subscription not found");
        }
        
        subscriptionRepository.deleteBySubscriberIdAndArtistId(subscriberId, artistId);
    }
    
    public List<SubscriptionDTO> getSubscriptionsBySubscriber(Long subscriberId) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(subscriberId);
        
        return subscriptions.stream()
            .map(sub -> {
                User artist = userRepository.findById(sub.getArtistId()).orElse(null);
                User subscriber = userRepository.findById(sub.getSubscriberId()).orElse(null);
                
                return toDTO(sub, 
                    subscriber != null ? subscriber.getUsername() : "Unknown",
                    artist != null ? artist.getUsername() : "Unknown");
            })
            .collect(Collectors.toList());
    }
    
    public List<SubscriptionDTO> getSubscribersByArtist(Long artistId) {
        List<Subscription> subscriptions = subscriptionRepository.findByArtistId(artistId);
        
        return subscriptions.stream()
            .map(sub -> {
                User artist = userRepository.findById(sub.getArtistId()).orElse(null);
                User subscriber = userRepository.findById(sub.getSubscriberId()).orElse(null);
                
                return toDTO(sub,
                    subscriber != null ? subscriber.getUsername() : "Unknown",
                    artist != null ? artist.getUsername() : "Unknown");
            })
            .collect(Collectors.toList());
    }
    
    public Long getSubscriberCount(Long artistId) {
        return subscriptionRepository.countByArtistId(artistId);
    }
    
    public boolean isSubscribed(Long subscriberId, Long artistId) {
        return subscriptionRepository.existsBySubscriberIdAndArtistId(subscriberId, artistId);
    }
    
    private SubscriptionDTO toDTO(Subscription subscription, String subscriberUsername, String artistUsername) {
        return new SubscriptionDTO(
            subscription.getId(),
            subscription.getSubscriberId(),
            subscriberUsername,
            subscription.getArtistId(),
            artistUsername,
            subscription.getCreatedAt()
        );
    }
}
