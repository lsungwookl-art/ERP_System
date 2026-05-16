import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../../api/client'

interface Summary {
  totalItems: number
  stockAlertCount: number
  pendingPurchaseOrders: number
  pendingSalesOrders: number
  pendingApprovals: number
  totalJournalEntries: number
}

export default function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    client.get('/dashboard/summary')
      .then((r) => setSummary(r.data.data))
      .catch(() => {})
  }, [])

  const cards = [
    {
      label: '등록 품목',
      value: summary?.totalItems ?? '-',
      color: 'bg-blue-500',
      icon: '📦',
      path: '/items',
    },
    {
      label: '안전재고 경보',
      value: summary?.stockAlertCount ?? '-',
      color: summary?.stockAlertCount ? 'bg-red-500' : 'bg-gray-400',
      icon: '⚠️',
      path: '/inventory/balances',
    },
    {
      label: '미처리 발주',
      value: summary?.pendingPurchaseOrders ?? '-',
      color: 'bg-orange-500',
      icon: '🛒',
      path: '/purchase/orders',
    },
    {
      label: '미처리 수주',
      value: summary?.pendingSalesOrders ?? '-',
      color: 'bg-green-500',
      icon: '📝',
      path: '/sales/orders',
    },
    {
      label: '결재 대기',
      value: summary?.pendingApprovals ?? '-',
      color: summary?.pendingApprovals ? 'bg-yellow-500' : 'bg-gray-400',
      icon: '✍️',
      path: '/approvals',
    },
    {
      label: '누적 전표',
      value: summary?.totalJournalEntries ?? '-',
      color: 'bg-purple-500',
      icon: '📑',
      path: '/accounting/journals',
    },
  ]

  return (
    <div className="space-y-6">
      {/* 통계 카드 */}
      <div className="grid grid-cols-3 gap-4">
        {cards.map((c) => (
          <div
            key={c.label}
            onClick={() => navigate(c.path)}
            className="bg-white rounded-xl shadow-sm p-5 flex items-center gap-4 border cursor-pointer hover:shadow-md transition-shadow"
          >
            <div className={`${c.color} text-white text-2xl w-12 h-12 flex items-center justify-center rounded-lg flex-shrink-0`}>
              {c.icon}
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-800">
                {typeof c.value === 'number' ? c.value.toLocaleString() : c.value}
              </p>
              <p className="text-sm text-gray-500">{c.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* 빠른 시작 + 구현 현황 */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white rounded-xl shadow-sm p-6 border">
          <h2 className="text-base font-semibold text-gray-700 mb-4">시작하기</h2>
          <ol className="space-y-2 text-sm text-gray-600">
            <li>① <strong>기준정보</strong> — 품목, 거래처, 창고 등록</li>
            <li>② <strong>구매 &gt; 발주서</strong> — 발주 등록 및 확정</li>
            <li>③ <strong>구매 &gt; 입고</strong> — 입고 처리 → 재고↑ + 전표 자동생성</li>
            <li>④ <strong>판매 &gt; 수주</strong> — 수주 등록 및 확정</li>
            <li>⑤ <strong>판매 &gt; 출고</strong> — 출고 처리 → 재고↓ + 4줄 분개</li>
            <li>⑥ <strong>회계 &gt; 원장/시산표</strong> — 자동 분개 확인</li>
          </ol>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 border">
          <h2 className="text-base font-semibold text-gray-700 mb-4">구현 현황</h2>
          <ul className="space-y-1.5 text-sm">
            {[
              { done: true, text: 'Phase 1 — JWT 인증, 기준정보 CRUD, 재고 기본' },
              { done: true, text: 'Phase 2 — 구매/판매 SCM 사이클, 자동분개' },
              { done: true, text: 'Phase 3 — 계정원장, 시산표' },
              { done: true, text: 'Phase 4 — 대시보드, BOM, 전자결재' },
            ].map((item, i) => (
              <li key={i} className="flex items-center gap-2">
                <span className={`w-4 h-4 rounded-full flex items-center justify-center text-xs flex-shrink-0 ${
                  item.done ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-400'
                }`}>
                  {item.done ? '✓' : ''}
                </span>
                <span className={item.done ? 'text-gray-700' : 'text-gray-400'}>{item.text}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
