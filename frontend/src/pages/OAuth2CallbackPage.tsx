import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Container, Box, CircularProgress, Typography } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';

const OAuth2CallbackPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const handleOAuth2Callback = async () => {
      const token = searchParams.get('token');
      const error = searchParams.get('error');

      if (error) {
        navigate('/login?error=oauth2_failed');
        return;
      }

      if (token) {
        try {
          localStorage.setItem('token', token);
          const user = await authAPI.getCurrentUser();
          
          // Manually update auth context
          window.location.href = '/';
        } catch (err) {
          console.error('OAuth2 callback error:', err);
          navigate('/login?error=oauth2_failed');
        }
      } else {
        navigate('/login?error=no_token');
      }
    };

    handleOAuth2Callback();
  }, [searchParams, navigate]);

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          mt: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
        }}
      >
        <CircularProgress size={60} />
        <Typography variant="h6">Completing authentication...</Typography>
      </Box>
    </Container>
  );
};

export default OAuth2CallbackPage;
