import React from 'react';
import {
  Card,
  CardContent,
  CardMedia,
  Typography,
  Box,
  Chip,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { Exhibition } from '../../types';
import ImageIcon from '@mui/icons-material/Image';
import { useAuth } from '../../context/AuthContext';
import CollectionsIcon from '@mui/icons-material/Collections';

interface ExhibitionCardProps {
  exhibition: Exhibition;
}

const ExhibitionCard: React.FC<ExhibitionCardProps> = ({ exhibition }) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const handleCardClick = () => {
    if (!isAuthenticated) {
      navigate('/login?redirect=' + encodeURIComponent(`/exhibitions/${exhibition.id}`));
      return;
    }
    navigate(`/exhibitions/${exhibition.id}`);
  };

  return (
    <Card
      sx={{
        cursor: 'pointer',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: '#FFF8E7',
        borderRadius: '16px',
        overflow: 'hidden',
        border: '1px solid rgba(147, 5, 0, 0.1)',
        transition: 'all 0.3s ease',
        '&:hover': {
          transform: 'translateY(-8px)',
          boxShadow: '0 12px 32px rgba(147, 5, 0, 0.15)',
          '& .exhibition-image': {
            transform: 'scale(1.05)',
          },
          '& .exhibition-overlay': {
            opacity: 1,
          },
        },
      }}
      onClick={handleCardClick}
    >
      <Box sx={{ overflow: 'hidden', position: 'relative', height: '240px' }}>
        {exhibition.coverImageUrl ? (
          <CardMedia
            component="img"
            className="exhibition-image"
            image={`http://localhost:8080${exhibition.coverImageUrl}`}
            alt={exhibition.title}
            sx={{
              height: '100%',
              objectFit: 'cover',
              transition: 'transform 0.3s ease',
            }}
          />
        ) : (
          <Box
            sx={{
              height: '100%',
              background: 'linear-gradient(135deg, #95BBEA 0%, #6a9dd6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <ImageIcon sx={{ fontSize: 80, color: 'rgba(255, 248, 231, 0.5)' }} />
          </Box>
        )}
        
        {/* Hover overlay */}
        <Box
          className="exhibition-overlay"
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'linear-gradient(to top, rgba(147, 5, 0, 0.8), transparent)',
            opacity: 0,
            transition: 'opacity 0.3s ease',
            display: 'flex',
            alignItems: 'flex-end',
            p: 2,
          }}
        >
          <Typography
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#FFF8E7',
              fontWeight: 600,
              fontSize: '0.9rem',
            }}
          >
            Посмотреть выставку →
          </Typography>
        </Box>
      </Box>

      <CardContent sx={{ flexGrow: 1, p: 3 }}>
        <Typography
          variant="h5"
          gutterBottom
          sx={{
            fontFamily: "'Playfair Display', serif",
            fontWeight: 600,
            fontSize: '1.4rem',
            color: '#1a1a1a',
            mb: 1,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
          }}
        >
          {exhibition.title}
        </Typography>

        <Typography
          variant="body2"
          gutterBottom
          sx={{
            fontFamily: "'Space Grotesk', sans-serif",
            color: '#7a7a7a',
            fontSize: '0.9rem',
            mb: 2,
          }}
        >
          {exhibition.creatorName}
        </Typography>

        <Typography
          variant="body2"
          sx={{
            fontFamily: "'Space Grotesk', sans-serif",
            color: '#4a4a4a',
            fontSize: '0.9rem',
            lineHeight: 1.6,
            mt: 2,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
          }}
        >
          {exhibition.description}
        </Typography>

        <Box sx={{ mt: 3, pt: 2, borderTop: '1px solid rgba(147, 5, 0, 0.1)' }}>
          <Chip
            icon={<CollectionsIcon sx={{ fontSize: 16 }} />}
            label={`${exhibition.artworksCount || 0} работ`}
            sx={{
              backgroundColor: 'rgba(149, 187, 234, 0.15)',
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 600,
              fontSize: '0.85rem',
              border: '1px solid rgba(149, 187, 234, 0.3)',
              '& .MuiChip-icon': {
                color: '#95BBEA',
              },
            }}
          />
        </Box>
      </CardContent>
    </Card>
  );
};

export default ExhibitionCard;
