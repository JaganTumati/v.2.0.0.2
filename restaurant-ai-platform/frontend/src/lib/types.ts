export type ConversationMode = 'AI_ACTIVE' | 'HUMAN_PENDING' | 'HUMAN_ACTIVE' | 'CLOSED';

export interface Conversation {
  id: string;
  customerId: string;
  customerDisplayName: string | null;
  customerWhatsappNumber: string | null;
  mode: ConversationMode;
  lastIntent: string | null;
  summary: string | null;
  updatedAt: string;
}

export interface Message {
  id: string;
  direction: 'INBOUND' | 'OUTBOUND';
  senderType: 'CUSTOMER' | 'AI' | 'AGENT';
  content: string;
  createdAt: string;
}

export interface MenuCategory {
  id: string;
  name: string;
  displayOrder: number;
  active: boolean;
}

export interface MenuItemOption {
  id: string;
  name: string;
  priceDelta: number;
  active: boolean;
}

export interface MenuItem {
  id: string;
  categoryId: string;
  categoryName: string | null;
  name: string;
  description: string | null;
  price: number;
  vegetarian: boolean;
  spicyLevel: number;
  available: boolean;
  imageUrl: string | null;
  options: MenuItemOption[];
}

export interface Lead {
  id: string;
  source: string | null;
  intentNotes: string | null;
  estimatedValue: number | null;
  status: 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'CONVERTED' | 'LOST';
  createdAt: string;
}

export interface SupportTicket {
  id: string;
  conversationId: string | null;
  orderId: string | null;
  issue: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  role: string;
  email: string;
}

export interface AnalyticsOverview {
  conversationsToday: number;
  ordersToday: number;
  revenueToday: number;
  activeOrders: number;
  openTickets: number;
  newLeads: number;
  totalConversations: number;
  conversationsCurrentlyWithHuman: number;
  aiHandledShare: number;
}

export interface MenuPopularity {
  itemName: string;
  totalQuantity: number;
}

export interface OrderLineItem {
  menuItemId: string;
  itemName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Order {
  id: string;
  status: string;
  fulfillmentType: 'DELIVERY' | 'TAKEAWAY';
  deliveryAddress: string | null;
  items: OrderLineItem[];
  subtotal: number;
  deliveryFee: number;
  discountTotal: number;
  taxTotal: number;
  total: number;
  appliedPromotionCode: string | null;
  createdAt: string;
}
