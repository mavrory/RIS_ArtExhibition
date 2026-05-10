-- Скрипт для добавления начального баланса пользователям для тестирования

-- Добавить баланс существующим пользователям (если они есть)
UPDATE users SET balance = 1000.0 WHERE email LIKE '%@example.com';
UPDATE users SET balance = 500.0 WHERE roles @> ARRAY['USER'];
UPDATE users SET balance = 2000.0 WHERE roles @> ARRAY['ARTIST'];

-- Примеры добавления тестовых транзакций (опционально)
-- INSERT INTO wallet_transactions (user_id, amount, transaction_type, description, balance_before, balance_after)
-- VALUES 
-- (1, 1000.00, 'DEPOSIT', 'Initial deposit via Credit Card', 0.00, 1000.00),
-- (2, 500.00, 'DEPOSIT', 'Initial deposit via PayPal', 0.00, 500.00);
