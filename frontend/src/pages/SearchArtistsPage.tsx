import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  TextField,
  InputAdornment,
  Grid,
  Card,
  CardContent,
  Avatar,
  Button,
  CircularProgress,
  Alert,
  Chip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { PersonAdd as PersonAddIcon } from '@mui/icons-material';
import api from '../services/api';

interface Artist {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

const SearchArtistsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [artists, setArtists] = useState<Artist[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadTopArtists();
  }, []);

  const loadTopArtists = async () => {
    try {
      setLoading(true);
      setError('');

      // Try to get top artists from detailed statistics
      try {
        const statsResponse = await api.get('/admin/statistics/detailed');
        const topArtists = statsResponse.data.topArtists || [];
        
        const artistsList = topArtists.slice(0, 10).map((artist: any) => ({
          id: artist.artistId,
          username: artist.artistName,
          email: '',
          roles: ['ARTIST'],
        }));
        
        setArtists(artistsList);
      } catch (err) {
        // If admin endpoint fails, get artists from artworks
        const artworksResponse = await api.get('/artworks');
        const artworks = artworksResponse.data;
        
        // Get unique artists
        const uniqueArtists = new Map<number, Artist>();
        artworks.forEach((artwork: any) => {
          if (!uniqueArtists.has(artwork.authorId)) {
            uniqueArtists.set(artwork.authorId, {
              id: artwork.authorId,
              username: artwork.authorName,
              email: '',
              roles: ['ARTIST'],
            });
          }
        });
        
        setArtists(Array.from(uniqueArtists.values()).slice(0, 10));
      }
    } catch (err: any) {
      console.error('Failed to load top artists:', err);
      setError('Не удалось загрузить рекомендованных художников');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      setError('Пожалуйста, введите запрос для поиска');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSearched(true);

      // Get all users and filter artists by username
      const response = await api.get('/admin/users');
      const allUsers = response.data;

      // Filter only artists whose username contains the search query
      const filteredArtists = allUsers.filter((user: Artist) =>
        user.roles.includes('ARTIST') &&
        user.username.toLowerCase().includes(searchQuery.toLowerCase())
      );

      setArtists(filteredArtists);
    } catch (err: any) {
      // If admin endpoint fails, try to search through artworks
      try {
        const artworksResponse = await api.get('/artworks');
        const artworks = artworksResponse.data;
        
        // Get unique artists from artworks
        const uniqueArtists = new Map<number, Artist>();
        artworks.forEach((artwork: any) => {
          if (artwork.authorName.toLowerCase().includes(searchQuery.toLowerCase())) {
            if (!uniqueArtists.has(artwork.authorId)) {
              uniqueArtists.set(artwork.authorId, {
                id: artwork.authorId,
                username: artwork.authorName,
                email: '',
                roles: ['ARTIST'],
              });
            }
          }
        });
        
        setArtists(Array.from(uniqueArtists.values()));
      } catch (err2: any) {
        setError('Не удалось выполнить поиск художников');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleViewProfile = (artistId: number) => {
    navigate(`/artist/${artistId}`);
  };

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        <Typography
          variant="h2"
          gutterBottom
          sx={{
            fontFamily: "'Playfair Display', serif",
            fontSize: { xs: '2rem', md: '3rem' },
            fontWeight: 700,
            color: '#1a1a1a',
            mb: 2,
          }}
        >
          Поиск художников
        </Typography>

        <Typography
          variant="body1"
          sx={{
            fontFamily: "'Space Grotesk', sans-serif",
            color: '#7a7a7a',
            mb: 4,
          }}
        >
          Найдите интересных художников и подпишитесь на них
        </Typography>

        {/* Search Bar */}
        <Box sx={{ mb: 4 }}>
          <TextField
            fullWidth
            placeholder="Введите имя художника..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={handleKeyPress}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: '#930500' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <Button
                      variant="contained"
                      onClick={handleSearch}
                      disabled={loading}
                      sx={{
                        backgroundColor: '#930500',
                        color: '#FFF8E7',
                        fontFamily: "'Space Grotesk', sans-serif",
                        fontWeight: 600,
                        textTransform: 'none',
                        px: 3,
                        '&:hover': {
                          backgroundColor: '#6b0300',
                        },
                      }}
                    >
                      {loading ? <CircularProgress size={24} sx={{ color: '#FFF8E7' }} /> : 'Поиск'}
                    </Button>
                  </InputAdornment>
                ),
              },
            }}
            sx={{
              backgroundColor: '#FFF8E7',
              '& .MuiOutlinedInput-root': {
                fontFamily: "'Space Grotesk', sans-serif",
                borderRadius: '12px',
                '& fieldset': {
                  borderColor: 'rgba(147, 5, 0, 0.2)',
                  borderWidth: '2px',
                },
                '&:hover fieldset': {
                  borderColor: '#930500',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#930500',
                },
              },
            }}
          />
        </Box>

        {error && (
          <Alert
            severity="error"
            sx={{
              mb: 3,
              backgroundColor: 'rgba(147, 5, 0, 0.1)',
              color: '#930500',
              fontFamily: "'Space Grotesk', sans-serif",
              borderRadius: '12px',
              border: '1px solid rgba(147, 5, 0, 0.3)',
            }}
          >
            {error}
          </Alert>
        )}

        {/* Results */}
        {!loading && artists.length > 0 && (
          <>
            <Typography
              variant="h6"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#1a1a1a',
                mb: 3,
              }}
            >
              {searched ? `Найдено художников: ${artists.length}` : 'Рекомендованные художники'}
            </Typography>

            <Grid container spacing={3}>
              {artists.map((artist) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={artist.id}>
                      <Card
                        sx={{
                          backgroundColor: '#FFF8E7',
                          borderRadius: '16px',
                          border: '1px solid rgba(147, 5, 0, 0.1)',
                          transition: 'all 0.3s ease',
                          cursor: 'pointer',
                          '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: '0 8px 24px rgba(147, 5, 0, 0.15)',
                          },
                        }}
                        onClick={() => handleViewProfile(artist.id)}
                      >
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                          <Avatar
                            sx={{
                              width: 80,
                              height: 80,
                              fontSize: '2rem',
                              bgcolor: '#930500',
                              color: '#FFF8E7',
                              fontFamily: "'Playfair Display', serif",
                              fontWeight: 700,
                              margin: '0 auto',
                              mb: 2,
                            }}
                          >
                            {artist.username.charAt(0).toUpperCase()}
                          </Avatar>

                          <Typography
                            variant="h6"
                            sx={{
                              fontFamily: "'Playfair Display', serif",
                              fontWeight: 600,
                              color: '#1a1a1a',
                              mb: 1,
                            }}
                          >
                            {artist.username}
                          </Typography>

                          <Chip
                            label="Художник"
                            size="small"
                            sx={{
                              backgroundColor: '#95BBEA',
                              color: '#FFF8E7',
                              fontFamily: "'Space Grotesk', sans-serif",
                              fontWeight: 600,
                              mb: 2,
                            }}
                          />

                          <Button
                            fullWidth
                            variant="contained"
                            startIcon={<PersonAddIcon />}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleViewProfile(artist.id);
                            }}
                            sx={{
                              backgroundColor: '#930500',
                              color: '#FFF8E7',
                              fontFamily: "'Space Grotesk', sans-serif",
                              fontWeight: 600,
                              textTransform: 'none',
                              '&:hover': {
                                backgroundColor: '#6b0300',
                              },
                            }}
                          >
                            Перейти в профиль
                          </Button>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </>
            )}

        {searched && !loading && artists.length === 0 && (
          <Alert
            severity="info"
            sx={{
              backgroundColor: 'rgba(149, 187, 234, 0.1)',
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              borderRadius: '12px',
              border: '1px solid rgba(149, 187, 234, 0.3)',
            }}
          >
            Художники не найдены. Попробуйте изменить запрос.
          </Alert>
        )}
      </Container>
    </Box>
  );
};

export default SearchArtistsPage;
