import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  Tabs,
  Tab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Card,
  CardContent,
  CardMedia,
  Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
  People as PeopleIcon,
  Image as ImageIcon,
  Museum as ExhibitionIcon,
  AttachMoney as MoneyIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  AdminPanelSettings as AdminIcon,
  Download as DownloadIcon,
  Visibility as VisibilityIcon,
  ShoppingCart as ShoppingCartIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import axios from 'axios';

interface User {
  id: number;
  email: string;
  username: string;
  roles: string[];
  balance: number;
}

interface Artwork {
  id: number;
  title: string;
  description: string;
  price: number;
  authorId: number;
  authorName: string;
  previewUrl: string;
  isSold: boolean;
  viewsCount: number;
  createdAt: string;
}

interface DetailedStats {
  totalUsers: number;
  totalArtists: number;
  totalArtworks: number;
  totalExhibitions: number;
  totalOrders: number;
  completedOrders: number;
  pendingOrders: number;
  cancelledOrders: number;
  totalRevenue: number;
  totalSales: number;
  averageArtworkPrice: number;
  totalViews: number;
  totalFavorites: number;
  totalComments: number;
  totalSubscriptions: number;
  newUsersThisMonth: number;
  newArtworksThisMonth: number;
  salesThisMonth: number;
  topArtists: TopArtist[];
  topArtworks: TopArtwork[];
  usersByRole: { [key: string]: number };
  revenueByMonth: { [key: string]: number };
}

interface TopArtist {
  artistId: number;
  artistName: string;
  artworksCount: number;
  salesCount: number;
  totalRevenue: number;
  subscribersCount: number;
}

interface TopArtwork {
  artworkId: number;
  title: string;
  artistName: string;
  price: number;
  viewsCount: number;
  favoritesCount: number;
  isSold: boolean;
}

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

const AdminDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [users, setUsers] = useState<User[]>([]);
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [detailedStats, setDetailedStats] = useState<DetailedStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [editUserDialog, setEditUserDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newBalance, setNewBalance] = useState('');

  const [stats, setStats] = useState({
    totalUsers: 0,
    totalArtists: 0,
    totalArtworks: 0,
    totalExhibitions: 0,
    totalRevenue: 0,
  });

  useEffect(() => {
    loadData();
  }, [tabValue]);

  const loadData = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const usersResponse = await axios.get('http://localhost:8080/api/admin/users', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUsers(usersResponse.data);

      const statsResponse = await axios.get('http://localhost:8080/api/admin/statistics', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setStats(statsResponse.data);

      // Load artworks for moderation tab
      if (tabValue === 1) {
        const artworksResponse = await axios.get('http://localhost:8080/api/admin/artworks', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setArtworks(artworksResponse.data);
      }

      // Load detailed statistics for analytics tab
      if (tabValue === 2) {
        const detailedStatsResponse = await axios.get('http://localhost:8080/api/admin/statistics/detailed', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setDetailedStats(detailedStatsResponse.data);
      }
    } catch (error) {
      console.error('Failed to load admin data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setNewBalance(user.balance.toString());
    setEditUserDialog(true);
  };

  const handleUpdateUser = async () => {
    if (!selectedUser) return;

    try {
      const token = localStorage.getItem('token');
      await axios.put(
        `http://localhost:8080/api/admin/users/${selectedUser.id}`,
        { balance: parseFloat(newBalance) },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      setEditUserDialog(false);
      loadData();
    } catch (error) {
      console.error('Failed to update user:', error);
      alert('Не удалось обновить пользователя');
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!window.confirm('Вы уверены, что хотите удалить этого пользователя?')) return;

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8080/api/admin/users/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      loadData();
    } catch (error) {
      console.error('Failed to delete user:', error);
      alert('Не удалось удалить пользователя');
    }
  };

  const handleToggleRole = async (userId: number, role: string) => {
    try {
      const token = localStorage.getItem('token');
      await axios.post(
        `http://localhost:8080/api/admin/users/${userId}/toggle-role`,
        { role },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      loadData();
    } catch (error) {
      console.error('Failed to toggle role:', error);
      alert('Не удалось изменить роль');
    }
  };

  const handleDeleteArtwork = async (artworkId: number) => {
    if (!window.confirm('Вы уверены, что хотите удалить это произведение?')) return;

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8080/api/admin/artworks/${artworkId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      alert('Произведение успешно удалено');
      loadData();
    } catch (error: any) {
      console.error('Failed to delete artwork:', error);
      if (error.response?.status === 400) {
        alert('Невозможно удалить произведение: оно было продано и имеет связанные заказы. Произведения с историей продаж сохраняются для целостности данных.');
      } else {
        alert('Не удалось удалить произведение: ' + (error.response?.data?.message || error.message));
      }
    }
  };

  const handleDownloadReport = async (reportType: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`http://localhost:8080/api/admin/reports/${reportType}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${reportType}_report.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Failed to download report:', error);
      alert('Не удалось скачать отчет');
    }
  };

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
          Панель администратора
        </Typography>

        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ background: 'linear-gradient(135deg, #95BBEA 0%, #6a9dd6 100%)', color: '#FFF8E7', borderRadius: '16px', boxShadow: '0 4px 16px rgba(149, 187, 234, 0.3)' }}>
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <PeopleIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  {stats.totalUsers}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Пользователей
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)', color: '#FFF8E7', borderRadius: '16px', boxShadow: '0 4px 16px rgba(147, 5, 0, 0.3)' }}>
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <AdminIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  {stats.totalArtists}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Художников
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ background: 'linear-gradient(135deg, #95BBEA 0%, #6a9dd6 100%)', color: '#FFF8E7', borderRadius: '16px', boxShadow: '0 4px 16px rgba(149, 187, 234, 0.3)' }}>
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <ImageIcon sx={{ fontSize: 48, mb: 1 }} />
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
            <Card sx={{ background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)', color: '#FFF8E7', borderRadius: '16px', boxShadow: '0 4px 16px rgba(147, 5, 0, 0.3)' }}>
              <CardContent sx={{ textAlign: 'center', py: 3 }}>
                <ExhibitionIcon sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h3" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700, mb: 1 }}>
                  {stats.totalExhibitions}
                </Typography>
                <Typography sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500, opacity: 0.9 }}>
                  Выставок
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
            <Tab label="Управление пользователями" />
            <Tab label="Модерация произведений" />
            <Tab label="Аналитика и отчеты" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <TableContainer component={Paper} sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
            <Table>
              <TableHead>
                <TableRow sx={{ backgroundColor: 'rgba(147, 5, 0, 0.05)' }}>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>ID</TableCell>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>Имя пользователя</TableCell>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>Email</TableCell>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>Роли</TableCell>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>Баланс</TableCell>
                  <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#1a1a1a' }}>Действия</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user) => (
                  <TableRow key={user.id} sx={{ '&:hover': { backgroundColor: 'rgba(147, 5, 0, 0.02)' } }}>
                    <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>{user.id}</TableCell>
                    <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500 }}>{user.username}</TableCell>
                    <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>{user.email}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                        {user.roles.map((role) => (
                          <Chip
                            key={role}
                            label={role}
                            size="small"
                            onClick={() => handleToggleRole(user.id, role)}
                            sx={{
                              backgroundColor: role === 'ARTIST' ? 'rgba(147, 5, 0, 0.15)' : 'rgba(149, 187, 234, 0.15)',
                              color: '#1a1a1a',
                              fontFamily: "'Space Grotesk', sans-serif",
                              fontWeight: 600,
                            }}
                          />
                        ))}
                      </Box>
                    </TableCell>
                    <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#930500' }}>
                      ${user.balance.toFixed(2)}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Button
                          size="small"
                          startIcon={<EditIcon />}
                          onClick={() => handleEditUser(user)}
                          sx={{
                            color: '#95BBEA',
                            fontFamily: "'Space Grotesk', sans-serif",
                            textTransform: 'none',
                            '&:hover': { backgroundColor: 'rgba(149, 187, 234, 0.1)' },
                          }}
                        >
                          Изменить
                        </Button>
                        <Button
                          size="small"
                          startIcon={<DeleteIcon />}
                          onClick={() => handleDeleteUser(user.id)}
                          sx={{
                            color: '#930500',
                            fontFamily: "'Space Grotesk', sans-serif",
                            textTransform: 'none',
                            '&:hover': { backgroundColor: 'rgba(147, 5, 0, 0.1)' },
                          }}
                        >
                          Удалить
                        </Button>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress sx={{ color: '#930500' }} />
            </Box>
          ) : (
            <Grid container spacing={3}>
              {artworks.map((artwork) => (
                <Grid size={{ xs: 12, sm: 6, md: 4 }} key={artwork.id}>
                  <Card sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
                    <CardMedia
                      component="img"
                      height="200"
                      image={`http://localhost:8080/api/artworks/${artwork.id}/preview`}
                      alt={artwork.title}
                      sx={{ objectFit: 'cover' }}
                    />
                    <CardContent>
                      <Typography variant="h6" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, mb: 1 }}>
                        {artwork.title}
                      </Typography>
                      <Typography 
                        variant="body2" 
                        color="text.secondary" 
                        sx={{ 
                          fontFamily: "'Space Grotesk', sans-serif", 
                          mb: 1,
                          cursor: 'pointer',
                          '&:hover': {
                            color: '#930500',
                            textDecoration: 'underline',
                          }
                        }}
                        onClick={() => navigate(`/artist/${artwork.authorId}`)}
                      >
                        Автор: {artwork.authorName}
                      </Typography>
                      <Typography variant="body2" sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#930500', mb: 1 }}>
                        ${artwork.price.toFixed(2)}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                        <Chip
                          label={`${artwork.viewsCount} просмотров`}
                          size="small"
                          sx={{ fontFamily: "'Space Grotesk', sans-serif" }}
                        />
                        {artwork.isSold && (
                          <Chip
                            label="Продано"
                            size="small"
                            color="success"
                            sx={{ fontFamily: "'Space Grotesk', sans-serif" }}
                          />
                        )}
                      </Box>
                      <Button
                        fullWidth
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={() => handleDeleteArtwork(artwork.id)}
                        sx={{
                          backgroundColor: '#930500',
                          fontFamily: "'Space Grotesk', sans-serif",
                          textTransform: 'none',
                          '&:hover': { backgroundColor: '#6b0300' },
                        }}
                      >
                        Удалить
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress sx={{ color: '#930500' }} />
            </Box>
          ) : detailedStats ? (
            <>
              <Typography variant="h5" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, mb: 3 }}>
                Детальная статистика
              </Typography>

              <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Card sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <ShoppingCartIcon sx={{ fontSize: 40, color: '#930500', mb: 1 }} />
                      <Typography variant="h4" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700 }}>
                        {detailedStats.completedOrders}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>
                        Завершенных заказов
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Card sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <MoneyIcon sx={{ fontSize: 40, color: '#95BBEA', mb: 1 }} />
                      <Typography variant="h4" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700 }}>
                        ${detailedStats.totalRevenue.toFixed(2)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>
                        Общая выручка
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Card sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <VisibilityIcon sx={{ fontSize: 40, color: '#930500', mb: 1 }} />
                      <Typography variant="h4" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700 }}>
                        {detailedStats.totalViews}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>
                        Всего просмотров
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Card sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
                    <CardContent sx={{ textAlign: 'center' }}>
                      <TrendingUpIcon sx={{ fontSize: 40, color: '#95BBEA', mb: 1 }} />
                      <Typography variant="h4" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 700 }}>
                        {detailedStats.salesThisMonth}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>
                        Продаж в этом месяце
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>

              <Typography variant="h6" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, mb: 2 }}>
                Топ художников
              </Typography>
              <TableContainer component={Paper} sx={{ backgroundColor: '#FFF8E7', borderRadius: '16px', border: '1px solid rgba(147, 5, 0, 0.1)', mb: 4 }}>
                <Table>
                  <TableHead>
                    <TableRow sx={{ backgroundColor: 'rgba(147, 5, 0, 0.05)' }}>
                      <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }}>Художник</TableCell>
                      <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }}>Работ</TableCell>
                      <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }}>Продаж</TableCell>
                      <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }}>Выручка</TableCell>
                      <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600 }}>Подписчиков</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {detailedStats.topArtists.slice(0, 5).map((artist) => (
                      <TableRow key={artist.artistId}>
                        <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 500 }}>{artist.artistName}</TableCell>
                        <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>{artist.artworksCount}</TableCell>
                        <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>{artist.salesCount}</TableCell>
                        <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#930500' }}>
                          ${artist.totalRevenue.toFixed(2)}
                        </TableCell>
                        <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif" }}>{artist.subscribersCount}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Typography variant="h6" sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, mb: 2 }}>
                Экспорт отчетов
              </Typography>
              <Grid container spacing={2} sx={{ mb: 4 }}>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownloadReport('users')}
                    sx={{
                      borderColor: '#930500',
                      color: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      textTransform: 'none',
                      '&:hover': { borderColor: '#6b0300', backgroundColor: 'rgba(147, 5, 0, 0.05)' },
                    }}
                  >
                    Отчет по пользователям
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownloadReport('artworks')}
                    sx={{
                      borderColor: '#930500',
                      color: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      textTransform: 'none',
                      '&:hover': { borderColor: '#6b0300', backgroundColor: 'rgba(147, 5, 0, 0.05)' },
                    }}
                  >
                    Отчет по произведениям
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownloadReport('orders')}
                    sx={{
                      borderColor: '#930500',
                      color: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      textTransform: 'none',
                      '&:hover': { borderColor: '#6b0300', backgroundColor: 'rgba(147, 5, 0, 0.05)' },
                    }}
                  >
                    Отчет по заказам
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownloadReport('revenue')}
                    sx={{
                      borderColor: '#930500',
                      color: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      textTransform: 'none',
                      '&:hover': { borderColor: '#6b0300', backgroundColor: 'rgba(147, 5, 0, 0.05)' },
                    }}
                  >
                    Отчет по выручке
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => handleDownloadReport('artists')}
                    sx={{
                      borderColor: '#930500',
                      color: '#930500',
                      fontFamily: "'Space Grotesk', sans-serif",
                      textTransform: 'none',
                      '&:hover': { borderColor: '#6b0300', backgroundColor: 'rgba(147, 5, 0, 0.05)' },
                    }}
                  >
                    Отчет по художникам
                  </Button>
                </Grid>
              </Grid>
            </>
          ) : (
            <Alert severity="info">Загрузка статистики...</Alert>
          )}
        </TabPanel>

        <Dialog
          open={editUserDialog}
          onClose={() => setEditUserDialog(false)}
          maxWidth="sm"
          fullWidth
          slotProps={{
            paper: {
              sx: { backgroundColor: '#FFF8E7', borderRadius: '16px' }
            }
          }}
        >
          <DialogTitle sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 600, fontSize: '1.5rem', color: '#1a1a1a' }}>
            Изменить баланс пользователя
          </DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              label="Новый баланс"
              type="number"
              value={newBalance}
              onChange={(e) => setNewBalance(e.target.value)}
              margin="normal"
              sx={{
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '& fieldset': { borderColor: 'rgba(147, 5, 0, 0.2)' },
                  '&:hover fieldset': { borderColor: '#930500' },
                  '&.Mui-focused fieldset': { borderColor: '#930500' },
                },
                '& .MuiInputLabel-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '&.Mui-focused': { color: '#930500' },
                },
              }}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={() => setEditUserDialog(false)} sx={{ fontFamily: "'Space Grotesk', sans-serif", textTransform: 'none', color: '#4a4a4a' }}>
              Отмена
            </Button>
            <Button
              onClick={handleUpdateUser}
              variant="contained"
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
              Сохранить
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default AdminDashboardPage;
