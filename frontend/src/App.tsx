import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import ItemsPage from './pages/master/ItemsPage'
import PartnersPage from './pages/master/PartnersPage'
import WarehousesPage from './pages/master/WarehousesPage'
import BalancesPage from './pages/inventory/BalancesPage'
import MovementsPage from './pages/inventory/MovementsPage'
import PurchaseOrdersPage from './pages/purchase/PurchaseOrdersPage'
import PurchaseReceiptsPage from './pages/purchase/PurchaseReceiptsPage'
import SalesOrdersPage from './pages/sales/SalesOrdersPage'
import SalesShipmentsPage from './pages/sales/SalesShipmentsPage'
import JournalsPage from './pages/accounting/JournalsPage'
import LedgerPage from './pages/accounting/LedgerPage'
import TrialBalancePage from './pages/accounting/TrialBalancePage'
import BomPage from './pages/bom/BomPage'
import ApprovalPage from './pages/approval/ApprovalPage'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated())
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <PrivateRoute>
      <Layout>{children}</Layout>
    </PrivateRoute>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<AppLayout><DashboardPage /></AppLayout>} />
        <Route path="/items" element={<AppLayout><ItemsPage /></AppLayout>} />
        <Route path="/partners" element={<AppLayout><PartnersPage /></AppLayout>} />
        <Route path="/warehouses" element={<AppLayout><WarehousesPage /></AppLayout>} />
        <Route path="/inventory/balances" element={<AppLayout><BalancesPage /></AppLayout>} />
        <Route path="/inventory/movements" element={<AppLayout><MovementsPage /></AppLayout>} />
        <Route path="/purchase/orders" element={<AppLayout><PurchaseOrdersPage /></AppLayout>} />
        <Route path="/purchase/receipts" element={<AppLayout><PurchaseReceiptsPage /></AppLayout>} />
        <Route path="/sales/orders" element={<AppLayout><SalesOrdersPage /></AppLayout>} />
        <Route path="/sales/shipments" element={<AppLayout><SalesShipmentsPage /></AppLayout>} />
        <Route path="/accounting/journals" element={<AppLayout><JournalsPage /></AppLayout>} />
        <Route path="/accounting/ledger" element={<AppLayout><LedgerPage /></AppLayout>} />
        <Route path="/accounting/trial-balance" element={<AppLayout><TrialBalancePage /></AppLayout>} />
        <Route path="/bom" element={<AppLayout><BomPage /></AppLayout>} />
        <Route path="/approvals" element={<AppLayout><ApprovalPage /></AppLayout>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
