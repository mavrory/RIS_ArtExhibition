package com.digitalart.artwork.infrastructure;

import com.digitalart.artwork.domain.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    
    List<Artwork> findByAuthorId(Long authorId);
    
    List<Artwork> findByIsSoldFalse();
}
