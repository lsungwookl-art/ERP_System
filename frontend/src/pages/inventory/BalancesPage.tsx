import { useEffect, useState } from 'react'
import client from '../../api/client'

interface StockBalanceDto {
  id: number
  itemId: number
  itemCode: string
  itemName: string
  unit: string
  warehouseId: number
  warehouseName: string
  qty: number
  avgCost: number
  stockValue: number
  safetyStock: number
  belowSafety: boolean
  lastMovedAt: string
}

export default function BalancesPage() {
  const [balances, setBalances] = useState<StockBalanceDto[]>([])
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')

  const fetchBalances = async () => {
    setLoading(true)
    try {
      const res = await client.get('/inventory/balances', { params: { size: 200 } })
      setBalances(res.data.data.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchBalances() }, [])

  const filtered = balances.filter((b) =>
    !keyword ||
    b.itemName.includes(keyword) ||
    b.itemCode.includes(keyword) ||
    b.warehouseName.includes(keyword)
  )

  const alertCount = balances.filter((b) => b.belowSafety).length

  return (
    <div className="space-y-4">
      {/* 상단 필터 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex flex-wrap gap-3 items-center justify-between">
        <div className="flex gap-3 items-center">
          <input
            type="text"
            placeholder="품목명, 품목코드, 창고명 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-60"
          />
          <button
            onClick={fetchBalances}
            className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
          >
            새로고침
          </button>
        </div>
        <div className="flex gap-3 text-sm">
          <span className="text-gray-500">전체 <strong>{balances.length}</strong>건</span>
          {alertCount > 0 && (
            <span className="text-red-600 font-medium">⚠️ 안전재고 미달 {alertCount}건</span>
          )}
        </div>
      </div>

      {/* 재고 테이블 */}
      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            {balances.length === 0 ? '재고 데이터가 없습니다. 입고 처리 후 재고가 등록됩니다.' : '검색 결과 없음'}
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-28">품목코드</th>
                <th className="border-b px-4 py-2.5">품목명</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">단위</th>
                <th className="border-b px-4 py-2.5 w-28">창고</th>
                <th className="border-b px-4 py-2.5 w-28 text-right">현재고</th>
                <th className="border-b px-4 py-2.5 w-24 text-right">안전재고</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">이동평균단가</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">재고금액</th>
                <th className="border-b px-4 py-2.5 w-36">최종이동</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((b) => (
                <tr
                  key={b.id}
                  className={`border-b hover:bg-gray-50 ${b.belowSafety ? 'bg-red-50' : ''}`}
                >
                  <td className="px-4 py-2.5 font-mono text-xs text-gray-500">{b.itemCode}</td>
                  <td className="px-4 py-2.5 font-medium text-gray-800">
                    {b.itemName}
                    {b.belowSafety && (
                      <span className="ml-2 text-xs text-red-500 font-normal">⚠️ 안전재고 미달</span>
                    )}
                  </td>
                  <td className="px-4 py-2.5 text-center text-gray-500">{b.unit}</td>
                  <td className="px-4 py-2.5 text-gray-600">{b.warehouseName}</td>
                  <td className={`px-4 py-2.5 text-right font-bold ${b.belowSafety ? 'text-red-600' : 'text-gray-800'}`}>
                    {b.qty.toLocaleString()}
                  </td>
                  <td className="px-4 py-2.5 text-right text-gray-400">
                    {b.safetyStock > 0 ? b.safetyStock.toLocaleString() : '-'}
                  </td>
                  <td className="px-4 py-2.5 text-right text-gray-600">
                    {b.avgCost.toLocaleString()}
                  </td>
                  <td className="px-4 py-2.5 text-right font-medium text-blue-700">
                    {b.stockValue.toLocaleString()}
                  </td>
                  <td className="px-4 py-2.5 text-gray-400 text-xs">
                    {b.lastMovedAt ? b.lastMovedAt.slice(0, 16).replace('T', ' ') : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="bg-slate-100 font-bold text-gray-700">
                <td colSpan={7} className="px-4 py-2.5 text-right">재고금액 합계</td>
                <td className="px-4 py-2.5 text-right text-blue-700">
                  {filtered.reduce((s, b) => s + b.stockValue, 0).toLocaleString()}
                </td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        )}
      </div>
    </div>
  )
}
