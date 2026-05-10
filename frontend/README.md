# Digital Art Gallery - Frontend

React + TypeScript frontend для платформы цифрового искусства.

## Технологии

- React 19
- TypeScript
- Material-UI
- Axios
- React Router

## Установка

```bash
npm install
```

## Запуск

```bash
npm start
```

Приложение откроется на `http://localhost:3000`

## Структура проекта

```
src/
├── components/
│   ├── auth/           # Компоненты авторизации
│   ├── artwork/        # Компоненты работ
│   ├── exhibition/     # Компоненты выставок
│   ├── layout/         # Layout компоненты
│   └── common/         # Общие компоненты
├── pages/              # Страницы приложения
├── context/            # React Context (Auth)
├── services/           # API сервисы
├── types/              # TypeScript типы
└── theme/              # Material-UI тема
```

## Основные страницы

- `/` - Главная страница
- `/exhibitions` - Список выставок
- `/exhibitions/:id` - Детали выставки
- `/artworks/:id` - Детали работы
- `/my-collection` - Моя коллекция
- `/login` - Вход
- `/register` - Регистрация

## Особенности

### Авторизация
- JWT токен хранится в localStorage
- Автоматическое добавление токена в заголовки запросов
- AuthContext для управления состоянием авторизации

### Artwork
- Preview изображения для всех пользователей
- Оригинал доступен только после покупки
- Комментарии и лайки
- Скачивание оригинала

### Purchase Flow
1. Клик "Purchase Artwork"
2. Создание заказа (Order)
3. Модальное окно оплаты
4. Mock обработка платежа (2 сек задержка)
5. Доступ к оригиналу

## API Integration

Backend должен быть запущен на `http://localhost:8080`

Все запросы идут через Axios instance с автоматическим добавлением JWT токена.
