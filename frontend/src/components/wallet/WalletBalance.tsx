import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Button, CircularProgress, Alert } from '@mui/material';
import { AccountBalanceWallet, Add } from '@mui/icons-material';
import { walletAPI } from '../../services/api';

interface WalletBalanceProps {
  onDepositClick?: () => void;
  showDepositButton?: boolean;
  refreshTrigger?: number;
}

export const WalletBalance: React.FC<WalletBalanceProps> = ({ 
  onDepositClick, 
  showDepositButton = true,
  refreshTrigger = 0
}) => {
  const [balance, setBalance] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchBalance = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await walletAPI.getBalance();
      setBalance(response.balance);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить баланс');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBalance();
  }, [refreshTrigger]);

  if (loading) {
    return (
      <Card sx={{ backgroundColor: '#FFF8E7', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
        <CardContent sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 3 }}>
          <CircularProgress size={24} sx={{ color: '#930500' }} />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card sx={{ backgroundColor: '#FFF8E7', border: '1px solid rgba(147, 5, 0, 0.1)' }}>
        <CardContent>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card 
      sx={{ 
        background: 'linear-gradient(135deg, #930500 0%, #6b0300 100%)',
        borderRadius: '16px',
        boxShadow: '0 8px 24px rgba(147, 5, 0, 0.25)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: '-50%',
          right: '-20%',
          width: '300px',
          height: '300px',
          borderRadius: '50%',
          background: 'rgba(149, 187, 234, 0.15)',
          filter: 'blur(60px)',
        },
      }}
    >
      <CardContent sx={{ position: 'relative', zIndex: 1, py: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              sx={{
                width: 60,
                height: 60,
                borderRadius: '12px',
                backgroundColor: 'rgba(255, 248, 231, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <AccountBalanceWallet sx={{ fontSize: 32, color: '#FFF8E7' }} />
            </Box>
            <Box>
              <Typography 
                variant="body2" 
                sx={{ 
                  color: '#95BBEA',
                  fontFamily: "'Space Grotesk', sans-serif",
                  fontWeight: 500,
                  fontSize: '0.9rem',
                  mb: 0.5,
                }}
              >
                Баланс кошелька
              </Typography>
              <Typography 
                variant="h3" 
                sx={{ 
                  color: '#FFF8E7', 
                  fontWeight: 700,
                  fontFamily: "'Playfair Display', serif",
                  fontSize: '2.5rem',
                }}
              >
                ${balance?.toFixed(2) || '0.00'}
              </Typography>
            </Box>
          </Box>
          {showDepositButton && (
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={onDepositClick}
              sx={{
                bgcolor: '#FFF8E7',
                color: '#930500',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                py: 1.5,
                borderRadius: '10px',
                boxShadow: 'none',
                '&:hover': {
                  bgcolor: '#fff',
                  boxShadow: '0 4px 12px rgba(255, 248, 231, 0.3)',
                },
              }}
            >
              Пополнить
            </Button>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
