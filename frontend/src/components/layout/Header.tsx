import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box, IconButton } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import PaletteIcon from '@mui/icons-material/Palette';
import LogoutIcon from '@mui/icons-material/Logout';
import LoginIcon from '@mui/icons-material/Login';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';

const Header: React.FC = () => {
  const { isAuthenticated, user, logout, isArtist } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.roles.includes('ADMIN') || false;

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <AppBar 
      position="static" 
      elevation={0}
      sx={{
        backgroundColor: '#FFF8E7',
        borderBottom: '1px solid rgba(147, 5, 0, 0.1)',
      }}
    >
      <Toolbar sx={{ py: 1 }}>
        <Box
          component={RouterLink}
          to="/"
          sx={{
            display: 'flex',
            alignItems: 'center',
            textDecoration: 'none',
            mr: 4,
          }}
        >
          <PaletteIcon sx={{ color: '#930500', fontSize: 32, mr: 1 }} />
          <Typography
            variant="h5"
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontWeight: 700,
              color: '#930500',
              letterSpacing: '-0.5px',
            }}
          >
            ArtSpace
          </Typography>
        </Box>

        <Box sx={{ flexGrow: 1 }} />

        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Button 
            component={RouterLink} 
            to="/artworks"
            sx={{
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 500,
              textTransform: 'none',
              fontSize: '1rem',
              px: 2,
              '&:hover': {
                backgroundColor: 'rgba(147, 5, 0, 0.05)',
                color: '#930500',
              },
            }}
          >
            Работы
          </Button>
          <Button 
            component={RouterLink} 
            to="/exhibitions"
            sx={{
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 500,
              textTransform: 'none',
              fontSize: '1rem',
              px: 2,
              '&:hover': {
                backgroundColor: 'rgba(147, 5, 0, 0.05)',
                color: '#930500',
              },
            }}
          >
            Выставки
          </Button>
          
          {isAuthenticated ? (
            <>
              <Button 
                component={RouterLink} 
                to="/my-collection"
                sx={{
                  color: '#1a1a1a',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 500,
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 2,
                  '&:hover': {
                    backgroundColor: 'rgba(147, 5, 0, 0.05)',
                    color: '#930500',
                  },
                }}
              >
                Коллекция
              </Button>

              <Button 
                component={RouterLink} 
                to="/search-artists"
                sx={{
                  color: '#1a1a1a',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 500,
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 2,
                  '&:hover': {
                    backgroundColor: 'rgba(147, 5, 0, 0.05)',
                    color: '#930500',
                  },
                }}
              >
                Художники
              </Button>

              <Button 
                component={RouterLink} 
                to="/my-subscriptions"
                sx={{
                  color: '#1a1a1a',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 500,
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 2,
                  '&:hover': {
                    backgroundColor: 'rgba(147, 5, 0, 0.05)',
                    color: '#930500',
                  },
                }}
              >
                Подписки
              </Button>
              
              <IconButton 
                component={RouterLink} 
                to="/wallet"
                sx={{
                  color: '#95BBEA',
                  '&:hover': {
                    backgroundColor: 'rgba(149, 187, 234, 0.1)',
                  },
                }}
              >
                <AccountBalanceWalletIcon />
              </IconButton>
              
              {isArtist && (
                <IconButton 
                  component={RouterLink} 
                  to="/artist-dashboard"
                  sx={{
                    color: '#95BBEA',
                    '&:hover': {
                      backgroundColor: 'rgba(149, 187, 234, 0.1)',
                    },
                  }}
                >
                  <DashboardIcon />
                </IconButton>
              )}
              
              {isAdmin && (
                <IconButton 
                  component={RouterLink} 
                  to="/admin-dashboard"
                  sx={{
                    color: '#95BBEA',
                    '&:hover': {
                      backgroundColor: 'rgba(149, 187, 234, 0.1)',
                    },
                  }}
                >
                  <AdminPanelSettingsIcon />
                </IconButton>
              )}
              
              {isArtist && (
                <Typography 
                  variant="body2" 
                  component={RouterLink}
                  to={`/artist/${user?.id}`}
                  sx={{ 
                    color: '#4a4a4a',
                    fontFamily: "'Space Grotesk', sans-serif",
                    fontWeight: 500,
                    mx: 1,
                    textDecoration: 'none',
                    cursor: 'pointer',
                    '&:hover': {
                      color: '#930500',
                      textDecoration: 'underline',
                    },
                  }}
                >
                  {user?.username}
                </Typography>
              )}
              
              {!isArtist && (
                <Typography 
                  variant="body2" 
                  sx={{ 
                    color: '#4a4a4a',
                    fontFamily: "'Space Grotesk', sans-serif",
                    fontWeight: 500,
                    mx: 1,
                  }}
                >
                  {user?.username}
                </Typography>
              )}
              
              <IconButton 
                onClick={handleLogout} 
                size="small"
                sx={{
                  color: '#930500',
                  '&:hover': {
                    backgroundColor: 'rgba(147, 5, 0, 0.1)',
                  },
                }}
              >
                <LogoutIcon />
              </IconButton>
            </>
          ) : (
            <>
              <Button 
                component={RouterLink} 
                to="/login"
                sx={{
                  color: '#1a1a1a',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 500,
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 2,
                  '&:hover': {
                    backgroundColor: 'rgba(147, 5, 0, 0.05)',
                  },
                }}
              >
                Войти
              </Button>
              <Button
                variant="contained"
                component={RouterLink}
                to="/register"
                sx={{
                  backgroundColor: '#930500',
                  color: '#FFF8E7',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 600,
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 3,
                  borderRadius: '8px',
                  boxShadow: 'none',
                  '&:hover': {
                    backgroundColor: '#6b0300',
                    boxShadow: '0 4px 12px rgba(147, 5, 0, 0.3)',
                  },
                }}
              >
                Регистрация
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
