import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Container,
  Box,
  Paper,
  Typography,
  Button,
  FormControlLabel,
  Checkbox,
  CircularProgress,
} from '@mui/material';
import { authAPI } from '../services/api';
import axios from 'axios';

const OAuth2RoleSelectionPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isArtist, setIsArtist] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      navigate('/login?error=no_token');
    }
  }, [token, navigate]);

  const handleSubmit = async () => {
    if (!token) return;

    setLoading(true);
    setError('');

    try {
      localStorage.setItem('token', token);
      
      if (isArtist) {
        // Upgrade user to artist
        await axios.post('http://localhost:8080/api/users/upgrade-to-artist', {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
      }

      // Redirect to home
      window.location.href = '/';
    } catch (err: any) {
      console.error('Role selection error:', err);
      setError('Failed to set role. Please try again.');
      setLoading(false);
    }
  };

  if (!token) {
    return null;
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8, mb: 4 }}>
        <Paper elevation={3} sx={{ p: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom align="center">
            Complete Your Registration
          </Typography>

          <Typography variant="body1" sx={{ mt: 3, mb: 3 }} align="center">
            Welcome! Please select your account type to continue.
          </Typography>

          {error && (
            <Typography color="error" sx={{ mb: 2 }} align="center">
              {error}
            </Typography>
          )}

          <Box sx={{ mt: 3 }}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={isArtist}
                  onChange={(e) => setIsArtist(e.target.checked)}
                  color="primary"
                />
              }
              label="I am an artist (I want to create and sell artworks)"
            />

            <Typography variant="body2" color="text.secondary" sx={{ ml: 4, mt: 1 }}>
              {isArtist
                ? 'You will be able to upload artworks, create exhibitions, and sell your art.'
                : 'You will be able to browse artworks, add favorites, and purchase art.'}
            </Typography>

            <Button
              fullWidth
              variant="contained"
              size="large"
              onClick={handleSubmit}
              disabled={loading}
              sx={{ mt: 4 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Continue'}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default OAuth2RoleSelectionPage;
