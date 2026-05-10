import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Avatar,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import { PersonRemove as PersonRemoveIcon } from '@mui/icons-material';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

interface Subscription {
  id: number;
  subscriberId: number;
  artistId: number;
  artistUsername: string;
  createdAt: string;
}

const MySubscriptionsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadSubscriptions();
  }, []);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      const response = await api.get('/subscriptions/my');
      setSubscriptions(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить подписки');
    } finally {
      setLoading(false);
    }
  };

  const handleUnsubscribe = async (artistId: number) => {
    if (!window.confirm('Вы уверены, что хотите отписаться?')) return;

    try {
      await api.delete(`/subscriptions/${artistId}`);
      loadSubscriptions();
    } catch (err: any) {
      alert('Не удалось отписаться: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleViewProfile = (artistId: number) => {
    navigate(`/artist/${artistId}`);
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
            mb: 4,
          }}
        >
          Мои подписки
        </Typography>

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

        {subscriptions.length === 0 ? (
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
            Вы еще не подписаны ни на одного художника. Найдите интересных художников и подпишитесь на них!
          </Alert>
        ) : (
          <Grid container spacing={3}>
            {subscriptions.map((subscription) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={subscription.id}>
                <Card
                  sx={{
                    backgroundColor: '#FFF8E7',
                    borderRadius: '16px',
                    border: '1px solid rgba(147, 5, 0, 0.1)',
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: '0 8px 24px rgba(147, 5, 0, 0.15)',
                    },
                  }}
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
                      {subscription.artistUsername ? subscription.artistUsername.charAt(0).toUpperCase() : '?'}
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
                      {subscription.artistUsername || 'Unknown Artist'}
                    </Typography>

                    <Typography
                      variant="body2"
                      sx={{
                        fontFamily: "'Space Grotesk', sans-serif",
                        color: '#7a7a7a',
                        mb: 3,
                      }}
                    >
                      Подписка с {new Date(subscription.createdAt).toLocaleDateString('ru-RU')}
                    </Typography>

                    <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                      <Button
                        variant="contained"
                        onClick={() => handleViewProfile(subscription.artistId)}
                        sx={{
                          backgroundColor: '#95BBEA',
                          color: '#FFF8E7',
                          fontFamily: "'Space Grotesk', sans-serif",
                          fontWeight: 600,
                          textTransform: 'none',
                          flex: 1,
                          '&:hover': {
                            backgroundColor: '#7a9dd6',
                          },
                        }}
                      >
                        Профиль
                      </Button>

                      <Button
                        variant="outlined"
                        startIcon={<PersonRemoveIcon />}
                        onClick={() => handleUnsubscribe(subscription.artistId)}
                        sx={{
                          borderColor: '#930500',
                          color: '#930500',
                          fontFamily: "'Space Grotesk', sans-serif",
                          fontWeight: 600,
                          textTransform: 'none',
                          '&:hover': {
                            backgroundColor: 'rgba(147, 5, 0, 0.05)',
                            borderColor: '#930500',
                          },
                        }}
                      >
                        Отписаться
                      </Button>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </Box>
  );
};

export default MySubscriptionsPage;
