import { useEffect, useState } from 'react'
import client from '../../api/client'
import { Item } from '../../types'

interface BomLine {
  id: number
  childItemId: number
  childItemCode: string
  childItemName: string
  qty: number
  unit: string
  remark: string
}

export default function BomPage() {
  const [items, setItems] = useState<Item[]>([])
  const [parentItemId, setParentItemId] = useState<number | ''>('')
  const [bomLines, setBomLines] = useState<BomLine[]>([])
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({ childItemId: '', qty: '', unit: '', remark: '' })
  const [showForm, setShowForm] = useState(false)

  useEffect(() => {
    client.get('/items', { params: { size: 200 } }).then((r) => {
      setItems(r.data.data.content)
    })
  }, [])

  const fetchBom = async (pid: number) => {
    setLoading(true)
    try {
      const res = await client.get('/bom', { params: { parentItemId: pid } })
      setBomLines(res.data.data)
    } finally {
      setLoading(false)
    }
  }

  const handleParentChange = (id: number | '') => {
    setParentItemId(id)
    setBomLines([])
    if (id) fetchBom(Number(id))
  }

  const handleAdd = async () => {
    if (!parentItemId || !form.childItemId || !form.qty) return
    await client.post('/bom', {
      parentItemId: Number(parentItemId),
      childItemId: Number(form.childItemId),
      qty: Number(form.qty),
      unit: form.unit,
      remark: form.remark,
    })
    setForm({ childItemId: '', qty: '', unit: '', remark: '' })
    setShowForm(false)
    fetchBom(Number(parentItemId))
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    await client.delete(`/bom/${id}`)
    fetchBom(Number(parentItemId))
  }

  const parentItem = items.find((i) => i.id === Number(parentItemId))
  const availableChildren = items.filter((i) => i.id !== Number(parentItemId))

  return (
    <div className="space-y-4">
      {/* 상위품목 선택 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs text-gray-500 mb-1">상위품목 (완제품)</label>
          <select
            value={parentItemId}
            onChange={(e) => handleParentChange(e.target.value ? Number(e.target.value) : '')}
            className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[240px]"
          >
            <option value="">품목 선택</option>
            {items.map((i) => (
              <option key={i.id} value={i.id}>{i.itemCode} - {i.itemName}</option>
            ))}
          </select>
        </div>
        {parentItemId && (
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
          >
            + 구성품목 추가
          </button>
        )}
      </div>

      {/* 추가 폼 */}
      {showForm && (
        <div className="bg-blue-50 rounded-xl border border-blue-200 p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-blue-800 mb-3">
            [{parentItem?.itemCode}] {parentItem?.itemName} — 구성품목 추가
          </h3>
          <div className="flex flex-wrap gap-3 items-end">
            <div>
              <label className="block text-xs text-gray-500 mb-1">하위품목</label>
              <select
                value={form.childItemId}
                onChange={(e) => setForm({ ...form, childItemId: e.target.value })}
                className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[200px]"
              >
                <option value="">선택</option>
                {availableChildren.map((i) => (
                  <option key={i.id} value={i.id}>{i.itemCode} - {i.itemName}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">수량</label>
              <input
                type="number" step="0.0001" min="0.0001"
                value={form.qty}
                onChange={(e) => setForm({ ...form, qty: e.target.value })}
                className="border rounded-lg px-3 py-1.5 text-sm w-24 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">단위</label>
              <input
                type="text"
                value={form.unit}
                onChange={(e) => setForm({ ...form, unit: e.target.value })}
                className="border rounded-lg px-3 py-1.5 text-sm w-20 focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="EA"
              />
            </div>
            <div className="flex-1">
              <label className="block text-xs text-gray-500 mb-1">비고</label>
              <input
                type="text"
                value={form.remark}
                onChange={(e) => setForm({ ...form, remark: e.target.value })}
                className="border rounded-lg px-3 py-1.5 text-sm w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button onClick={handleAdd} className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">저장</button>
            <button onClick={() => setShowForm(false)} className="px-4 py-1.5 bg-gray-200 text-gray-700 text-sm rounded-lg hover:bg-gray-300">취소</button>
          </div>
        </div>
      )}

      {/* BOM 테이블 */}
      {parentItemId && (
        <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
          <div className="px-5 py-3 border-b bg-gray-50">
            <span className="font-semibold text-gray-700">
              BOM — [{parentItem?.itemCode}] {parentItem?.itemName}
            </span>
          </div>
          {loading ? (
            <div className="text-center py-10 text-gray-400">로딩 중...</div>
          ) : bomLines.length === 0 ? (
            <div className="text-center py-10 text-gray-400">등록된 구성품목 없음</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-50 text-gray-600">
                  <th className="border-b px-4 py-2 text-left">품목코드</th>
                  <th className="border-b px-4 py-2 text-left">품목명</th>
                  <th className="border-b px-4 py-2 text-right w-24">수량</th>
                  <th className="border-b px-4 py-2 text-center w-20">단위</th>
                  <th className="border-b px-4 py-2 text-left">비고</th>
                  <th className="border-b px-4 py-2 text-center w-16">삭제</th>
                </tr>
              </thead>
              <tbody>
                {bomLines.map((line) => (
                  <tr key={line.id} className="hover:bg-gray-50 border-b">
                    <td className="px-4 py-2 font-mono text-xs text-gray-500">{line.childItemCode}</td>
                    <td className="px-4 py-2 font-medium">{line.childItemName}</td>
                    <td className="px-4 py-2 text-right font-semibold">{line.qty.toLocaleString()}</td>
                    <td className="px-4 py-2 text-center text-gray-500">{line.unit}</td>
                    <td className="px-4 py-2 text-gray-500 text-xs">{line.remark}</td>
                    <td className="px-4 py-2 text-center">
                      <button
                        onClick={() => handleDelete(line.id)}
                        className="text-red-500 hover:text-red-700 text-xs"
                      >삭제</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}
