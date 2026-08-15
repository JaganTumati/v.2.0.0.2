import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { RequireAuth } from './components/RequireAuth';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardHome } from './pages/DashboardHome';
import { ConversationsPage } from './pages/ConversationsPage';
import { ConversationDetailPage } from './pages/ConversationDetailPage';
import { OrdersPage } from './pages/OrdersPage';
import { MenuPage } from './pages/MenuPage';
import { LeadsPage } from './pages/LeadsPage';
import { SupportTicketsPage } from './pages/SupportTicketsPage';
import { AnalyticsPage } from './pages/AnalyticsPage';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<RequireAuth />}>
              <Route element={<Layout />}>
                <Route path="/" element={<DashboardHome />} />
                <Route path="/conversations" element={<ConversationsPage />} />
                <Route path="/conversations/:id" element={<ConversationDetailPage />} />
                <Route path="/orders" element={<OrdersPage />} />
                <Route path="/menu" element={<MenuPage />} />
                <Route path="/leads" element={<LeadsPage />} />
                <Route path="/support-tickets" element={<SupportTicketsPage />} />
                <Route path="/analytics" element={<AnalyticsPage />} />
              </Route>
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
