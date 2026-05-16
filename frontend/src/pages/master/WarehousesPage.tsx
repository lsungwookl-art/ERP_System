import { useEffect, useState } from 'react'
import client from '../../api/client'
import { Warehouse } from '../../types'

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState<Partial<Warehouse>>({})
  const [editing, setEditing] = useState<number | null>(null)

  const fetchWarehouses = async () => {
    const res = await client.get('/warehouses')
    setWarehouses(res.data.data)
  }

  useEffect(() => { fetchWarehouses() }, [])

  const handleSave = async () => {
    try {
      editing ? await client.put(`/warehouses/${editing}`, form) : await client.post('/warehouses', form)
      setShowModal(false); setForm({}); setEditing(null); fetchWarehouses()
    } catch (err: any) { alert(err.response?.data?.message ?? '저장 실패') }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    await client.delete(`/warehouses/${id}`); fetchWarehouses()
  }

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-end">
        <button onClick={() => { setForm({}); setEditing(null); setShowModal(true) }}
          className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">+ 신규등록</button>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {warehouses.length === 0 ? (
          <div className="text-center py-16 text-gray-400">등록된 창고가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5">창고명</th>
                <th className="border-b px-4 py-2.5">주소</th>
                <th className="border-b px-4 py-2.5">설명</th>
                <th className="border-b px-4 py-2.5 w-24 text-center">관리</th>
              </tr>
            </thead>
            <tbody>
              {warehouses.map((w) => (
                <tr key={w.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 font-medium text-gray-800">{w.warehouseName}</td>
                  <td className="px-4 py-2.5 text-gray-500">{w.address}</td>
                  <td className="px-4 py-2.5 text-gray-500">{w.description}</td>
                  <td className="px-4 py-2.5 text-center">
                    <div className="flex gap-1 justify-center">
                      <button onClick={() => { setForm(w); setEditing(w.id); setShowModal(true) }}
                        className="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200">수정</button>
                      <button onClick={() => handleDelete(w.id)}
                        className="px-2 py-0.5 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200">삭제</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-80 shadow-2xl">
            <h2 className="text-lg font-semibold mb-4">{editing ? '창고 수정' : '창고 등록'}</h2>
            <div className="space-y-3">
              {([['warehouseName', '창고명 *'], ['address', '주소'], ['description', '설명']] as [string, string][]).map(([field, label]) => (
                <div key={field}>
                  <label className="block text-xs text-gray-600 mb-1">{label}</label>
                  <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={(form as any)[field] ?? ''} onChange={(e) => setForm({ ...form, [field]: e.target.value })} />
                </div>
              ))}
            </div>
            <div className="flex justify-end gap-2 mt-4">
              <button onClick={() => setShowModal(false)} className="px-4 py-1.5 text-sm border rounded-lg hover:bg-gray-50">취소</button>
              <button onClick={handleSave} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">저장</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
