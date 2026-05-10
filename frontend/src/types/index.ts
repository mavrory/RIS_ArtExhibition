export interface User {
  id: number;
  email: string;
  username: string;
  roles: string[];
  balance?: number;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  isArtist?: boolean;
}

export interface Artwork {
  id: number;
  authorId: number;
  artistName?: string;
  authorName: string;
  title: string;
  description: string;
  price: number;
  previewUrl: string;
  isSold: boolean;
  favoritesCount: number;
  viewsCount: number;
  isFavorited: boolean;
  createdAt: string;
}

export interface ArtworkDetail extends Artwork {
  imageUrl: string;
  isPurchased: boolean;
  comments: Comment[];
}

export interface Comment {
  id: number;
  userId: number;
  username: string;
  content: string;
  createdAt: string;
}

export interface Exhibition {
  id: number;
  title: string;
  description: string;
  createdBy: number;
  curatorName: string;
  creatorName: string;
  artworkCount?: number;
  artworksCount?: number;
  coverImageUrl?: string;
  createdAt: string;
}

export interface ExhibitionDetail extends Exhibition {
  artworks: Artwork[];
}

export interface Order {
  id: number;
  userId: number;
  artworkId: number;
  artworkTitle: string;
  status: string;
  totalPrice: number;
  createdAt: string;
}

export interface Payment {
  id: number;
  orderId: number;
  status: string;
  paymentMethod: string;
  transactionId: string;
  amount?: number;
  createdAt: string;
}

export interface WalletTransaction {
  id: number;
  userId: number;
  amount: number;
  transactionType: string;
  description: string;
  orderId?: number;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
}

export interface DepositRequest {
  amount: number;
  paymentMethod: string;
}

export enum PaymentMethodType {
  WALLET = 'WALLET',
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  CRYPTOCURRENCY = 'CRYPTOCURRENCY'
}

export interface Subscription {
  id: number;
  subscriberId: number;
  artistId: number;
  artistName?: string;
  subscriberName?: string;
  createdAt: string;
}

export interface ArtistProfile {
  id: number;
  username: string;
  email: string;
  roles: string[];
  artworksCount: number;
  subscribersCount: number;
  totalViews: number;
  totalSales: number;
}
