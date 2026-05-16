import { useEffect, useState } from 'react'
import client from '../../api/client'
import { Partner } from '../../types'

const TYPE_LABEL: Record<string, string> = { CUSTOMER: '고객사', SUPPLIER: '공급사', BOTH: '양방' }
const TYPE_COLOR: Record<string, string> = {
  CUSTOMER: 'bg-blue-100 text-blue-700',
  SUPPLIER: 'bg-orange-100 text-orange-700',
  BOTH: 'bg-purple-100 text-purple-700',
}

export default function PartnersPage() {
  const [partners, setPartners] = useState<Partner[]>([])
  const [keyword, setKeyword] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState<Partial<Partner>>({ partnerType: 'BOTH' })
  const [editing, setEditing] = useState<number | null>(null)

  const fetchPartners = async (kw = keyword) => {
    const res = await client.get('/partners', { params: { keyword: kw || undefined, size: 200 } })
    setPartners(res.data.data.content)
  }

  useEffect(() => { fetchPartners('') }, [])

  const handleSave = async () => {
    try {
      editing ? await client.put(`/partners/${editing}`, form) : await client.post('/partners', form)
      setShowModal(false); setForm({ partnerType: 'BOTH' }); setEditing(null); fetchPartners()
    } catch (err: any) { alert(err.response?.data?.message ?? '저장 실패') }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    await client.delete(`/partners/${id}`); fetchPartners()
  }

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <form onSubmit={(e) => { e.preventDefault(); fetchPartners() }} className="flex gap-2">
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="거래처명 검색"
            className="border px-3 py-1.5 text-sm rounded-lg w-60 focus:outline-none focus:ring-2 focus:ring-blue-400" />
          <button type="submit" className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">검색</button>
        </form>
        <button onClick={() => { setForm({ partnerType: 'BOTH' }); setEditing(null); setShowModal(true) }}
          className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">+ 신규등록</button>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {partners.length === 0 ? (
          <div className="text-center py-16 text-gray-400">등록된 거래처가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5">거래처명</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">유형</th>
                <th className="border-b px-4 py-2.5 w-36">사업자번호</th>
                <th className="border-b px-4 py-2.5 w-28">대표자</th>
                <th className="border-b px-4 py-2.5 w-32">전화번호</th>
                <th className="border-b px-4 py-2.5">주소</th>
                <th className="border-b px-4 py-2.5 w-24 text-center">관리</th>
              </tr>
            </thead>
            <tbody>
              {partners.map((p) => (
                <tr key={p.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 font-medium text-gray-800">{p.partnerName}</td>
                  <td className="px-4 py-2.5 text-center">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLOR[p.partnerType]}`}>
                      {TYPE_LABEL[p.partnerType]}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-gray-500 font-mono text-xs">{p.businessNo}</td>
                  <td className="px-4 py-2.5 text-gray-600">{p.representativeName}</td>
                  <td className="px-4 py-2.5 text-gray-600">{p.phone}</td>
                  <td className="px-4 py-2.5 text-gray-500 text-xs">{p.address}</td>
                  <td className="px-4 py-2.5 text-center">
                    <div className="flex gap-1 justify-center">
                      <button onClick={() => { setForm(p); setEditing(p.id); setShowModal(true) }}
                        className="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200">수정</button>
                      <button onClick={() => handleDelete(p.id)}
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
          <div className="bg-white rounded-xl p-6 w-96 shadow-2xl">
            <h2 className="text-lg font-semibold mb-4">{editing ? '거래처 수정' : '거래처 등록'}</h2>
            <div className="space-y-3">
              {([['partnerName', '거래처명 *'], ['businessNo', '사업자번호'], ['representativeName', '대표자'],
                ['address', '주소'], ['phone', '전화번호'], ['email', '이메일']] as [string, string][]).map(([field, label]) => (
                <div key={field}>
                  <label className="block text-xs text-gray-600 mb-1">{label}</label>
                  <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    value={(form as any)[field] ?? ''} onChange={(e) => setForm({ ...form, [field]: e.target.value })} />
                </div>
              ))}
              <div>
                <label className="block text-xs text-gray-600 mb-1">유형</label>
                <select className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={form.partnerType} onChange={(e) => setForm({ ...form, partnerType: e.target.value as any })}>
                  <option value="CUSTOMER">고객사</option>
                  <option value="SUPPLIER">공급사</option>
                  <option value="BOTH">양방</option>
                </select>
              </div>
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
