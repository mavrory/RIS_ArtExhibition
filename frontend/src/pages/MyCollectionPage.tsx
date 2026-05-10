import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, Tabs, Tab } from '@mui/material';
import ArtworkGrid from '../components/artwork/ArtworkGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Artwork } from '../types';
import { artworkAPI, orderAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import FavoriteIcon from '@mui/icons-material/Favorite';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';

const MyCollectionPage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [purchasedArtworks, setPurchasedArtworks] = useState<Artwork[]>([]);
  const [favoriteArtworks, setFavoriteArtworks] = useState<Artwork[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    const fetchData = async () => {
      try {
        const [orders, favorites] = await Promise.all([
          orderAPI.getMyOrders(),
          artworkAPI.getFavorites(),
        ]);

        const completedOrders = orders.filter(order => order.status === 'COMPLETED');
        const artworkIds = completedOrders.map(order => order.artworkId);
        
        const artworkPromises = artworkIds.map(id => artworkAPI.getById(id));
        const artworks = await Promise.all(artworkPromises);
        
        setPurchasedArtworks(artworks as any);
        setFavoriteArtworks(favorites);
      } catch (error) {
        console.error('Failed to fetch collection:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated, navigate]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleFavoriteToggle = async () => {
    try {
      const favorites = await artworkAPI.getFavorites();
      setFavoriteArtworks(favorites);
    } catch (error) {
      console.error('Failed to refresh favorites:', error);
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h2"
            component="h1"
            gutterBottom
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontSize: { xs: '2rem', md: '3rem' },
              fontWeight: 700,
              color: '#1a1a1a',
              mb: 2,
            }}
          >
            Моя коллекция
          </Typography>
          <Typography
            variant="h6"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#4a4a4a',
              fontWeight: 400,
            }}
          >
            Ваши приобретенные работы и избранное
          </Typography>
        </Box>

        <Box sx={{ borderBottom: '2px solid rgba(147, 5, 0, 0.1)', mb: 4 }}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            sx={{
              '& .MuiTab-root': {
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                fontSize: '1rem',
                color: '#4a4a4a',
                '&.Mui-selected': {
                  color: '#930500',
                },
              },
              '& .MuiTabs-indicator': {
                backgroundColor: '#930500',
                height: '3px',
              },
            }}
          >
            <Tab
              icon={<ShoppingBagIcon />}
              iconPosition="start"
              label={`Куплено (${purchasedArtworks.length})`}
            />
            <Tab
              icon={<FavoriteIcon />}
              iconPosition="start"
              label={`Избранное (${favoriteArtworks.length})`}
            />
          </Tabs>
        </Box>

        {tabValue === 0 && (
          <>
            {purchasedArtworks.length === 0 ? (
              <Box
                sx={{
                  textAlign: 'center',
                  py: 8,
                  px: 3,
                  backgroundColor: '#FFF8E7',
                  borderRadius: '16px',
                  border: '1px solid rgba(147, 5, 0, 0.1)',
                }}
              >
                <ShoppingBagIcon sx={{ fontSize: 64, color: '#95BBEA', mb: 2 }} />
                <Typography
                  variant="h5"
                  sx={{
                    fontFamily: "'Space Grotesk', sans-serif",
                    color: '#7a7a7a',
                    fontWeight: 500,
                    mb: 1,
                  }}
                >
                  Вы еще не купили ни одной работы
                </Typography>
                <Typography
                  variant="body1"
                  sx={{
                    fontFamily: "'Space Grotesk', sans-serif",
                    color: '#7a7a7a',
                  }}
                >
                  Исследуйте галерею и найдите что-то особенное
                </Typography>
              </Box>
            ) : (
              <ArtworkGrid artworks={purchasedArtworks} />
            )}
          </>
        )}

        {tabValue === 1 && (
          <>
            {favoriteArtworks.length === 0 ? (
              <Box
                sx={{
                  textAlign: 'center',
                  py: 8,
                  px: 3,
                  backgroundColor: '#FFF8E7',
                  borderRadius: '16px',
                  border: '1px solid rgba(147, 5, 0, 0.1)',
                }}
              >
                <FavoriteIcon sx={{ fontSize: 64, color: '#930500', mb: 2 }} />
                <Typography
                  variant="h5"
                  sx={{
                    fontFamily: "'Space Grotesk', sans-serif",
                    color: '#7a7a7a',
                    fontWeight: 500,
                    mb: 1,
                  }}
                >
                  У вас пока нет избранных работ
                </Typography>
                <Typography
                  variant="body1"
                  sx={{
                    fontFamily: "'Space Grotesk', sans-serif",
                    color: '#7a7a7a',
                  }}
                >
                  Добавляйте работы в избранное, чтобы не потерять их
                </Typography>
              </Box>
            ) : (
              <ArtworkGrid artworks={favoriteArtworks} onFavoriteToggle={handleFavoriteToggle} />
            )}
          </>
        )}
      </Container>
    </Box>
  );
};

export default MyCollectionPage;
