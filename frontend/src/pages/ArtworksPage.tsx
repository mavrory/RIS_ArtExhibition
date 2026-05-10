import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, TextField, InputAdornment, MenuItem, TextFieldProps } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ArtworkGrid from '../components/artwork/ArtworkGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Artwork } from '../types';
import { artworkAPI } from '../services/api';

const ArtworksPage: React.FC = () => {
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [filteredArtworks, setFilteredArtworks] = useState<Artwork[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [priceFilter, setPriceFilter] = useState('all');

  const fetchArtworks = async () => {
    try {
      const data = await artworkAPI.getAll();
      console.log('Fetched artworks:', data);
      console.log('First artwork:', data[0]);
      if (data[0]) {
        console.log('First artwork authorName:', data[0].authorName);
        console.log('First artwork authorId:', data[0].authorId);
      }
      setArtworks(data);
      setFilteredArtworks(data);
    } catch (error) {
      console.error('Failed to fetch artworks:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArtworks();
  }, []);

  const handleFavoriteToggle = () => {
    fetchArtworks();
  };

  useEffect(() => {
    let filtered = artworks;

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (a) =>
          a.title.toLowerCase().includes(query) ||
          a.description.toLowerCase().includes(query) ||
          a.authorName.toLowerCase().includes(query)
      );
    }

    // Price filter
    if (priceFilter === 'under50') {
      filtered = filtered.filter((a) => a.price < 50);
    } else if (priceFilter === '50to100') {
      filtered = filtered.filter((a) => a.price >= 50 && a.price <= 100);
    } else if (priceFilter === 'over100') {
      filtered = filtered.filter((a) => a.price > 100);
    }

    setFilteredArtworks(filtered);
  }, [searchQuery, priceFilter, artworks]);

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        <Box sx={{ mb: 6 }}>
          <Typography
            variant="h2"
            component="h1"
            gutterBottom
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontSize: { xs: '2.5rem', md: '3.5rem' },
              fontWeight: 700,
              color: '#1a1a1a',
              mb: 2,
            }}
          >
            Все работы
          </Typography>
          <Typography
            variant="h6"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#4a4a4a',
              fontWeight: 400,
              mb: 4,
            }}
          >
            Открывай уникальные цифровые работы от нашего сообщества
          </Typography>

          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 4 }}>
            <TextField
              select
              label="Цена"
              value={priceFilter}
              onChange={(e) => setPriceFilter(e.target.value)}
              size="small"
              sx={{
                minWidth: 150,
                backgroundColor: '#fff',
                borderRadius: '10px',
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '& fieldset': {
                    borderColor: 'rgba(147, 5, 0, 0.2)',
                  },
                  '&:hover fieldset': {
                    borderColor: '#930500',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#930500',
                  },
                },
                '& .MuiInputLabel-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '&.Mui-focused': {
                    color: '#930500',
                  },
                },
              }}
            >
              <MenuItem value="all">Все цены</MenuItem>
              <MenuItem value="under50">До $50</MenuItem>
              <MenuItem value="50to100">$50 - $100</MenuItem>
              <MenuItem value="over100">Свыше $100</MenuItem>
            </TextField>
            <TextField
              placeholder="Поиск работ..."
              variant="outlined"
              size="small"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              sx={{
                minWidth: 300,
                flexGrow: 1,
                backgroundColor: '#fff',
                borderRadius: '10px',
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '& fieldset': {
                    borderColor: 'rgba(147, 5, 0, 0.2)',
                  },
                  '&:hover fieldset': {
                    borderColor: '#930500',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#930500',
                  },
                },
              }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: '#95BBEA' }} />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Box>
        </Box>

        {filteredArtworks.length === 0 ? (
          <Box
            sx={{
              textAlign: 'center',
              py: 8,
              px: 3,
              backgroundColor: '#fff',
              borderRadius: '16px',
              border: '1px solid rgba(147, 5, 0, 0.1)',
            }}
          >
            <Typography
              variant="h5"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#7a7a7a',
                fontWeight: 500,
              }}
            >
              {searchQuery || priceFilter !== 'all' ? 'Работы не найдены' : 'Пока нет доступных работ'}
            </Typography>
          </Box>
        ) : (
          <ArtworkGrid artworks={filteredArtworks} onFavoriteToggle={handleFavoriteToggle} />
        )}
      </Container>
    </Box>
  );
};

export default ArtworksPage;
