# 📋 Памятка - Система оплаты с кошельками

## Быстрая справка для использования

---

## 🚀 Запуск (3 команды)

```bash
# 1. База данных (если еще не выполнено)
psql -U postgres -d your_database -f backend/add_wallet_system.sql

# 2. Backend
cd backend && mvn spring-boot:run

# 3. Frontend
cd frontend && npm start
```

---

## 💡 Как это работает

### Пользователь хочет купить произведение за $50:

**Шаг 1: Пополнение**
```
Wallet → Add Funds → Credit Card → $100 → Deposit
✅ Баланс: $100
```

**Шаг 2: Покупка**
```
Artworks → Выбрать работу → Purchase Artwork → Pay Now
✅ Баланс: $50
✅ Произведение куплено
```

**Шаг 3: Художник получает**
```
Artist Dashboard → Current Balance: $50
✅ Деньги зачислены автоматически
```

---

## 🎯 Основные страницы

| Страница | URL | Что там |
|----------|-----|---------|
| Кошелек | `/wallet` | Баланс, пополнение, история |
| Произведения | `/artworks` | Каталог для покупки |
| Дашборд художника | `/artist-dashboard` | Статистика, баланс |
| Детали работы | `/artworks/:id` | Просмотр и покупка |

---

## 💰 Типы транзакций

| Тип | Значок | Когда |
|-----|--------|-------|
| DEPOSIT | 🟢 ↑ | Пополнение кошелька |
| PURCHASE | 🔴 ↓ | Покупка произведения |
| ARTIST_EARNING | 🟢 ↑ | Продажа произведения |

---

## 🎨 Способы пополнения

1. 💳 Credit Card
2. 💳 Debit Card
3. 💰 PayPal
4. 🏦 Bank Transfer
5. ₿ Cryptocurrency

---

## ⚠️ Важно помнить

✅ **Покупка ТОЛЬКО через кошелек**
- Нельзя купить напрямую картой
- Сначала пополните кошелек

✅ **Баланс обновляется сразу**
- Не нужно перезагружать страницу

✅ **Художники получают деньги автоматически**
- При продаже средства сразу на кошельке

---

## 🐛 Частые вопросы

**Q: Не могу купить произведение**
A: Проверьте баланс. Пополните через Wallet → Add Funds

**Q: Баланс не обновился**
A: Обновите страницу или проверьте консоль (F12)

**Q: Где мои деньги как художник?**
A: Artist Dashboard → Current Balance (кликните на карточку)

**Q: Как вывести деньги?**
A: Пока не реализовано (для будущих версий)

---

## 📊 API Endpoints

```
GET  /api/wallet/balance              - Баланс
POST /api/wallet/deposit              - Пополнить
GET  /api/wallet/transactions         - История
POST /api/payments/process            - Оплата
POST /api/orders                      - Создать заказ
```

---

## 🔧 Если что-то сломалось

### Backend не запускается
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend не компилируется
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

### База данных
```sql
-- Проверить баланс
SELECT username, balance FROM users;

-- Проверить транзакции
SELECT * FROM wallet_transactions ORDER BY created_at DESC LIMIT 10;
```

---

## 📚 Документация

| Файл | Для чего |
|------|----------|
| **QUICK_START.md** | Быстрый старт |
| **PROJECT_COMPLETE.md** | Полная информация |
| **FINAL_CHECKLIST.md** | Что сделано |

---

## ✅ Чек-лист перед демонстрацией

- [ ] Backend запущен (http://localhost:8080)
- [ ] Frontend запущен (http://localhost:3000)
- [ ] База данных обновлена
- [ ] Есть тестовый пользователь
- [ ] Есть тестовые произведения
- [ ] Баланс пополнен для теста

---

## 🎯 Демонстрация (5 минут)

1. **Показать кошелек** (30 сек)
   - Wallet → Баланс $0
   
2. **Пополнить** (1 мин)
   - Add Funds → Credit Card → $100
   - Показать обновление баланса
   
3. **Купить** (1 мин)
   - Artworks → Выбрать → Purchase
   - Показать списание
   
4. **История** (1 мин)
   - Wallet → Transaction History
   - Показать DEPOSIT и PURCHASE
   
5. **Художник** (1.5 мин)
   - Artist Dashboard → Current Balance
   - Показать ARTIST_EARNING

---

## 💾 Резервная копия

Перед демонстрацией сделайте backup БД:
```bash
pg_dump -U postgres your_database > backup.sql
```

Восстановление:
```bash
psql -U postgres your_database < backup.sql
```

---

## 🎉 Готово!

Система работает и готова к использованию!

**Версия:** 2.1  
**Дата:** 2026-05-01  
**Статус:** ✅ Готово

---

**Удачи с проектом!** 🚀
