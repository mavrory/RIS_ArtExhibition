package com.digitalart.artwork.infrastructure;

import com.digitalart.artwork.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Favorite.FavoriteId> {
    
    List<Favorite> findByIdUserId(Long userId);
    
    Optional<Favorite> findByIdUserIdAndIdArtworkId(Long userId, Long artworkId);
    
    long countByIdArtworkId(Long artworkId);
}
