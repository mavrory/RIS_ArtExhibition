import React, { useState } from 'react';
import {
  Container,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  Paper,
  Link as MuiLink,
  FormControlLabel,
  Checkbox,
  Divider,
} from '@mui/material';
import { Google as GoogleIcon, GitHub as GitHubIcon } from '@mui/icons-material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const RegisterForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [isArtist, setIsArtist] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      setLoading(false);
      return;
    }

    try {
      await register({ email, password, username, isArtist });
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка регистрации. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  const handleOAuth2Login = (provider: string) => {
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
  };

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', display: 'flex', alignItems: 'center', py: 6 }}>
      <Container maxWidth="sm">
        <Paper 
          elevation={0} 
          sx={{ 
            p: 5, 
            backgroundColor: '#FFF8E7', 
            borderRadius: '20px',
            border: '2px solid rgba(147, 5, 0, 0.1)',
            boxShadow: '0 8px 32px rgba(147, 5, 0, 0.1)'
          }}
        >
          <Typography 
            variant="h3" 
            component="h1" 
            align="center"
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontWeight: 700,
              color: '#1a1a1a',
              mb: 1,
            }}
          >
            Регистрация
          </Typography>
          
          <Typography 
            variant="body1" 
            align="center"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#7a7a7a',
              mb: 4,
            }}
          >
            Присоединяйтесь к ArtSpace
          </Typography>

          {error && (
            <Alert 
              severity="error" 
              sx={{ 
                mb: 3,
                fontFamily: "'Space Grotesk', sans-serif",
                borderRadius: '10px',
              }}
            >
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Имя пользователя"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              margin="normal"
              autoComplete="username"
              sx={{
                mb: 2,
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  backgroundColor: '#FFF8E7',
                  borderRadius: '10px',
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
            />

            <TextField
              fullWidth
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              margin="normal"
              autoComplete="email"
              sx={{
                mb: 2,
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  backgroundColor: '#FFF8E7',
                  borderRadius: '10px',
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
            />

            <TextField
              fullWidth
              label="Пароль"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              margin="normal"
              autoComplete="new-password"
              helperText="Минимум 6 символов"
              sx={{
                mb: 2,
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  backgroundColor: '#FFF8E7',
                  borderRadius: '10px',
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
                '& .MuiFormHelperText-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                },
              }}
            />

            <FormControlLabel
              control={
                <Checkbox
                  checked={isArtist}
                  onChange={(e) => setIsArtist(e.target.checked)}
                  sx={{
                    color: 'rgba(147, 5, 0, 0.5)',
                    '&.Mui-checked': {
                      color: '#930500',
                    },
                  }}
                />
              }
              label={
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                  Зарегистрироваться как художник (вы сможете создавать и продавать работы)
                </Typography>
              }
              sx={{ mt: 2, mb: 3 }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                fontSize: '1.1rem',
                py: 1.5,
                borderRadius: '10px',
                mb: 3,
                '&:hover': {
                  backgroundColor: '#6b0300',
                },
              }}
            >
              {loading ? 'Создание аккаунта...' : 'Зарегистрироваться'}
            </Button>

            <Divider sx={{ my: 3, '&::before, &::after': { borderColor: 'rgba(147, 5, 0, 0.2)' } }}>
              <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', px: 2 }}>
                ИЛИ
              </Typography>
            </Divider>

            <Button
              fullWidth
              variant="outlined"
              size="large"
              onClick={() => handleOAuth2Login('google')}
              startIcon={<GoogleIcon />}
              sx={{
                mb: 2,
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                borderRadius: '10px',
                borderColor: 'rgba(147, 5, 0, 0.3)',
                color: '#1a1a1a',
                py: 1.5,
                borderWidth: '2px',
                '&:hover': {
                  borderColor: '#930500',
                  backgroundColor: 'rgba(147, 5, 0, 0.05)',
                  borderWidth: '2px',
                },
              }}
            >
              Войти через Google
            </Button>

            <Button
              fullWidth
              variant="outlined"
              size="large"
              onClick={() => handleOAuth2Login('github')}
              startIcon={<GitHubIcon />}
              sx={{
                mb: 3,
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                borderRadius: '10px',
                borderColor: 'rgba(147, 5, 0, 0.3)',
                color: '#1a1a1a',
                py: 1.5,
                borderWidth: '2px',
                '&:hover': {
                  borderColor: '#930500',
                  backgroundColor: 'rgba(147, 5, 0, 0.05)',
                  borderWidth: '2px',
                },
              }}
            >
              Войти через GitHub
            </Button>

            <Box sx={{ textAlign: 'center', mt: 3 }}>
              <Typography 
                variant="body1"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#4a4a4a',
                }}
              >
                Уже есть аккаунт?{' '}
                <MuiLink 
                  component={RouterLink} 
                  to="/login"
                  sx={{
                    color: '#930500',
                    fontWeight: 600,
                    textDecoration: 'none',
                    '&:hover': {
                      textDecoration: 'underline',
                    },
                  }}
                >
                  Войти
                </MuiLink>
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};

export default RegisterForm;
