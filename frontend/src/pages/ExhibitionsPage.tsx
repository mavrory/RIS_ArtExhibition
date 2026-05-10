import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, TextField, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ExhibitionGrid from '../components/exhibition/ExhibitionGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Exhibition } from '../types';
import { exhibitionAPI } from '../services/api';

const ExhibitionsPage: React.FC = () => {
  const [exhibitions, setExhibitions] = useState<Exhibition[]>([]);
  const [filteredExhibitions, setFilteredExhibitions] = useState<Exhibition[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchExhibitions = async () => {
      try {
        const data = await exhibitionAPI.getAll();
        setExhibitions(data);
        setFilteredExhibitions(data);
      } catch (error) {
        console.error('Failed to fetch exhibitions:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchExhibitions();
  }, []);

  useEffect(() => {
    const query = searchQuery.toLowerCase();
    const filtered = exhibitions.filter(
      (e) =>
        e.title.toLowerCase().includes(query) ||
        e.description.toLowerCase().includes(query) ||
        e.creatorName.toLowerCase().includes(query)
    );
    setFilteredExhibitions(filtered);
  }, [searchQuery, exhibitions]);

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
            Все выставки
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
            Исследуй нашу коллекцию кураторских выставок цифрового искусства
          </Typography>

          <TextField
            placeholder="Поиск выставок..."
            variant="outlined"
            size="small"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{
              width: '100%',
              maxWidth: 500,
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

        {filteredExhibitions.length === 0 ? (
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
              {searchQuery ? 'Выставки не найдены' : 'Пока нет доступных выставок'}
            </Typography>
          </Box>
        ) : (
          <ExhibitionGrid exhibitions={filteredExhibitions} />
        )}
      </Container>
    </Box>
  );
};

export default ExhibitionsPage;
