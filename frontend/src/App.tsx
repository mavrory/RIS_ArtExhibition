import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/layout/Layout';
import theme from './theme/theme';

// Pages
import HomePage from './pages/HomePage';
import ArtworksPage from './pages/ArtworksPage';
import ExhibitionsPage from './pages/ExhibitionsPage';
import ExhibitionDetailPage from './pages/ExhibitionDetailPage';
import ArtworkDetailPage from './pages/ArtworkDetailPage';
import MyCollectionPage from './pages/MyCollectionPage';
import WalletPage from './pages/WalletPage';
import LoginForm from './components/auth/LoginForm';
import RegisterForm from './components/auth/RegisterForm';
import OAuth2CallbackPage from './pages/OAuth2CallbackPage';
import OAuth2RoleSelectionPage from './pages/OAuth2RoleSelectionPage';
import ArtistDashboardPage from './pages/ArtistDashboardPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import ArtistProfilePage from './pages/ArtistProfilePage';
import MySubscriptionsPage from './pages/MySubscriptionsPage';
import SearchArtistsPage from './pages/SearchArtistsPage';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Layout>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/artworks" element={<ArtworksPage />} />
              <Route path="/exhibitions" element={<ExhibitionsPage />} />
              <Route path="/exhibitions/:id" element={<ExhibitionDetailPage />} />
              <Route path="/artworks/:id" element={<ArtworkDetailPage />} />
              <Route path="/my-collection" element={<MyCollectionPage />} />
              <Route path="/wallet" element={<WalletPage />} />
              <Route path="/artist-dashboard" element={<ArtistDashboardPage />} />
              <Route path="/admin-dashboard" element={<AdminDashboardPage />} />
              <Route path="/artist/:artistId" element={<ArtistProfilePage />} />
              <Route path="/my-subscriptions" element={<MySubscriptionsPage />} />
              <Route path="/search-artists" element={<SearchArtistsPage />} />
              <Route path="/login" element={<LoginForm />} />
              <Route path="/register" element={<RegisterForm />} />
              <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
              <Route path="/oauth2/role-selection" element={<OAuth2RoleSelectionPage />} />
            </Routes>
          </Layout>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
