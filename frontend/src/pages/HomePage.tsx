import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ExhibitionGrid from '../components/exhibition/ExhibitionGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Exhibition } from '../types';
import { exhibitionAPI, artworkAPI } from '../services/api';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import { useAuth } from '../context/AuthContext';

const HomePage: React.FC = () => {
  const [exhibitions, setExhibitions] = useState<Exhibition[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ artworks: 0, artists: 0, exhibitions: 0 });
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [exhibitionsData, artworksData] = await Promise.all([
          exhibitionAPI.getAll(),
          artworkAPI.getAll(),
        ]);
        
        setExhibitions(exhibitionsData.slice(0, 6));
        
        // Calculate real stats
        const uniqueArtists = new Set(artworksData.map((a: any) => a.authorId)).size;
        setStats({
          artworks: artworksData.length,
          artists: uniqueArtists,
          exhibitions: exhibitionsData.length,
        });
      } catch (error) {
        console.error('Failed to fetch data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh' }}>
      {/* Hero Banner */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)',
          color: '#FFF8E7',
          py: { xs: 8, md: 12 },
          px: 3,
          position: 'relative',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: '-50%',
            right: '-10%',
            width: '600px',
            height: '600px',
            borderRadius: '50%',
            background: 'rgba(149, 187, 234, 0.1)',
            filter: 'blur(80px)',
          },
          '&::after': {
            content: '""',
            position: 'absolute',
            bottom: '-30%',
            left: '-5%',
            width: '400px',
            height: '400px',
            borderRadius: '50%',
            background: 'rgba(255, 248, 231, 0.05)',
            filter: 'blur(60px)',
          },
        }}
      >
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Box sx={{ maxWidth: '800px', mx: 'auto', textAlign: 'center' }}>
            {isAuthenticated && (
              <Box sx={{ mb: 3 }}>
                <Typography
                  variant="h5"
                  sx={{
                    fontFamily: "'Space Grotesk', sans-serif",
                    fontSize: '1.2rem',
                    fontWeight: 500,
                    color: '#95BBEA',
                    mb: 1,
                  }}
                >
                  Привет, {user?.username}!
                </Typography>
              </Box>
            )}
            
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 3 }}>
              <AutoAwesomeIcon sx={{ fontSize: 40, mr: 1, color: '#95BBEA' }} />
              <Typography
                variant="overline"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontSize: '1rem',
                  fontWeight: 600,
                  letterSpacing: '3px',
                  color: '#95BBEA',
                }}
              >
                Молодежная площадка искусства
              </Typography>
            </Box>
            
            <Typography
              variant="h1"
              sx={{
                fontFamily: "'Playfair Display', serif",
                fontSize: { xs: '2.5rem', md: '4rem' },
                fontWeight: 700,
                mb: 3,
                lineHeight: 1.1,
                letterSpacing: '-1px',
              }}
            >
              {isAuthenticated ? 'Твое пространство творчества' : 'Открывай. Создавай. Вдохновляйся.'}
            </Typography>
            
            <Typography
              variant="h5"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                fontSize: { xs: '1.1rem', md: '1.3rem' },
                fontWeight: 400,
                mb: 5,
                opacity: 0.9,
                lineHeight: 1.6,
              }}
            >
              {isAuthenticated 
                ? 'Исследуй новые выставки, пополняй свою коллекцию и следи за любимыми художниками.'
                : 'Пространство для молодых художников и ценителей цифрового искусства. Коллекционируй уникальные работы, участвуй в выставках.'
              }
            </Typography>
            
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                size="large"
                endIcon={<ArrowForwardIcon />}
                onClick={() => navigate('/exhibitions')}
                sx={{
                  backgroundColor: '#FFF8E7',
                  color: '#930500',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 600,
                  fontSize: '1.1rem',
                  textTransform: 'none',
                  px: 4,
                  py: 1.5,
                  borderRadius: '12px',
                  boxShadow: '0 8px 24px rgba(0, 0, 0, 0.2)',
                  '&:hover': {
                    backgroundColor: '#fff',
                    transform: 'translateY(-2px)',
                    boxShadow: '0 12px 32px rgba(0, 0, 0, 0.3)',
                  },
                  transition: 'all 0.3s ease',
                }}
              >
                Исследовать выставки
              </Button>
              
              <Button
                variant="outlined"
                size="large"
                onClick={() => navigate('/artworks')}
                sx={{
                  borderColor: '#FFF8E7',
                  color: '#FFF8E7',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 600,
                  fontSize: '1.1rem',
                  textTransform: 'none',
                  px: 4,
                  py: 1.5,
                  borderRadius: '12px',
                  borderWidth: '2px',
                  '&:hover': {
                    borderColor: '#FFF8E7',
                    backgroundColor: 'rgba(255, 248, 231, 0.1)',
                    borderWidth: '2px',
                    transform: 'translateY(-2px)',
                  },
                  transition: 'all 0.3s ease',
                }}
              >
                Смотреть работы
              </Button>
            </Box>
          </Box>
        </Container>
      </Box>

      {/* Featured Exhibitions Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box sx={{ mb: 6, textAlign: 'center' }}>
          <Typography
            variant="h2"
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontSize: { xs: '2rem', md: '3rem' },
              fontWeight: 700,
              color: '#1a1a1a',
              mb: 2,
            }}
          >
            Актуальные выставки
          </Typography>
          <Box
            sx={{
              width: '80px',
              height: '4px',
              backgroundColor: '#930500',
              mx: 'auto',
              borderRadius: '2px',
            }}
          />
        </Box>

        <ExhibitionGrid exhibitions={exhibitions} />

        {exhibitions.length > 0 && (
          <Box sx={{ textAlign: 'center', mt: 6 }}>
            <Button
              variant="outlined"
              size="large"
              endIcon={<ArrowForwardIcon />}
              onClick={() => navigate('/exhibitions')}
              sx={{
                borderColor: '#930500',
                color: '#930500',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 4,
                py: 1.5,
                borderRadius: '10px',
                borderWidth: '2px',
                '&:hover': {
                  borderColor: '#930500',
                  backgroundColor: 'rgba(147, 5, 0, 0.05)',
                  borderWidth: '2px',
                },
              }}
            >
              Все выставки
            </Button>
          </Box>
        )}
      </Container>

      {/* Stats Section */}
      <Box
        sx={{
          backgroundColor: '#95BBEA',
          py: 6,
          mt: 4,
        }}
      >
        <Container maxWidth="lg">
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
              gap: 4,
              textAlign: 'center',
            }}
          >
            <Box>
              <Typography
                variant="h2"
                sx={{
                  fontFamily: "'Playfair Display', serif",
                  fontSize: '3rem',
                  fontWeight: 700,
                  color: '#FFF8E7',
                  mb: 1,
                }}
              >
                {stats.artworks}+
              </Typography>
              <Typography
                variant="h6"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#1a1a1a',
                  fontWeight: 500,
                }}
              >
                Цифровых работ
              </Typography>
            </Box>
            
            <Box>
              <Typography
                variant="h2"
                sx={{
                  fontFamily: "'Playfair Display', serif",
                  fontSize: '3rem',
                  fontWeight: 700,
                  color: '#FFF8E7',
                  mb: 1,
                }}
              >
                {stats.artists}+
              </Typography>
              <Typography
                variant="h6"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#1a1a1a',
                  fontWeight: 500,
                }}
              >
                Молодых художников
              </Typography>
            </Box>
            
            <Box>
              <Typography
                variant="h2"
                sx={{
                  fontFamily: "'Playfair Display', serif",
                  fontSize: '3rem',
                  fontWeight: 700,
                  color: '#FFF8E7',
                  mb: 1,
                }}
              >
                {stats.exhibitions}+
              </Typography>
              <Typography
                variant="h6"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#1a1a1a',
                  fontWeight: 500,
                }}
              >
                Выставок
              </Typography>
            </Box>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default HomePage;
