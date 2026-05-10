import React from 'react';
import { Box, Container, Typography, Link, IconButton } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import PaletteIcon from '@mui/icons-material/Palette';
import GitHubIcon from '@mui/icons-material/GitHub';
import InstagramIcon from '@mui/icons-material/Instagram';
import TwitterIcon from '@mui/icons-material/Twitter';

const Footer: React.FC = () => {
  return (
    <Box
      component="footer"
      sx={{
        py: 6,
        px: 2,
        mt: 'auto',
        backgroundColor: '#1a1a1a',
        borderTop: '3px solid #930500',
      }}
    >
      <Container maxWidth="lg">
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
            gap: 4,
            mb: 4,
          }}
        >
          {/* Brand Section */}
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <PaletteIcon sx={{ color: '#930500', fontSize: 32, mr: 1 }} />
              <Typography
                variant="h5"
                sx={{
                  fontFamily: "'Playfair Display', serif",
                  fontWeight: 700,
                  color: '#FFF8E7',
                }}
              >
                ArtSpace
              </Typography>
            </Box>
            <Typography
              variant="body2"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#95BBEA',
                lineHeight: 1.6,
              }}
            >
              Молодежная площадка для людей искусства. Открывай, создавай, вдохновляйся.
            </Typography>
          </Box>

          {/* Navigation Links */}
          <Box>
            <Typography
              variant="h6"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                color: '#FFF8E7',
                mb: 2,
              }}
            >
              Навигация
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Link
                component={RouterLink}
                to="/artworks"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#95BBEA',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  '&:hover': {
                    color: '#FFF8E7',
                  },
                }}
              >
                Работы
              </Link>
              <Link
                component={RouterLink}
                to="/exhibitions"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#95BBEA',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  '&:hover': {
                    color: '#FFF8E7',
                  },
                }}
              >
                Выставки
              </Link>
              <Link
                component={RouterLink}
                to="/register"
                sx={{
                  fontFamily: "'Space Grotesk', sans-serif",
                  color: '#95BBEA',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  '&:hover': {
                    color: '#FFF8E7',
                  },
                }}
              >
                Регистрация
              </Link>
            </Box>
          </Box>

          {/* Social Links */}
          <Box>
            <Typography
              variant="h6"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                color: '#FFF8E7',
                mb: 2,
              }}
            >
              Социальные сети
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <IconButton
                sx={{
                  color: '#95BBEA',
                  '&:hover': {
                    color: '#FFF8E7',
                    backgroundColor: 'rgba(149, 187, 234, 0.1)',
                  },
                }}
              >
                <InstagramIcon />
              </IconButton>
              <IconButton
                sx={{
                  color: '#95BBEA',
                  '&:hover': {
                    color: '#FFF8E7',
                    backgroundColor: 'rgba(149, 187, 234, 0.1)',
                  },
                }}
              >
                <TwitterIcon />
              </IconButton>
              <IconButton
                sx={{
                  color: '#95BBEA',
                  '&:hover': {
                    color: '#FFF8E7',
                    backgroundColor: 'rgba(149, 187, 234, 0.1)',
                  },
                }}
              >
                <GitHubIcon />
              </IconButton>
            </Box>
          </Box>
        </Box>

        {/* Bottom Section */}
        <Box
          sx={{
            pt: 3,
            borderTop: '1px solid rgba(149, 187, 234, 0.2)',
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Typography
            variant="body2"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#7a7a7a',
              fontSize: '0.85rem',
            }}
          >
            © 2026 ArtSpace. Все права защищены.
          </Typography>
          <Typography
            variant="body2"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#7a7a7a',
              fontSize: '0.85rem',
            }}
          >
            Образовательный проект по курсу РИС
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};

export default Footer;
