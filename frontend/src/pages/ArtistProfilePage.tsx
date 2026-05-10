import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Button,
  Grid,
  Card,
  CardContent,
  Avatar,
  Divider,
  CircularProgress,
  Alert,
  Chip,
} from '@mui/material';
import {
  PersonAdd as PersonAddIcon,
  PersonRemove as PersonRemoveIcon,
  Visibility as VisibilityIcon,
  ShoppingCart as ShoppingCartIcon,
  Image as ImageIcon,
  People as PeopleIcon,
} from '@mui/icons-material';
import api from '../services/api';
import { Artwork, User } from '../types';
import ArtworkCard from '../components/artwork/ArtworkCard';
import { useAuth } from '../context/AuthContext';

const ArtistProfilePage: React.FC = () => {
  const { artistId } = useParams<{ artistId: string }>();
  const navigate = useNavigate();
  const { user: currentUser } = useAuth();
  
  const [artist, setArtist] = useState<User | null>(null);
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [subscribersCount, setSubscribersCount] = useState<number>(0);
  const [isSubscribed, setIsSubscribed] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [actionLoading, setActionLoading] = useState<boolean>(false);

  useEffect(() => {
    loadArtistProfile();
  }, [artistId]);

  const loadArtistProfile = async () => {
    try {
      setLoading(true);
      setError('');

      // Load artist info
      const artistResponse = await api.get(`/users/${artistId}/profile`);
      setArtist(artistResponse.data);

      // Load artist's artworks
      const artworksResponse = await api.get(`/artworks/artist/${artistId}`);
      setArtworks(artworksResponse.data);

      // Load subscribers count
      const subscribersResponse = await api.get(`/subscriptions/artist/${artistId}/count`);
      setSubscribersCount(subscribersResponse.data.count);

      // Check if current user is subscribed
      if (currentUser) {
        try {
          const subscriptionResponse = await api.get(`/subscriptions/check/${artistId}`);
          setIsSubscribed(subscriptionResponse.data.isSubscribed);
        } catch (err) {
          // User not authenticated or error checking subscription
          setIsSubscribed(false);
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить профиль художника');
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async () => {
    if (!currentUser) {
      navigate('/login');
      return;
    }

    try {
      setActionLoading(true);
      if (isSubscribed) {
        await api.delete(`/subscriptions/${artistId}`);
        setIsSubscribed(false);
        setSubscribersCount(prev => prev - 1);
      } else {
        await api.post(`/subscriptions/${artistId}`);
        setIsSubscribed(true);
        setSubscribersCount(prev => prev + 1);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить подписку');
    } finally {
      setActionLoading(false);
    }
  };

  const calculateStats = () => {
    const totalViews = artworks.reduce((sum, artwork) => sum + artwork.viewsCount, 0);
    const totalSales = artworks.filter(artwork => artwork.isSold).length;
    const totalRevenue = artworks
      .filter(artwork => artwork.isSold)
      .reduce((sum, artwork) => sum + artwork.price, 0);

    return { totalViews, totalSales, totalRevenue };
  };

  if (loading) {
    return (
      <Box sx={{ 
        backgroundColor: '#FFF8E7', 
        minHeight: '100vh', 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center' 
      }}>
        <CircularProgress sx={{ color: '#930500' }} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
        <Container sx={{ mt: 4 }}>
          <Alert 
            severity="error"
            sx={{
              backgroundColor: 'rgba(147, 5, 0, 0.1)',
              color: '#930500',
              fontFamily: "'Space Grotesk', sans-serif",
              borderRadius: '12px',
              border: '1px solid rgba(147, 5, 0, 0.3)',
            }}
          >
            {error}
          </Alert>
        </Container>
      </Box>
    );
  }

  if (!artist) {
    return (
      <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
        <Container sx={{ mt: 4 }}>
          <Alert 
            severity="warning"
            sx={{
              backgroundColor: 'rgba(149, 187, 234, 0.1)',
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              borderRadius: '12px',
              border: '1px solid rgba(149, 187, 234, 0.3)',
            }}
          >
            Художник не найден
          </Alert>
        </Container>
      </Box>
    );
  }

  const stats = calculateStats();
  const isOwnProfile = currentUser?.id === artist.id;

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        {/* Artist Header */}
        <Card sx={{ 
          mb: 4, 
          backgroundColor: '#FFF8E7',
          borderRadius: '16px',
          border: '1px solid rgba(147, 5, 0, 0.1)',
          boxShadow: '0 4px 16px rgba(147, 5, 0, 0.1)',
        }}>
          <CardContent sx={{ p: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, flexWrap: 'wrap' }}>
              <Avatar
                sx={{
                  width: 120,
                  height: 120,
                  fontSize: '3rem',
                  bgcolor: '#930500',
                  color: '#FFF8E7',
                  fontFamily: "'Playfair Display', serif",
                  fontWeight: 700,
                }}
              >
                {artist.username.charAt(0).toUpperCase()}
              </Avatar>

              <Box sx={{ flex: 1, minWidth: 250 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                  <Typography 
                    variant="h4" 
                    component="h1"
                    sx={{
                      fontFamily: "'Playfair Display', serif",
                      fontWeight: 700,
                      color: '#1a1a1a',
                    }}
                  >
                    {artist.username}
                  </Typography>
                  {artist.roles.includes('ARTIST') && (
                    <Chip 
                      label="Artist" 
                      sx={{
                        backgroundColor: '#930500',
                        color: '#FFF8E7',
                        fontFamily: "'Space Grotesk', sans-serif",
                        fontWeight: 600,
                      }}
                      size="small" 
                    />
                  )}
                </Box>

                <Typography 
                  variant="body2" 
                  color="text.secondary" 
                  sx={{ 
                    mb: 3,
                    fontFamily: "'Space Grotesk', sans-serif",
                    color: '#7a7a7a',
                  }}
                >
                  {artist.email}
                </Typography>

                {/* Stats */}
                <Box sx={{ 
                  display: 'flex', 
                  gap: 4, 
                  mb: 3,
                  pb: 3,
                  borderBottom: '1px solid rgba(147, 5, 0, 0.1)',
                }}>
                  <Box>
                    <Typography 
                      variant="h5"
                      sx={{
                        fontFamily: "'Playfair Display', serif",
                        fontWeight: 700,
                        color: '#930500',
                      }}
                    >
                      {artworks.length}
                    </Typography>
                    <Typography 
                      variant="body2" 
                      sx={{
                        fontFamily: "'Space Grotesk', sans-serif",
                        color: '#7a7a7a',
                      }}
                    >
                      Работ
                    </Typography>
                  </Box>
                  <Box>
                    <Typography 
                      variant="h5"
                      sx={{
                        fontFamily: "'Playfair Display', serif",
                        fontWeight: 700,
                        color: '#95BBEA',
                      }}
                    >
                      {subscribersCount}
                    </Typography>
                    <Typography 
                      variant="body2"
                      sx={{
                        fontFamily: "'Space Grotesk', sans-serif",
                        color: '#7a7a7a',
                      }}
                    >
                      Подписчиков
                    </Typography>
                  </Box>
                  <Box>
                    <Typography 
                      variant="h5"
                      sx={{
                        fontFamily: "'Playfair Display', serif",
                        fontWeight: 700,
                        color: '#930500',
                      }}
                    >
                      {stats.totalViews}
                    </Typography>
                    <Typography 
                      variant="body2"
                      sx={{
                        fontFamily: "'Space Grotesk', sans-serif",
                        color: '#7a7a7a',
                      }}
                    >
                      Просмотров
                    </Typography>
                  </Box>
                  <Box>
                    <Typography 
                      variant="h5"
                      sx={{
                        fontFamily: "'Playfair Display', serif",
                        fontWeight: 700,
                        color: '#95BBEA',
                      }}
                    >
                      {stats.totalSales}
                    </Typography>
                    <Typography 
                      variant="body2"
                      sx={{
                        fontFamily: "'Space Grotesk', sans-serif",
                        color: '#7a7a7a',
                      }}
                    >
                      Продаж
                    </Typography>
                  </Box>
                </Box>

                {/* Subscribe Button */}
                {!isOwnProfile && currentUser && (
                  <Button
                    variant={isSubscribed ? 'outlined' : 'contained'}
                    startIcon={isSubscribed ? <PersonRemoveIcon /> : <PersonAddIcon />}
                    onClick={handleSubscribe}
                    disabled={actionLoading}
                    sx={{
                      backgroundColor: isSubscribed ? 'transparent' : '#930500',
                      color: isSubscribed ? '#930500' : '#FFF8E7',
                      borderColor: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      fontWeight: 600,
                      textTransform: 'none',
                      px: 3,
                      py: 1,
                      borderRadius: '8px',
                      '&:hover': {
                        backgroundColor: isSubscribed ? 'rgba(147, 5, 0, 0.05)' : '#6b0300',
                        borderColor: isSubscribed ? '#930500' : '#6b0300',
                      },
                    }}
                  >
                    {isSubscribed ? 'Отписаться' : 'Подписаться'}
                  </Button>
                )}

                {isOwnProfile && (
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/artist-dashboard')}
                    sx={{
                      borderColor: '#95BBEA',
                      color: '#95BBEA',
                      fontFamily: "'Space Grotesk', sans-serif",
                      fontWeight: 600,
                      textTransform: 'none',
                      px: 3,
                      py: 1,
                      borderRadius: '8px',
                      '&:hover': {
                        backgroundColor: 'rgba(149, 187, 234, 0.1)',
                        borderColor: '#95BBEA',
                      },
                    }}
                  >
                    Перейти в дашборд
                  </Button>
                )}
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Divider sx={{ mb: 4, borderColor: 'rgba(147, 5, 0, 0.1)' }} />

        {/* Artworks Grid */}
        <Typography 
          variant="h5" 
          sx={{ 
            mb: 3,
            fontFamily: "'Playfair Display', serif",
            fontWeight: 700,
            color: '#1a1a1a',
          }}
        >
          Работы
        </Typography>

      {artworks.length === 0 ? (
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
          У этого художника пока нет загруженных работ.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {artworks.map((artwork) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={artwork.id}>
              <ArtworkCard artwork={artwork} />
            </Grid>
          ))}
        </Grid>
      )}
      </Container>
    </Box>
  );
};

export default ArtistProfilePage;
