package com.digitalart.integration.repository;

import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ArtworkRepositoryTest {

    @Autowired
    private ArtworkRepository artworkRepository;

    private Artwork createArtwork(Long authorId, boolean isSold) {
        return Artwork.builder()
                .authorId(authorId)
                .title("Test Artwork")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .imageUrl("/api/artworks/1/image")
                .previewUrl("/api/artworks/1/preview")
                .imageData(new byte[]{1, 2, 3})
                .previewData(new byte[]{4, 5, 6})
                .isSold(isSold)
                .viewsCount(0L)
                .build();
    }

    @Test
    void findByAuthorId_shouldReturnArtworks() {
        artworkRepository.save(createArtwork(1L, false));
        artworkRepository.save(createArtwork(1L, true));
        artworkRepository.save(createArtwork(2L, false));

        List<Artwork> result = artworkRepository.findByAuthorId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findByIsSoldFalse_shouldReturnUnsoldArtworks() {
        artworkRepository.save(createArtwork(1L, false));
        artworkRepository.save(createArtwork(2L, true));
        artworkRepository.save(createArtwork(3L, false));

        List<Artwork> result = artworkRepository.findByIsSoldFalse();

        assertEquals(2, result.size());
    }

    @Test
    void saveAndFindById_shouldWork() {
        Artwork saved = artworkRepository.save(createArtwork(1L, false));

        Artwork found = artworkRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Test Artwork", found.getTitle());
    }
}
