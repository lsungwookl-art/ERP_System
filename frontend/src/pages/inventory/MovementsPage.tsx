import { useEffect, useState } from 'react'
import client from '../../api/client'
import { StockMovement } from '../../types'

const TYPE_LABEL: Record<string, string> = { IN: '입고', OUT: '출고', ADJUST: '조정', TRANSFER: '이동' }
const TYPE_COLOR: Record<string, string> = {
  IN: 'bg-green-100 text-green-700',
  OUT: 'bg-red-100 text-red-700',
  ADJUST: 'bg-yellow-100 text-yellow-700',
  TRANSFER: 'bg-blue-100 text-blue-700',
}

export default function MovementsPage() {
  const [movements, setMovements] = useState<StockMovement[]>([])
  const [loading, setLoading] = useState(false)

  const fetchMovements = async () => {
    setLoading(true)
    try {
      const res = await client.get('/inventory/movements', { params: { size: 200 } })
      setMovements(res.data.data.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchMovements() }, [])

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <span className="text-sm text-gray-500">전체 <strong>{movements.length}</strong>건</span>
        <button onClick={fetchMovements} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">새로고침</button>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : movements.length === 0 ? (
          <div className="text-center py-16 text-gray-400">재고 이동 내역이 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-28">이동일자</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">유형</th>
                <th className="border-b px-4 py-2.5 w-24">품목ID</th>
                <th className="border-b px-4 py-2.5 w-24">창고ID</th>
                <th className="border-b px-4 py-2.5 w-24 text-right">수량</th>
                <th className="border-b px-4 py-2.5 w-28 text-right">단가</th>
                <th className="border-b px-4 py-2.5 w-24 text-right">이전재고</th>
                <th className="border-b px-4 py-2.5 w-24 text-right">이후재고</th>
                <th className="border-b px-4 py-2.5 w-24">참조유형</th>
                <th className="border-b px-4 py-2.5 w-28">처리자</th>
              </tr>
            </thead>
            <tbody>
              {movements.map((m) => (
                <tr key={m.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 text-gray-600">{m.movementDate}</td>
                  <td className="px-4 py-2.5 text-center">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLOR[m.movementType] ?? 'bg-gray-100 text-gray-600'}`}>
                      {TYPE_LABEL[m.movementType] ?? m.movementType}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-gray-500">{m.itemId}</td>
                  <td className="px-4 py-2.5 text-gray-500">{m.warehouseId}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{m.qty?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{m.unitCost?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-400">{m.beforeQty?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-400">{m.afterQty?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-gray-500 text-xs">{m.refType}</td>
                  <td className="px-4 py-2.5 text-gray-500 text-xs">{m.createdBy}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
