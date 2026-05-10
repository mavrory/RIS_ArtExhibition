import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Card,
  CardContent,
  CardMedia,
  Button,
  Tabs,
  Tab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Chip,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
  Add as AddIcon,
  AttachMoney as MoneyIcon,
  Visibility as ViewIcon,
  AccountBalanceWallet as WalletIcon,
  Collections as CollectionsIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { artworkAPI, exhibitionAPI, walletAPI } from '../services/api';
import { Artwork, Exhibition } from '../types';
import { useNavigate } from 'react-router-dom';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div hidden={value !== index} {...other}>
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

const ArtistDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [exhibitions, setExhibitions] = useState<Exhibition[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [createExhibitionDialogOpen, setCreateExhibitionDialogOpen] = useState(false);
  const [walletBalance, setWalletBalance] = useState<number>(0);

  const [artworkTitle, setArtworkTitle] = useState('');
  const [artworkDescription, setArtworkDescription] = useState('');
  const [artworkPrice, setArtworkPrice] = useState('');
  const [artworkFile, setArtworkFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);

  const [exhibitionTitle, setExhibitionTitle] = useState('');
  const [exhibitionDescription, setExhibitionDescription] = useState('');
  const [creatingExhibition, setCreatingExhibition] = useState(false);

  useEffect(() => {
    if (user) {
      loadData();
    }
  }, [user]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [artworksData, exhibitionsData, balanceData] = await Promise.all([
        artworkAPI.getAll(),
        exhibitionAPI.getAll(),
        walletAPI.getBalance(),
      ]);
      
      const myArtworks = artworksData.filter((a: Artwork) => a.authorName === user?.username);
      setArtworks(myArtworks);
      
      const myExhibitions = exhibitionsData.filter((e: Exhibition) => e.creatorName === user?.username);
      setExhibitions(myExhibitions);
      
      setWalletBalance(balanceData.balance);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadArtwork = async () => {
    if (!artworkTitle || !artworkDescription || !artworkPrice || !artworkFile) {
      alert('Заполните все поля');
      return;
    }

    try {
      setUploading(true);
      const formData = new FormData();
      formData.append('title', artworkTitle);
      formData.append('description', artworkDescription);
      formData.append('price', artworkPrice);
      formData.append('image', artworkFile);

      await artworkAPI.create(formData);
      
      setUploadDialogOpen(false);
      setArtworkTitle('');
      setArtworkDescription('');
      setArtworkPrice('');
      setArtworkFile(null);
      
      loadData();
    } catch (error) {
      console.error('Failed to upload artwork:', error);
      alert('Не удалось загрузить работу');
    } finally {
      setUploading(false);
    }
  };

  const handleCreateExhibition = async () => {
    if (!exhibitionTitle || !exhibitionDescription) {
      alert('Заполните все поля');
      return;
    }

    try {
      setCreatingExhibition(true);
      await exhibitionAPI.create({
        title: exhibitionTitle,
        description: exhibitionDescription,
      });
      
      setCreateExhibitionDialogOpen(false);
      setExhibitionTitle('');
      setExhibitionDescription('');
      
      loadData();
    } catch (error) {
      console.error('Failed to create exhibition:', error);
      alert('Не удалось создать выставку');
    } finally {
      setCreatingExhibition(false);
    }
  };

  const calculateStats = () => {
    const totalArtworks = artworks.length;
    const totalRevenue = artworks
      .filter(a => a.isSold)
      .reduce((sum, a) => sum + (a.price || 0), 0);
    const totalViews = artworks.reduce((sum, a) => sum + (a.viewsCount || 0), 0);

    return { totalArtworks, totalRevenue, totalViews };
  };

  const stats = calculateStats();

  if (loading) {
    return (
      <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
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
          Панель художника
        </Typography>

        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card
              sx={{
                background: 'linear-gradient(135deg, #95BBEA 0%, #6a9dd6 100%)',
                color: '#FFF8E7',
                borderRadius: '16px',
                boxShadow: '0 4px 16px rgba(149, 187, 234, 0.3)',
              }}
            >
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <CollectionsIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  {stats.totalArtworks}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Работ
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card
              sx={{
                background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)',
                color: '#FFF8E7',
                borderRadius: '16px',
                boxShadow: '0 4px 16px rgba(147, 5, 0, 0.3)',
              }}
            >
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <MoneyIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  ${stats.totalRevenue.toFixed(2)}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Заработано
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card
              sx={{
                background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)',
                color: '#FFF8E7',
                borderRadius: '16px',
                boxShadow: '0 4px 16px rgba(147, 5, 0, 0.3)',
                cursor: 'pointer',
                transition: 'transform 0.3s ease',
                '&:hover': { transform: 'translateY(-4px)' },
              }}
              onClick={() => navigate('/wallet')}
            >
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <WalletIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  ${walletBalance.toFixed(2)}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Баланс
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card
              sx={{
                background: 'linear-gradient(135deg, #95BBEA 0%, #6a9dd6 100%)',
                color: '#FFF8E7',
                borderRadius: '16px',
                boxShadow: '0 4px 16px rgba(149, 187, 234, 0.3)',
              }}
            >
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <ViewIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  {stats.totalViews}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Просмотров
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Box sx={{ borderBottom: '2px solid rgba(147, 5, 0, 0.1)', mb: 3 }}>
          <Tabs
            value={tabValue}
            onChange={(e, v) => setTabValue(v)}
            sx={{
              '& .MuiTab-root': {
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                fontSize: '1rem',
                color: '#4a4a4a',
                '&.Mui-selected': { color: '#930500' },
              },
              '& .MuiTabs-indicator': { backgroundColor: '#930500', height: '3px' },
            }}
          >
            <Tab label="Мои работы" />
            <Tab label="Мои выставки" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <Box sx={{ mb: 3 }}>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setUploadDialogOpen(true)}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                py: 1.5,
                borderRadius: '10px',
                '&:hover': { backgroundColor: '#6b0300' },
              }}
            >
              Загрузить работу
            </Button>
          </Box>

          <Grid container spacing={3}>
            {artworks.map((artwork) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={artwork.id}>
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
                  <CardMedia
                    component="img"
                    height="200"
                    image={artworkAPI.getPreviewUrl(artwork.id)}
                    alt={artwork.title}
                  />
                  <CardContent>
                    <Typography variant="h6" noWrap sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, color: '#1a1a1a', mb: 1 }}>
                      {artwork.title}
                    </Typography>
                    <Typography variant="body2" noWrap sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', mb: 2 }}>
                      {artwork.description}
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="h6" sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 700, color: '#930500' }}>
                        ${artwork.price}
                      </Typography>
                      <Button size="small" onClick={() => navigate(`/artworks/${artwork.id}`)} sx={{ fontFamily: "'Space Grotesk', sans-serif", textTransform: 'none', color: '#930500' }}>
                        Смотреть
                      </Button>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {artworks.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 8, backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
              <Typography variant="h6" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', fontWeight: 500 }}>
                Вы еще не загрузили ни одной работы
              </Typography>
            </Box>
          )}
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Box sx={{ mb: 3 }}>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setCreateExhibitionDialogOpen(true)}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                py: 1.5,
                borderRadius: '10px',
                '&:hover': { backgroundColor: '#6b0300' },
              }}
            >
              Создать выставку
            </Button>
          </Box>

          <Grid container spacing={3}>
            {exhibitions.map((exhibition) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={exhibition.id}>
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
                  {exhibition.coverImageUrl && (
                    <CardMedia component="img" height="200" image={`http://localhost:8080${exhibition.coverImageUrl}`} alt={exhibition.title} />
                  )}
                  <CardContent>
                    <Typography variant="h6" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, color: '#1a1a1a', mb: 1 }}>
                      {exhibition.title}
                    </Typography>
                    <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', mb: 2 }}>
                      {exhibition.description}
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Chip
                        label={`${exhibition.artworksCount || 0} работ`}
                        size="small"
                        sx={{
                          backgroundColor: 'rgba(149, 187, 234, 0.15)',
                          color: '#1a1a1a',
                          fontFamily: "'Space Grotesk', sans-serif",
                          fontWeight: 600,
                          border: '1px solid rgba(149, 187, 234, 0.3)',
                        }}
                      />
                    </Box>
                    <Button
                      fullWidth
                      onClick={() => navigate(`/exhibitions/${exhibition.id}`)}
                      sx={{
                        backgroundColor: '#930500',
                        color: '#FFF8E7',
                        fontFamily: "'Space Grotesk', sans-serif",
                        fontWeight: 600,
                        textTransform: 'none',
                        borderRadius: '8px',
                        '&:hover': { backgroundColor: '#6b0300' },
                      }}
                    >
                      Смотреть выставку
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {exhibitions.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 8, backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
              <Typography variant="h6" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', fontWeight: 500 }}>
                Вы еще не создали ни одной выставки
              </Typography>
            </Box>
          )}
        </TabPanel>

        <Dialog open={uploadDialogOpen} onClose={() => setUploadDialogOpen(false)} maxWidth="sm" fullWidth slotProps={{ paper: { sx: { backgroundColor: '#FFF8E7', borderRadius: '16px' } } }}>
          <DialogTitle sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, fontSize: '1.5rem', color: '#1a1a1a' }}>
            Загрузить работу
          </DialogTitle>
          <DialogContent>
            <TextField fullWidth label="Название" value={artworkTitle} onChange={(e) => setArtworkTitle(e.target.value)} margin="normal" />
            <TextField fullWidth label="Описание" value={artworkDescription} onChange={(e) => setArtworkDescription(e.target.value)} margin="normal" multiline rows={3} />
            <TextField fullWidth label="Цена ($)" type="number" value={artworkPrice} onChange={(e) => setArtworkPrice(e.target.value)} margin="normal" />
            <Button variant="outlined" component="label" fullWidth sx={{ mt: 2 }}>
              {artworkFile ? artworkFile.name : 'Выбрать изображение'}
              <input type="file" hidden accept="image/*" onChange={(e) => setArtworkFile(e.target.files?.[0] || null)} />
            </Button>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={() => setUploadDialogOpen(false)}>Отмена</Button>
            <Button onClick={handleUploadArtwork} variant="contained" disabled={uploading} sx={{ backgroundColor: '#930500', '&:hover': { backgroundColor: '#6b0300' } }}>
              {uploading ? <CircularProgress size={24} /> : 'Загрузить'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog open={createExhibitionDialogOpen} onClose={() => setCreateExhibitionDialogOpen(false)} maxWidth="sm" fullWidth slotProps={{ paper: { sx: { backgroundColor: '#FFF8E7', borderRadius: '16px' } } }}>
          <DialogTitle sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, fontSize: '1.5rem', color: '#1a1a1a' }}>
            Создать выставку
          </DialogTitle>
          <DialogContent>
            <TextField fullWidth label="Название" value={exhibitionTitle} onChange={(e) => setExhibitionTitle(e.target.value)} margin="normal" />
            <TextField fullWidth label="Описание" value={exhibitionDescription} onChange={(e) => setExhibitionDescription(e.target.value)} margin="normal" multiline rows={4} />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={() => setCreateExhibitionDialogOpen(false)}>Отмена</Button>
            <Button onClick={handleCreateExhibition} variant="contained" disabled={creatingExhibition} sx={{ backgroundColor: '#930500', '&:hover': { backgroundColor: '#6b0300' } }}>
              {creatingExhibition ? <CircularProgress size={24} /> : 'Создать'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default ArtistDashboardPage;
