import { useEffect, useState } from 'react'
import client from '../../api/client'
import { Item } from '../../types'

const empty: Partial<Item> = {}

export default function ItemsPage() {
  const [items, setItems] = useState<Item[]>([])
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState<Partial<Item>>(empty)
  const [editing, setEditing] = useState<number | null>(null)

  const fetchItems = async (kw = keyword) => {
    setLoading(true)
    try {
      const res = await client.get('/items', { params: { keyword: kw || undefined, size: 200 } })
      setItems(res.data.data.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchItems('') }, [])

  const handleSearch = (e: React.FormEvent) => { e.preventDefault(); fetchItems() }

  const openCreate = () => { setForm(empty); setEditing(null); setShowModal(true) }
  const openEdit = (item: Item) => { setForm(item); setEditing(item.id); setShowModal(true) }

  const handleSave = async () => {
    try {
      if (editing) {
        await client.put(`/items/${editing}`, form)
      } else {
        await client.post('/items', form)
      }
      setShowModal(false)
      fetchItems()
    } catch (err: any) {
      alert(err.response?.data?.message ?? '저장 실패')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    await client.delete(`/items/${id}`)
    fetchItems()
  }

  return (
    <div className="space-y-4">
      {/* 검색/버튼 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="품목명 또는 코드 검색"
            className="border px-3 py-1.5 text-sm rounded-lg w-60 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <button type="submit" className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">검색</button>
        </form>
        <button onClick={openCreate} className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">
          + 신규등록
        </button>
      </div>

      {/* 테이블 */}
      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : items.length === 0 ? (
          <div className="text-center py-16 text-gray-400">등록된 품목이 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-32">품목코드</th>
                <th className="border-b px-4 py-2.5">품목명</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">단위</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">판매단가</th>
                <th className="border-b px-4 py-2.5 w-28 text-right">원가</th>
                <th className="border-b px-4 py-2.5 w-28 text-right">안전재고</th>
                <th className="border-b px-4 py-2.5 w-24 text-center">상태</th>
                <th className="border-b px-4 py-2.5 w-24 text-center">관리</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 font-mono text-xs text-gray-500">{item.itemCode}</td>
                  <td className="px-4 py-2.5 font-medium text-gray-800">{item.itemName}</td>
                  <td className="px-4 py-2.5 text-center text-gray-500">{item.unit}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{item.standardPrice?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{item.standardCost?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{item.safetyStock?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-center">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${item.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {item.active ? '활성' : '비활성'}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-center">
                    <div className="flex gap-1 justify-center">
                      <button onClick={() => openEdit(item)} className="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200">수정</button>
                      <button onClick={() => handleDelete(item.id)} className="px-2 py-0.5 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200">삭제</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* 등록/수정 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-96 shadow-2xl">
            <h2 className="text-lg font-semibold mb-4">{editing ? '품목 수정' : '품목 등록'}</h2>
            <div className="space-y-3">
              {!editing && (
                <div>
                  <label className="block text-xs text-gray-600 mb-1">품목코드 (미입력 시 자동생성)</label>
                  <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={form.itemCode ?? ''} onChange={(e) => setForm({ ...form, itemCode: e.target.value })} />
                </div>
              )}
              <div>
                <label className="block text-xs text-gray-600 mb-1">품목명 *</label>
                <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={form.itemName ?? ''} onChange={(e) => setForm({ ...form, itemName: e.target.value })} />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs text-gray-600 mb-1">단위</label>
                  <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={form.unit ?? ''} onChange={(e) => setForm({ ...form, unit: e.target.value })} />
                </div>
                <div>
                  <label className="block text-xs text-gray-600 mb-1">안전재고</label>
                  <input type="number" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={form.safetyStock ?? ''} onChange={(e) => setForm({ ...form, safetyStock: +e.target.value })} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs text-gray-600 mb-1">판매단가</label>
                  <input type="number" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={form.standardPrice ?? ''} onChange={(e) => setForm({ ...form, standardPrice: +e.target.value })} />
                </div>
                <div>
                  <label className="block text-xs text-gray-600 mb-1">원가</label>
                  <input type="number" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={form.standardCost ?? ''} onChange={(e) => setForm({ ...form, standardCost: +e.target.value })} />
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-5">
              <button onClick={() => setShowModal(false)} className="px-4 py-1.5 text-sm border rounded-lg hover:bg-gray-50">취소</button>
              <button onClick={handleSave} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">저장</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
