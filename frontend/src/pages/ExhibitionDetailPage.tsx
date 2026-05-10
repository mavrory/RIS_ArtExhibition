import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Container, 
  Typography, 
  Box, 
  Divider, 
  Button, 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  CircularProgress
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import ArtworkGrid from '../components/artwork/ArtworkGrid';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { ExhibitionDetail, Artwork } from '../types';
import { exhibitionAPI, artworkAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

const ExhibitionDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [exhibition, setExhibition] = useState<ExhibitionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [addArtworkDialogOpen, setAddArtworkDialogOpen] = useState(false);
  const [availableArtworks, setAvailableArtworks] = useState<Artwork[]>([]);
  const [loadingArtworks, setLoadingArtworks] = useState(false);
  const [addingArtwork, setAddingArtwork] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login?redirect=' + encodeURIComponent(`/exhibitions/${id}`));
      return;
    }
    fetchExhibition();
  }, [id, isAuthenticated, navigate]);

  const fetchExhibition = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const data = await exhibitionAPI.getById(Number(id));
      setExhibition(data);
    } catch (error) {
      console.error('Failed to fetch exhibition:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAddArtworkDialog = async () => {
    setAddArtworkDialogOpen(true);
    setLoadingArtworks(true);
    
    try {
      const allArtworks = await artworkAPI.getAll();
      // Filter artworks by current user and not already in exhibition
      const myArtworks = allArtworks.filter((a: Artwork) => 
        a.authorName === user?.username && 
        !exhibition?.artworks.some(ea => ea.id === a.id)
      );
      setAvailableArtworks(myArtworks);
    } catch (error) {
      console.error('Failed to fetch artworks:', error);
    } finally {
      setLoadingArtworks(false);
    }
  };

  const handleCloseAddArtworkDialog = () => {
    setAddArtworkDialogOpen(false);
  };

  const handleAddArtwork = async (artworkId: number) => {
    if (!id) return;
    
    try {
      setAddingArtwork(true);
      await exhibitionAPI.addArtwork(Number(id), artworkId);
      setAddArtworkDialogOpen(false);
      fetchExhibition(); // Reload exhibition
    } catch (error) {
      console.error('Failed to add artwork:', error);
      alert('Не удалось добавить работу в выставку');
    } finally {
      setAddingArtwork(false);
    }
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

  if (!exhibition) {
    return (
      <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
        <Container maxWidth="lg">
          <Typography 
            variant="h5" 
            align="center" 
            sx={{ 
              mt: 8,
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#1a1a1a',
            }}
          >
            Выставка не найдена
          </Typography>
        </Container>
      </Box>
    );
  }

  const isOwner = user && exhibition.createdBy === user.id;

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography 
            variant="h3" 
            component="h1" 
            gutterBottom
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontWeight: 700,
              color: '#1a1a1a',
            }}
          >
            {exhibition.title}
          </Typography>
          <Typography 
            variant="body1" 
            color="text.secondary" 
            gutterBottom
            sx={{
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#7a7a7a',
            }}
          >
            Куратор: {exhibition.creatorName}
          </Typography>
          {exhibition.description && (
            <Typography 
              variant="body1" 
              sx={{ 
                mt: 2,
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#4a4a4a',
              }}
            >
              {exhibition.description}
            </Typography>
          )}
        </Box>

        <Divider sx={{ mb: 4, borderColor: 'rgba(147, 5, 0, 0.1)' }} />

        <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography 
            variant="h5" 
            gutterBottom
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontWeight: 700,
              color: '#1a1a1a',
            }}
          >
            Работы ({exhibition.artworks.length})
          </Typography>
          {isOwner && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleOpenAddArtworkDialog}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                '&:hover': {
                  backgroundColor: '#6b0300',
                },
              }}
            >
              Добавить работу
            </Button>
          )}
        </Box>

        {exhibition.artworks.length === 0 ? (
          <Typography 
            variant="body1" 
            color="text.secondary" 
            align="center" 
            sx={{ 
              mt: 8,
              fontFamily: "'Space Grotesk', sans-serif",
              color: '#7a7a7a',
            }}
          >
            В этой выставке пока нет работ
          </Typography>
        ) : (
          <ArtworkGrid artworks={exhibition.artworks} />
        )}

      {/* Add Artwork Dialog */}
      <Dialog 
        open={addArtworkDialogOpen} 
        onClose={handleCloseAddArtworkDialog}
        maxWidth="sm"
        fullWidth
        slotProps={{
          paper: {
            sx: {
              backgroundColor: '#FFF8E7',
              borderRadius: '16px',
              border: '1px solid rgba(147, 5, 0, 0.1)',
            }
          }
        }}
      >
        <DialogTitle
          sx={{
            fontFamily: "'Playfair Display', serif",
            fontWeight: 700,
            color: '#1a1a1a',
          }}
        >
          Добавить работу в выставку
        </DialogTitle>
        <DialogContent>
          {loadingArtworks ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress sx={{ color: '#930500' }} />
            </Box>
          ) : availableArtworks.length === 0 ? (
            <Typography
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                color: '#7a7a7a',
              }}
            >
              У вас нет доступных работ для добавления
            </Typography>
          ) : (
            <List>
              {availableArtworks.map((artwork) => (
                <ListItem key={artwork.id} disablePadding>
                  <ListItemButton 
                    onClick={() => handleAddArtwork(artwork.id)}
                    disabled={addingArtwork}
                    sx={{
                      borderRadius: '8px',
                      mb: 1,
                      '&:hover': {
                        backgroundColor: 'rgba(147, 5, 0, 0.05)',
                      },
                    }}
                  >
                    <ListItemText 
                      primary={artwork.title}
                      secondary={`$${artwork.price.toFixed(2)}`}
                      slotProps={{
                        primary: {
                          sx: {
                            fontFamily: "'Space Grotesk', sans-serif",
                            fontWeight: 600,
                            color: '#1a1a1a',
                          }
                        },
                        secondary: {
                          sx: {
                            fontFamily: "'Space Grotesk', sans-serif",
                            color: '#930500',
                          }
                        }
                      }}
                    />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button 
            onClick={handleCloseAddArtworkDialog}
            sx={{
              color: '#930500',
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 600,
              textTransform: 'none',
            }}
          >
            Отмена
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
    </Box>
  );
};

export default ExhibitionDetailPage;
