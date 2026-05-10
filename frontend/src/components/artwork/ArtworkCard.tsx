import React from 'react';
import {
  Card,
  CardMedia,
  CardContent,
  Typography,
  Box,
  Chip,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import { Artwork } from '../../types';
import { artworkAPI } from '../../services/api';

interface ArtworkCardProps {
  artwork: Artwork;
  onFavoriteToggle?: () => void;
}

const ArtworkCard: React.FC<ArtworkCardProps> = ({ artwork, onFavoriteToggle }) => {
  const navigate = useNavigate();

  const handleCardClick = () => {
    navigate(`/artworks/${artwork.id}`);
  };

  const handleArtistClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/artist/${artwork.authorId}`);
  };

  return (
    <Card
      sx={{
        cursor: 'pointer',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
        backgroundColor: '#FFF8E7',
        borderRadius: '16px',
        overflow: 'hidden',
        border: '1px solid rgba(147, 5, 0, 0.1)',
        transition: 'all 0.3s ease',
        '&:hover': {
          transform: 'translateY(-8px)',
          boxShadow: '0 12px 32px rgba(147, 5, 0, 0.15)',
          '& .artwork-image': {
            transform: 'scale(1.05)',
          },
        },
      }}
      onClick={handleCardClick}
    >
      <Box sx={{ overflow: 'hidden', position: 'relative', height: '300px' }}>
        <CardMedia
          component="img"
          className="artwork-image"
          image={artworkAPI.getPreviewUrl(artwork.id)}
          alt={artwork.title}
          sx={{
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.3s ease',
          }}
        />
        
        {/* Gradient overlay */}
        <Box
          sx={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            height: '50%',
            background: 'linear-gradient(to top, rgba(0,0,0,0.3), transparent)',
          }}
        />
      </Box>

      {/* Display favorite status (read-only) */}
      {artwork.isFavorited && (
        <Box
          sx={{
            position: 'absolute',
            top: 12,
            right: 12,
            backgroundColor: '#FFF8E7',
            borderRadius: '50%',
            padding: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
          }}
        >
          <FavoriteIcon sx={{ color: '#930500', fontSize: 20 }} />
        </Box>
      )}

      {artwork.isSold && (
        <Chip
          label="ПРОДАНО"
          sx={{
            position: 'absolute',
            top: 12,
            left: 12,
            backgroundColor: '#930500',
            color: '#FFF8E7',
            fontFamily: "'Space Grotesk', sans-serif",
            fontWeight: 600,
            fontSize: '0.75rem',
            letterSpacing: '1px',
            boxShadow: '0 4px 12px rgba(147, 5, 0, 0.3)',
          }}
        />
      )}

      <CardContent sx={{ flexGrow: 1, p: 3 }}>
        <Typography
          variant="h6"
          gutterBottom
          noWrap
          sx={{
            fontFamily: "'Playfair Display', serif",
            fontWeight: 600,
            fontSize: '1.25rem',
            color: '#1a1a1a',
            mb: 1,
          }}
        >
          {artwork.title}
        </Typography>

        <Typography
          variant="body2"
          gutterBottom
          onClick={handleArtistClick}
          sx={{
            fontFamily: "'Space Grotesk', sans-serif",
            color: '#7a7a7a',
            fontSize: '0.9rem',
            mb: 2,
            cursor: 'pointer',
            '&:hover': {
              color: '#930500',
              textDecoration: 'underline',
            },
          }}
        >
          {artwork.authorName}
        </Typography>

        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mt: 2,
            pt: 2,
            borderTop: '1px solid rgba(147, 5, 0, 0.1)',
          }}
        >
          <Typography
            variant="h6"
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 700,
              fontSize: '1.3rem',
              color: '#930500',
            }}
          >
            ${artwork.price.toFixed(2)}
          </Typography>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <FavoriteIcon sx={{ fontSize: 16, color: '#95BBEA' }} />
            <Typography
              variant="body2"
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#7a7a7a',
                fontWeight: 500,
              }}
            >
              {artwork.favoritesCount}
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default ArtworkCard;
