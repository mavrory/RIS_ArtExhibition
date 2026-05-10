import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  Grid,
} from '@mui/material';
import { Add, TrendingUp, TrendingDown } from '@mui/icons-material';
import { WalletBalance } from '../components/wallet/WalletBalance';
import { walletAPI } from '../services/api';
import { WalletTransaction, PaymentMethodType } from '../types';
import { PaymentMethodSelector } from '../components/payment/PaymentMethodSelector';

const WalletPage: React.FC = () => {
  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [depositDialogOpen, setDepositDialogOpen] = useState(false);
  const [depositAmount, setDepositAmount] = useState<string>('');
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<string>(PaymentMethodType.CREDIT_CARD);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [refreshBalance, setRefreshBalance] = useState(0);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const data = await walletAPI.getTransactions();
      setTransactions(data);
    } catch (err: any) {
      console.error('Failed to fetch transactions:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleDepositClick = () => {
    setDepositDialogOpen(true);
    setError(null);
    setSuccess(null);
    setDepositAmount('');
  };

  const handleDeposit = async () => {
    const amount = parseFloat(depositAmount);

    if (isNaN(amount) || amount <= 0) {
      setError('Введите корректную сумму');
      return;
    }

    if (amount > 10000) {
      setError('Максимальная сумма пополнения: $10,000');
      return;
    }

    setProcessing(true);
    setError(null);

    try {
      await walletAPI.deposit({
        amount,
        paymentMethod: selectedPaymentMethod,
      });

      setSuccess(`Успешно пополнено $${amount.toFixed(2)} на ваш кошелек`);
      setDepositDialogOpen(false);
      
      // Refresh transactions and balance
      await fetchTransactions();
      setRefreshBalance(prev => prev + 1);
      
      // Show success message
      setTimeout(() => setSuccess(null), 5000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обработать пополнение');
    } finally {
      setProcessing(false);
    }
  };

  const getTransactionColor = (type: string) => {
    switch (type) {
      case 'DEPOSIT':
        return 'success';
      case 'PURCHASE':
        return 'error';
      case 'ARTIST_EARNING':
        return 'success';
      case 'REFUND':
        return 'info';
      default:
        return 'default';
    }
  };

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case 'DEPOSIT':
      case 'ARTIST_EARNING':
      case 'REFUND':
        return <TrendingUp />;
      case 'PURCHASE':
        return <TrendingDown />;
      default:
        return undefined;
    }
  };

  const getTransactionTypeLabel = (type: string) => {
    switch (type) {
      case 'DEPOSIT':
        return 'Пополнение';
      case 'PURCHASE':
        return 'Покупка';
      case 'ARTIST_EARNING':
        return 'Доход художника';
      case 'REFUND':
        return 'Возврат';
      default:
        return type;
    }
  };

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
          Мой кошелек
        </Typography>

        {success && (
          <Alert 
            severity="success" 
            sx={{ 
              mb: 3,
              backgroundColor: 'rgba(149, 187, 234, 0.15)',
              color: '#1a1a1a',
              fontFamily: "'Space Grotesk', sans-serif",
              borderRadius: '12px',
              border: '1px solid rgba(149, 187, 234, 0.3)',
            }} 
            onClose={() => setSuccess(null)}
          >
            {success}
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid size={{ xs: 12 }}>
            <WalletBalance onDepositClick={handleDepositClick} refreshTrigger={refreshBalance} />
          </Grid>

          <Grid size={{ xs: 12 }}>
            <Card
              sx={{
                backgroundColor: '#FFF8E7',
                borderRadius: '16px',
                border: '1px solid rgba(147, 5, 0, 0.1)',
                boxShadow: '0 4px 16px rgba(147, 5, 0, 0.08)',
              }}
            >
              <CardContent sx={{ p: 4 }}>
                <Typography
                  variant="h5"
                  sx={{
                    mb: 3,
                    fontFamily: "'Playfair Display', serif",
                    fontWeight: 600,
                    color: '#1a1a1a',
                  }}
                >
                  История транзакций
                </Typography>

                {loading ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress sx={{ color: '#930500' }} />
                  </Box>
                ) : transactions.length === 0 ? (
                  <Box sx={{ textAlign: 'center', py: 6 }}>
                    <Typography
                      variant="body1"
                      sx={{
                        color: '#7a7a7a',
                        fontFamily: "'Space Grotesk', sans-serif",
                        mb: 3,
                      }}
                    >
                      Пока нет транзакций
                    </Typography>
                    <Button
                      variant="contained"
                      startIcon={<Add />}
                      onClick={handleDepositClick}
                      sx={{
                        backgroundColor: '#930500',
                        color: '#FFF8E7',
                        fontFamily: "'Space Grotesk', sans-serif",
                        fontWeight: 600,
                        textTransform: 'none',
                        px: 3,
                        py: 1.5,
                        borderRadius: '10px',
                        '&:hover': {
                          backgroundColor: '#6b0300',
                        },
                      }}
                    >
                      Первое пополнение
                    </Button>
                  </Box>
                ) : (
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#4a4a4a' }}>
                            Дата
                          </TableCell>
                          <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#4a4a4a' }}>
                            Тип
                          </TableCell>
                          <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#4a4a4a' }}>
                            Описание
                          </TableCell>
                          <TableCell align="right" sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#4a4a4a' }}>
                            Сумма
                          </TableCell>
                          <TableCell align="right" sx={{ fontFamily: "'Space Grotesk', sans-serif", fontWeight: 600, color: '#4a4a4a' }}>
                            Баланс после
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {transactions.map((transaction) => (
                          <TableRow 
                            key={transaction.id}
                            sx={{
                              '&:hover': {
                                backgroundColor: 'rgba(147, 5, 0, 0.02)',
                              },
                            }}
                          >
                            <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                              {new Date(transaction.createdAt).toLocaleString('ru-RU')}
                            </TableCell>
                            <TableCell>
                              <Chip
                                icon={getTransactionIcon(transaction.transactionType)}
                                label={getTransactionTypeLabel(transaction.transactionType)}
                                color={getTransactionColor(transaction.transactionType)}
                                size="small"
                                sx={{
                                  fontFamily: "'Space Grotesk', sans-serif",
                                  fontWeight: 500,
                                }}
                              />
                            </TableCell>
                            <TableCell sx={{ fontFamily: "'Space Grotesk', sans-serif", color: '#4a4a4a' }}>
                              {transaction.description}
                            </TableCell>
                            <TableCell
                              align="right"
                              sx={{
                                color: transaction.amount >= 0 ? '#2e7d32' : '#930500',
                                fontWeight: 700,
                                fontFamily: "'Space Grotesk', sans-serif",
                              }}
                            >
                              {transaction.amount >= 0 ? '+' : ''}${transaction.amount.toFixed(2)}
                            </TableCell>
                            <TableCell 
                              align="right"
                              sx={{
                                fontFamily: "'Space Grotesk', sans-serif",
                                fontWeight: 600,
                                color: '#1a1a1a',
                              }}
                            >
                              ${transaction.balanceAfter.toFixed(2)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Dialog
          open={depositDialogOpen}
          onClose={() => !processing && setDepositDialogOpen(false)}
          maxWidth="sm"
          fullWidth
          slotProps={{
            paper: {
              sx: {
                backgroundColor: '#FFF8E7',
                borderRadius: '16px',
              }
            }
          }}
        >
          <DialogTitle
            sx={{
              fontFamily: "'Playfair Display', serif",
              fontWeight: 600,
              fontSize: '1.5rem',
              color: '#1a1a1a',
            }}
          >
            Пополнить кошелек
          </DialogTitle>
          <DialogContent>
            {error && (
              <Alert 
                severity="error" 
                sx={{ 
                  mb: 2,
                  fontFamily: "'Space Grotesk', sans-serif",
                  borderRadius: '10px',
                }}
              >
                {error}
              </Alert>
            )}

            <TextField
              fullWidth
              label="Сумма"
              type="number"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              placeholder="0.00"
              slotProps={{
                htmlInput: { min: 0.01, max: 10000, step: 0.01 }
              }}
              sx={{
                mb: 3,
                mt: 2,
                '& .MuiOutlinedInput-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '& fieldset': {
                    borderColor: 'rgba(147, 5, 0, 0.2)',
                  },
                  '&:hover fieldset': {
                    borderColor: '#930500',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#930500',
                  },
                },
                '& .MuiInputLabel-root': {
                  fontFamily: "'Space Grotesk', sans-serif",
                  '&.Mui-focused': {
                    color: '#930500',
                  },
                },
              }}
              helperText="Минимум: $0.01, Максимум: $10,000"
            />

            <PaymentMethodSelector
              selectedMethod={selectedPaymentMethod}
              onMethodChange={setSelectedPaymentMethod}
            />

            <Alert 
              severity="info" 
              sx={{ 
                mt: 2,
                fontFamily: "'Space Grotesk', sans-serif",
                backgroundColor: 'rgba(149, 187, 234, 0.15)',
                color: '#1a1a1a',
                borderRadius: '10px',
                border: '1px solid rgba(149, 187, 234, 0.3)',
              }}
            >
              Это симуляция пополнения. В реальной системе вы будете перенаправлены к платежному провайдеру.
            </Alert>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button 
              onClick={() => setDepositDialogOpen(false)} 
              disabled={processing}
              sx={{
                fontFamily: "'Space Grotesk', sans-serif",
                textTransform: 'none',
                color: '#4a4a4a',
              }}
            >
              Отмена
            </Button>
            <Button
              onClick={handleDeposit}
              variant="contained"
              disabled={processing || !depositAmount}
              startIcon={processing && <CircularProgress size={20} />}
              sx={{
                backgroundColor: '#930500',
                color: '#FFF8E7',
                fontFamily: "'Space Grotesk', sans-serif",
                fontWeight: 600,
                textTransform: 'none',
                px: 3,
                borderRadius: '10px',
                '&:hover': {
                  backgroundColor: '#6b0300',
                },
              }}
            >
              {processing ? 'Обработка...' : 'Пополнить'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default WalletPage;
