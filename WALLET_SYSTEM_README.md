# Система оплаты с кошельками - Инструкция по развертыванию

## Обзор изменений

Реализована полноценная система оплаты с кошельками пользователей, включающая:

1. **Backend (Java/Spring Boot)**
   - Система кошельков с балансами пользователей
   - Множественные способы оплаты (Wallet, Credit Card, Debit Card, PayPal, Bank Transfer, Cryptocurrency)
   - История транзакций
   - Проверка баланса перед покупкой
   - Автоматическое начисление средств художникам

2. **Frontend (React/TypeScript)**
   - Компонент выбора способа оплаты
   - Страница кошелька с историей транзакций
   - Пополнение баланса
   - Проверка достаточности средств при покупке

## Шаги по развертыванию

### 1. Обновление базы данных

Выполните SQL-скрипты в следующем порядке:

```bash
# 1. Добавить колонку balance (если еще не добавлена)
psql -U your_user -d your_database -f backend/add_balance_column.sql

# 2. Создать таблицу wallet_transactions и обновить payments
psql -U your_user -d your_database -f backend/add_wallet_system.sql

# 3. (Опционально) Добавить тестовые балансы
psql -U your_user -d your_database -f backend/init_test_balances.sql
```

### 2. Backend

Новые файлы и изменения:

**Новые классы:**
- `com.digitalart.payment.domain.PaymentMethod` - enum способов оплаты
- `com.digitalart.wallet.domain.WalletTransaction` - entity транзакций
- `com.digitalart.wallet.domain.TransactionType` - enum типов транзакций
- `com.digitalart.wallet.infrastructure.WalletTransactionRepository` - репозиторий
- `com.digitalart.wallet.application.UserWalletService` - сервис кошелька
- `com.digitalart.wallet.application.dto.WalletTransactionDto` - DTO
- `com.digitalart.wallet.application.dto.DepositRequest` - DTO запроса пополнения
- `com.digitalart.wallet.presentation.WalletController` - контроллер API

**Обновленные классы:**
- `PaymentService` - добавлена проверка баланса и списание средств
- `OrderService` - добавлено начисление средств художникам
- `Payment` entity - добавлены поля amount и paymentDetails

Перезапустите backend:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. Frontend

Новые файлы:
- `src/components/payment/PaymentMethodSelector.tsx` - выбор способа оплаты
- `src/components/wallet/WalletBalance.tsx` - отображение баланса
- `src/pages/WalletPage.tsx` - страница кошелька

Обновленные файлы:
- `src/types/index.ts` - добавлены типы для транзакций и способов оплаты
- `src/services/api.ts` - добавлены API методы для кошелька
- `src/pages/ArtworkDetailPage.tsx` - интегрирован выбор способа оплаты
- `src/App.tsx` - добавлен роут /wallet
- `src/components/layout/Header.tsx` - добавлена кнопка Wallet

Установите зависимости и запустите:
```bash
cd frontend
npm install
npm start
```

## Использование

### Для пользователей:

1. **Пополнение кошелька:**
   - Перейдите в раздел "Wallet" в меню
   - Нажмите "Add Funds"
   - Введите сумму и выберите способ оплаты
   - Подтвердите пополнение

2. **Покупка произведения:**
   - Выберите произведение
   - Нажмите "Purchase Artwork"
   - Выберите способ оплаты:
     - **Wallet** - оплата с баланса кошелька (требуется достаточный баланс)
     - **Credit/Debit Card, PayPal, Bank Transfer, Cryptocurrency** - внешние способы оплаты (симуляция)
   - Подтвердите оплату

3. **Просмотр истории:**
   - В разделе "Wallet" отображается полная история транзакций
   - Типы транзакций: DEPOSIT, PURCHASE, ARTIST_EARNING, REFUND

### Для художников:

- При продаже произведения средства автоматически зачисляются на кошелек художника
- История заработков доступна в разделе "Wallet"

## API Endpoints

### Wallet API

```
GET    /api/wallet/balance              - Получить баланс
POST   /api/wallet/deposit              - Пополнить баланс
GET    /api/wallet/transactions         - История транзакций
GET    /api/wallet/transactions/recent  - Последние транзакции
```

### Payment API (обновлен)

```
POST   /api/payments/process            - Обработать платеж
Body: { orderId: number, paymentMethod: string }
```

## Способы оплаты

1. **WALLET** - Оплата с баланса кошелька
   - Требуется достаточный баланс
   - Мгновенное списание средств
   - Средства сразу зачисляются художнику

2. **CREDIT_CARD** - Кредитная карта (симуляция)
3. **DEBIT_CARD** - Дебетовая карта (симуляция)
4. **PAYPAL** - PayPal (симуляция)
5. **BANK_TRANSFER** - Банковский перевод (симуляция)
6. **CRYPTOCURRENCY** - Криптовалюта (симуляция)

Для внешних способов оплаты (2-6) симулируется задержка в 2 секунды для имитации обработки платежа.

## Безопасность

- Все операции с кошельком требуют аутентификации
- Проверка прав доступа на уровне сервисов
- Транзакционная целостность при переводе средств
- Валидация сумм (минимум $0.01, максимум $10,000 для пополнения)

## Тестирование

1. Создайте тестового пользователя
2. Пополните кошелек через страницу Wallet
3. Попробуйте купить произведение с недостаточным балансом (должна появиться ошибка)
4. Пополните баланс и совершите покупку
5. Проверьте историю транзакций
6. Войдите как художник и проверьте начисление средств

## Примечания

- В production необходимо интегрировать реальные платежные системы (Stripe, PayPal API и т.д.)
- Добавить обработку ошибок платежей и возвратов
- Реализовать систему уведомлений о транзакциях
- Добавить лимиты на операции и защиту от мошенничества
