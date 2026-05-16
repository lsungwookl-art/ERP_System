import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

const menuItems = [
  { label: '대시보드', path: '/', icon: '📊' },
  { label: '─ 기준정보 ─', type: 'header' },
  { label: '품목관리', path: '/items', icon: '📦' },
  { label: '거래처', path: '/partners', icon: '🏢' },
  { label: '창고', path: '/warehouses', icon: '🏭' },
  { label: '─ 재고 ─', type: 'header' },
  { label: '현재고', path: '/inventory/balances', icon: '📋' },
  { label: '이동이력', path: '/inventory/movements', icon: '🔄' },
  { label: '─ 구매 ─', type: 'header' },
  { label: '발주서', path: '/purchase/orders', icon: '🛒' },
  { label: '입고', path: '/purchase/receipts', icon: '📥' },
  { label: '─ 판매 ─', type: 'header' },
  { label: '수주', path: '/sales/orders', icon: '📝' },
  { label: '출고', path: '/sales/shipments', icon: '📤' },
  { label: '─ 생산 ─', type: 'header' },
  { label: 'BOM', path: '/bom', icon: '🔩' },
  { label: '전자결재', path: '/approvals', icon: '✍️' },
  { label: '─ 회계 ─', type: 'header' },
  { label: '전표', path: '/accounting/journals', icon: '📑' },
  { label: '계정원장', path: '/accounting/ledger', icon: '📒' },
  { label: '시산표', path: '/accounting/trial-balance', icon: '⚖️' },
]

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50">
      {/* Sidebar */}
      <aside
        className={`${collapsed ? 'w-14' : 'w-52'} transition-all duration-200 bg-slate-800 text-white flex flex-col flex-shrink-0`}
      >
        <div className="flex items-center justify-between p-3 border-b border-slate-700">
          {!collapsed && <span className="font-bold text-sm text-blue-300">ERP System</span>}
          <button onClick={() => setCollapsed(!collapsed)} className="text-slate-400 hover:text-white text-lg">
            {collapsed ? '→' : '←'}
          </button>
        </div>

        <nav className="flex-1 overflow-y-auto py-2">
          {menuItems.map((item, idx) => {
            if (item.type === 'header') {
              return collapsed ? null : (
                <div key={idx} className="px-3 pt-4 pb-1 text-xs text-slate-500 font-medium">
                  {item.label}
                </div>
              )
            }
            const isActive = location.pathname === item.path
            return (
              <Link
                key={item.path}
                to={item.path!}
                className={`flex items-center gap-2 px-3 py-2 text-sm transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                }`}
              >
                <span>{item.icon}</span>
                {!collapsed && <span>{item.label}</span>}
              </Link>
            )
          })}
        </nav>

        <div className="p-3 border-t border-slate-700 text-xs text-slate-400">
          {!collapsed && (
            <>
              <div className="font-medium text-white">{user?.name}</div>
              <div className="truncate">{user?.companyName}</div>
            </>
          )}
          <button onClick={handleLogout} className="mt-2 text-red-400 hover:text-red-300 text-xs">
            {collapsed ? '⬚' : '로그아웃'}
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 overflow-auto">
        <header className="bg-white border-b px-6 py-3 flex items-center justify-between shadow-sm">
          <h1 className="text-lg font-semibold text-gray-700">
            {menuItems.find((m) => m.path === location.pathname)?.label ?? 'ERP'}
          </h1>
          <span className="text-sm text-gray-500">{user?.email}</span>
        </header>
        <div className="p-6">{children}</div>
      </main>
    </div>
  )
}
