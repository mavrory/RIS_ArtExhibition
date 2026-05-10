import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  Alert,
  Chip,
} from '@mui/material';
import {
  AccountBalanceWallet,
  CreditCard,
  AccountBalance,
  CurrencyBitcoin,
} from '@mui/icons-material';
import { PaymentMethodType } from '../../types';

interface PaymentMethodSelectorProps {
  selectedMethod: string;
  onMethodChange: (method: string) => void;
  userBalance?: number;
  requiredAmount?: number;
}

const paymentMethods = [
  {
    value: PaymentMethodType.CREDIT_CARD,
    label: 'Credit Card',
    icon: <CreditCard />,
    description: 'Visa, Mastercard, American Express',
  },
  {
    value: PaymentMethodType.DEBIT_CARD,
    label: 'Debit Card',
    icon: <CreditCard />,
    description: 'Direct payment from your bank account',
  },
  {
    value: PaymentMethodType.PAYPAL,
    label: 'PayPal',
    icon: <AccountBalance />,
    description: 'Pay securely with PayPal',
  },
  {
    value: PaymentMethodType.BANK_TRANSFER,
    label: 'Bank Transfer',
    icon: <AccountBalance />,
    description: 'Direct bank transfer',
  },
  {
    value: PaymentMethodType.CRYPTOCURRENCY,
    label: 'Cryptocurrency',
    icon: <CurrencyBitcoin />,
    description: 'Bitcoin, Ethereum, and more',
  },
];

export const PaymentMethodSelector: React.FC<PaymentMethodSelectorProps> = ({
  selectedMethod,
  onMethodChange,
  userBalance = 0,
  requiredAmount = 0,
}) => {
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Select Payment Method
      </Typography>

      <FormControl component="fieldset" fullWidth>
        <RadioGroup value={selectedMethod} onChange={(e) => onMethodChange(e.target.value)}>
          {paymentMethods.map((method) => {
            return (
              <Card
                key={method.value}
                sx={{
                  mb: 2,
                  cursor: 'pointer',
                  border: selectedMethod === method.value ? '2px solid' : '1px solid',
                  borderColor: selectedMethod === method.value ? 'primary.main' : 'divider',
                  '&:hover': {
                    borderColor: 'primary.main',
                  },
                }}
                onClick={() => onMethodChange(method.value)}
              >
                <CardContent>
                  <FormControlLabel
                    value={method.value}
                    control={<Radio />}
                    label={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
                        <Box sx={{ color: 'primary.main' }}>{method.icon}</Box>
                        <Box sx={{ flex: 1 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                            {method.label}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {method.description}
                          </Typography>
                        </Box>
                      </Box>
                    }
                    sx={{ width: '100%', m: 0 }}
                  />
                </CardContent>
              </Card>
            );
          })}
        </RadioGroup>
      </FormControl>
    </Box>
  );
};
