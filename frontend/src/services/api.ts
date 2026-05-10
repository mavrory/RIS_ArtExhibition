import axios from 'axios';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  Artwork,
  ArtworkDetail,
  Exhibition,
  ExhibitionDetail,
  Order,
  Payment,
  Comment,
  WalletTransaction,
  DepositRequest
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authAPI = {
  register: (data: RegisterRequest): Promise<AuthResponse> =>
    api.post('/auth/register', data).then(res => res.data),
  
  login: (data: LoginRequest): Promise<AuthResponse> =>
    api.post('/auth/login', data).then(res => res.data),
  
  getCurrentUser: (): Promise<User> =>
    api.get('/users/me').then(res => res.data),
};

// Artwork API
export const artworkAPI = {
  getAll: (): Promise<Artwork[]> =>
    api.get('/artworks').then(res => res.data),
  
  getById: (id: number): Promise<ArtworkDetail> =>
    api.get(`/artworks/${id}`).then(res => res.data),
  
  create: (formData: FormData): Promise<Artwork> =>
    api.post('/artworks', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(res => res.data),
  
  getPreviewUrl: (id: number): string =>
    `${API_BASE_URL}/artworks/${id}/preview`,
  
  downloadOriginal: (id: number): Promise<Blob> =>
    api.get(`/artworks/${id}/image`, { responseType: 'blob' }).then(res => res.data),
  
  addComment: (id: number, content: string): Promise<Comment> =>
    api.post(`/artworks/${id}/comments`, { content }).then(res => res.data),
  
  toggleFavorite: (id: number): Promise<void> =>
    api.post(`/artworks/${id}/favorite`).then(res => res.data),
  
  getFavorites: (): Promise<Artwork[]> =>
    api.get('/artworks/favorites').then(res => res.data),
};

// Exhibition API
export const exhibitionAPI = {
  getAll: (): Promise<Exhibition[]> =>
    api.get('/exhibitions').then(res => res.data),
  
  getById: (id: number): Promise<ExhibitionDetail> =>
    api.get(`/exhibitions/${id}`).then(res => res.data),
  
  create: (data: { title: string; description: string }): Promise<Exhibition> =>
    api.post('/exhibitions', data).then(res => res.data),
  
  addArtwork: (id: number, artworkId: number): Promise<void> =>
    api.post(`/exhibitions/${id}/artworks`, { artworkId }).then(res => res.data),
};

// Order API
export const orderAPI = {
  create: (artworkId: number): Promise<Order> =>
    api.post('/orders', { artworkId }).then(res => res.data),
  
  getMyOrders: (): Promise<Order[]> =>
    api.get('/orders/my').then(res => res.data),
  
  getById: (id: number): Promise<Order> =>
    api.get(`/orders/${id}`).then(res => res.data),
};

// Payment API
export const paymentAPI = {
  process: (orderId: number, paymentMethod: string = 'WALLET'): Promise<Payment> =>
    api.post('/payments/process', { orderId, paymentMethod }).then(res => res.data),
};

// Wallet API
export const walletAPI = {
  getBalance: (): Promise<{ balance: number }> =>
    api.get('/wallet/balance').then(res => res.data),
  
  deposit: (data: DepositRequest): Promise<WalletTransaction> =>
    api.post('/wallet/deposit', data).then(res => res.data),
  
  getTransactions: (): Promise<WalletTransaction[]> =>
    api.get('/wallet/transactions').then(res => res.data),
  
  getRecentTransactions: (limit: number = 10): Promise<WalletTransaction[]> =>
    api.get(`/wallet/transactions/recent?limit=${limit}`).then(res => res.data),
};

export default api;
