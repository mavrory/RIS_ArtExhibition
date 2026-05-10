import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Button,
  Grid,
  Paper,
  Divider,
  TextField,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
} from '@mui/material';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import DownloadIcon from '@mui/icons-material/Download';
import { AccountBalanceWallet } from '@mui/icons-material';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { ArtworkDetail, PaymentMethodType } from '../types';
import { artworkAPI, orderAPI, paymentAPI, walletAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

const ArtworkDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [artwork, setArtwork] = useState<ArtworkDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [comment, setComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [orderId, setOrderId] = useState<number | null>(null);
  const [imageUrl, setImageUrl] = useState<string>('');
  const [userBalance, setUserBalance] = useState<number>(0);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  const fetchArtwork = async () => {
    if (!id) return;
    
    try {
      const data = await artworkAPI.getById(Number(id));
      setArtwork(data);
      await loadImage(data);
    } catch (error) {
      console.error('Failed to fetch artwork:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadImage = async (artworkData: ArtworkDetail) => {
    try {
      const token = localStorage.getItem('token');
      const url = `http://localhost:8080${artworkData.imageUrl}`;
      
      const response = await fetch(url, {
        headers: token ? { 'Authorization': `Bearer ${token}` } : {}
      });
      
      if (response.ok) {
        const blob = await response.blob();
        const objectUrl = URL.createObjectURL(blob);
        setImageUrl(objectUrl);
      } else {
        setImageUrl('https://via.placeholder.com/800x600?text=Image+Not+Available');
      }
    } catch (error) {
      setImageUrl('https://via.placeholder.com/800x600?text=Image+Not+Available');
    }
  };

  useEffect(() => {
    fetchArtwork();
    return () => {
      if (imageUrl && imageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [id]);

  const handleFavoriteToggle = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!artwork) return;

    const wasFavorited = artwork.isFavorited;
    setArtwork({
      ...artwork,
      isFavorited: !wasFavorited,
      favoritesCount: wasFavorited ? (artwork.favoritesCount || 0) - 1 : (artwork.favoritesCount || 0) + 1
    });

    try {
      await artworkAPI.toggleFavorite(artwork.id);
      const updatedArtwork = await artworkAPI.getById(artwork.id);
      setArtwork(updatedArtwork);
    } catch (error) {
      fetchArtwork();
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!artwork || !comment.trim()) return;

    setSubmittingComment(true);
    try {
      await artworkAPI.addComment(artwork.id, comment);
      setComment('');
      fetchArtwork();
    } catch (error) {
      console.error('Failed to add comment:', error);
    } finally {
      setSubmittingComment(false);
    }
  };

  const handlePurchase = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!artwork) return;

    try {
      const balanceData = await walletAPI.getBalance();
      setUserBalance(balanceData.balance);
      const order = await orderAPI.create(artwork.id);
      setOrderId(order.id);
      setPaymentError(null);
      setPaymentDialogOpen(true);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Не удалось создать заказ');
    }
  };

  const handlePayment = async () => {
    if (!orderId || !artwork) return;

    if (userBalance < artwork.price) {
      setPaymentError(`Недостаточно средств. У вас $${userBalance.toFixed(2)}, необходимо $${artwork.price.toFixed(2)}. Пополните кошелек.`);
      return;
    }

    setProcessing(true);
    setPaymentError(null);
    
    try {
      await paymentAPI.process(orderId, PaymentMethodType.WALLET);
      setPaymentDialogOpen(false);
      alert('Оплата прошла успешно! Теперь у вас есть доступ к оригиналу.');
      await fetchArtwork();
    } catch (error: any) {
      setPaymentError(error.response?.data?.message || 'Ошибка оплаты. Попробуйте снова.');
    } finally {
      setProcessing(false);
    }
  };

  const handleDownload = async () => {
    if (!artwork) return;
    try {
      const blob = await artworkAPI.downloadOriginal(artwork.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${artwork.title}.jpg`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      alert('Не удалось скачать работу');
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!artwork) {
    return (
      <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography variant="h5" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a' }}>
          Работа не найдена
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ backgroundColor: '#FFF8E7', minHeight: '100vh', py: 6 }}>
      <Container maxWidth="lg">
        <Grid container spacing={4}>
          <Grid size={{ xs: 12, md: 8 }}>
            <Paper elevation={0} sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', overflow: 'hidden', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
              {imageUrl ? (
                <Box
                  component="img"
                  src={imageUrl}
                  alt={artwork.title}
                  sx={{ width: '100%', height: 'auto', display: 'block' }}
                />
              ) : (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <CircularProgress sx={{ color: '#930500' }} />
                </Box>
              )}
            </Paper>
          </Grid>

          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', p: 3, border: '1px solid rgba(147, 5, 0, 0.1)' }}>
              <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, color: '#1a1a1a', mb: 1 }}>
                {artwork.title}
              </Typography>

              <Typography variant="h6" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', mb: 2 }}>
                {artwork.authorName}
              </Typography>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <IconButton onClick={handleFavoriteToggle} sx={{ color: artwork.isFavorited ? '#930500' : '#7a7a7a' }}>
                  {artwork.isFavorited ? <FavoriteIcon /> : <FavoriteBorderIcon />}
                </IconButton>
                <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a' }}>
                  {artwork.favoritesCount} лайков
                </Typography>
              </Box>

              <Divider sx={{ my: 2, borderColor: 'rgba(147, 5, 0, 0.1)' }} />

              <Typography variant="h4" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, color: '#930500', mb: 2 }}>
                ${artwork.price.toFixed(2)}
              </Typography>

              {artwork.isSold && !artwork.isPurchased && (
                <Chip label="ПРОДАНО" sx={{ mb: 2, backgroundColor: '#930500', color: '#FFF8E7', fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }} />
              )}

              {artwork.isPurchased ? (
                <Box>
                  <Chip label="КУПЛЕНО" sx={{ mb: 2, backgroundColor: 'rgba(149, 187, 234, 0.3)', color: '#1a1a1a', fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }} />
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<DownloadIcon />}
                    onClick={handleDownload}
                    sx={{
                      backgroundColor: '#930500',
                      color: '#FFF8E7',
                      fontFamily: "'Space Grotesk', sans-serif",
                      fontWeight: 600,
                      textTransform: 'none',
                      py: 1.5,
                      borderRadius: '10px',
                      '&:hover': { backgroundColor: '#6b0300' },
                    }}
                  >
                    Скачать оригинал
                  </Button>
                </Box>
              ) : !artwork.isSold ? (
                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  onClick={handlePurchase}
                  sx={{
                    backgroundColor: '#930500',
                    color: '#FFF8E7',
                    fontFamily: "'Space Grotesk', sans-serif",
                    fontWeight: 600,
                    textTransform: 'none',
                    py: 1.5,
                    borderRadius: '10px',
                    fontSize: '1.1rem',
                    '&:hover': { backgroundColor: '#6b0300' },
                  }}
                >
                  Купить работу
                </Button>
              ) : null}

              <Divider sx={{ my: 3, borderColor: 'rgba(147, 5, 0, 0.1)' }} />

              <Typography variant="h6" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, color: '#1a1a1a', mb: 1 }}>
                Описание
              </Typography>
              <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a', lineHeight: 1.6 }}>
                {artwork.description || 'Описание отсутствует'}
              </Typography>
            </Box>
          </Grid>
        </Grid>

        <Box sx={{ mt: 6, backgroundColor: '#FFF8E7', borderRadius: '16px', p: 4, border: '1px solid rgba(147, 5, 0, 0.1)' }}>
          <Typography variant="h5" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, color: '#1a1a1a', mb: 3 }}>
            Комментарии ({artwork.comments.length})
          </Typography>

          {isAuthenticated && (
            <Box component="form" onSubmit={handleAddComment} sx={{ mb: 3 }}>
              <TextField
                fullWidth
                multiline
                rows={3}
                placeholder="Добавить комментарий..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                sx={{
                  mb: 1,
                  '& .MuiOutlinedInput-root': {
                    fontFamily: "'Space Grotesk', sans-serif",
                    backgroundColor: '#FFF8E7',
                    '& fieldset': { borderColor: 'rgba(147, 5, 0, 0.2)' },
                    '&:hover fieldset': { borderColor: '#930500' },
                    '&.Mui-focused fieldset': { borderColor: '#930500' },
                  },
                }}
              />
              <Button
                type="submit"
                variant="contained"
                disabled={submittingComment || !comment.trim()}
                sx={{
                  backgroundColor: '#930500',
                  color: '#FFF8E7',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 600,
                  textTransform: 'none',
                  px: 3,
                  '&:hover': { backgroundColor: '#6b0300' },
                }}
              >
                {submittingComment ? 'Отправка...' : 'Отправить'}
              </Button>
            </Box>
          )}

          <List>
            {artwork.comments.map((comment) => (
              <ListItem key={comment.id} alignItems="flex-start" divider sx={{ borderColor: 'rgba(147, 5, 0, 0.1)' }}>
                <ListItemText
                  primary={
                    <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>
                      {comment.username}
                    </Typography>
                  }
                  secondary={
                    <>
                      <Typography variant="body2" component="span" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                        {comment.content}
                      </Typography>
                      <Typography variant="caption" sx={{ display: 'block', fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', mt: 0.5 }}>
                        {new Date(comment.createdAt).toLocaleDateString('ru-RU')}
                      </Typography>
                    </>
                  }
                />
              </ListItem>
            ))}
          </List>

          {artwork.comments.length === 0 && (
            <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#7a7a7a', textAlign: 'center', py: 4 }}>
              Пока нет комментариев. Будьте первым!
            </Typography>
          )}
        </Box>

        <Dialog 
          open={paymentDialogOpen} 
          onClose={() => !processing && setPaymentDialogOpen(false)}
          maxWidth="sm"
          fullWidth
          slotProps={{ paper: { sx: { backgroundColor: '#FFF8E7', borderRadius: '16px' } } }}
        >
          <DialogTitle sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, fontSize: '1.5rem', color: '#1a1a1a' }}>
            Завершить оплату
          </DialogTitle>
          <DialogContent>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body1" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                Работа: <strong>{artwork.title}</strong>
              </Typography>
              <Typography variant="h5" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, color: '#930500', mt: 1 }}>
                Итого: ${artwork.price.toFixed(2)}
              </Typography>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'rgba(147, 5, 0, 0.1)' }} />

            <Box sx={{ p: 2, bgcolor: 'rgba(149, 187, 234, 0.1)', borderRadius: '10px', border: '1px solid rgba(149, 187, 234, 0.3)' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <AccountBalanceWallet sx={{ color: '#95BBEA' }} />
                <Typography variant="subtitle1" sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>
                  Оплата из кошелька
                </Typography>
              </Box>
              <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                Ваш баланс: <strong>${userBalance.toFixed(2)}</strong>
              </Typography>
            </Box>

            {paymentError && (
              <Alert severity="error" sx={{ mt: 2, fontFamily: "'Space Grotesk', sans-serif" }}>
                {paymentError}
              </Alert>
            )}

            {userBalance < artwork.price && (
              <Alert severity="warning" sx={{ mt: 2, fontFamily: "'Space Grotesk', sans-serif" }}>
                Недостаточно средств. Нужно еще ${(artwork.price - userBalance).toFixed(2)}.
                <Button 
                  size="small" 
                  onClick={() => {
                    setPaymentDialogOpen(false);
                    navigate('/wallet');
                  }}
                  sx={{ ml: 1, textTransform: 'none' }}
                >
                  Пополнить
                </Button>
              </Alert>
            )}

            {userBalance >= artwork.price && (
              <Alert severity="success" sx={{ mt: 2, fontFamily: "'Space Grotesk', sans-serif" }}>
                У вас достаточно средств для покупки.
              </Alert>
            )}
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={() => setPaymentDialogOpen(false)} disabled={processing} sx={{ fontFamily: "'Space Grotesk', sans-serif", textTransform: 'none', color: '#4a4a4a' }}>
              Отмена
            </Button>
            <Button
              onClick={handlePayment}
              variant="contained"
              disabled={processing || userBalance < artwork.price}
              startIcon={processing && <CircularProgress size={20} />}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                '&:hover': { backgroundColor: '#6b0300' },
              }}
            >
              {processing ? 'Обработка...' : 'Оплатить'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default ArtworkDetailPage;
