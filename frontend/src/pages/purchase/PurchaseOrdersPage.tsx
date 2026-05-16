import { useEffect, useState } from 'react'
import client from '../../api/client'
import { PurchaseOrder, Partner, Item } from '../../types'

const today = new Date().toISOString().slice(0, 10)

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '임시저장', CONFIRMED: '확정', PARTIALLY_RECEIVED: '부분입고', COMPLETED: '완료', CANCELLED: '취소',
}
const STATUS_COLOR: Record<string, string> = {
  DRAFT: 'bg-gray-100 text-gray-600',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PARTIALLY_RECEIVED: 'bg-yellow-100 text-yellow-700',
  COMPLETED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-600',
}

interface OrderLine { itemId: string; orderQty: number; unitPrice: number }

export default function PurchaseOrdersPage() {
  const [orders, setOrders] = useState<PurchaseOrder[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [saving, setSaving] = useState(false)
  const [partners, setPartners] = useState<Partner[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [partnerId, setPartnerId] = useState('')
  const [orderDate, setOrderDate] = useState(today)
  const [expectedDate, setExpectedDate] = useState('')
  const [remark, setRemark] = useState('')
  const [lines, setLines] = useState<OrderLine[]>([{ itemId: '', orderQty: 1, unitPrice: 0 }])

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await client.get('/purchase/orders', { params: { size: 100 } })
      setOrders(res.data.data.content)
    } finally { setLoading(false) }
  }

  useEffect(() => { fetchOrders() }, [])

  const openModal = async () => {
    setPartnerId(''); setOrderDate(today); setExpectedDate(''); setRemark('')
    setLines([{ itemId: '', orderQty: 1, unitPrice: 0 }])
    const [pRes, iRes] = await Promise.all([
      client.get('/partners', { params: { size: 200 } }),
      client.get('/items', { params: { size: 200 } }),
    ])
    setPartners(pRes.data.data.content)
    setItems(iRes.data.data.content)
    setShowModal(true)
  }

  const handleConfirm = async (id: number) => {
    try { await client.patch(`/purchase/orders/${id}/confirm`); fetchOrders() }
    catch (err: any) { alert(err.response?.data?.message ?? '확정 실패') }
  }

  const updateLine = (i: number, field: keyof OrderLine, value: any) => {
    setLines(lines.map((l, idx) => {
      if (idx !== i) return l
      const updated = { ...l, [field]: value }
      if (field === 'itemId') {
        const item = items.find(it => it.id === +value)
        if (item) updated.unitPrice = item.standardCost
      }
      return updated
    }))
  }

  const total = lines.reduce((s, l) => s + l.orderQty * l.unitPrice, 0)

  const handleCreate = async () => {
    if (!partnerId) return alert('거래처를 선택해주세요.')
    if (lines.some(l => !l.itemId)) return alert('모든 라인의 품목을 선택해주세요.')
    setSaving(true)
    try {
      await client.post('/purchase/orders', {
        partnerId: +partnerId, orderDate,
        expectedDate: expectedDate || null, remark,
        items: lines.map(l => ({ itemId: +l.itemId, orderQty: +l.orderQty, unitPrice: +l.unitPrice })),
      })
      setShowModal(false); fetchOrders()
    } catch (err: any) {
      alert(err.response?.data?.message ?? '저장 실패')
    } finally { setSaving(false) }
  }

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <span className="text-sm text-gray-500">전체 <strong>{orders.length}</strong>건</span>
        <div className="flex gap-2">
          <button onClick={fetchOrders} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">새로고침</button>
          <button onClick={openModal} className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">+ 발주등록</button>
        </div>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : orders.length === 0 ? (
          <div className="text-center py-16 text-gray-400">발주서가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-40">발주번호</th>
                <th className="border-b px-4 py-2.5 w-28">발주일자</th>
                <th className="border-b px-4 py-2.5 w-24">거래처ID</th>
                <th className="border-b px-4 py-2.5 w-28 text-center">상태</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">발주금액</th>
                <th className="border-b px-4 py-2.5">비고</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">관리</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 font-mono text-xs text-gray-600">{o.orderNo}</td>
                  <td className="px-4 py-2.5 text-gray-600">{o.orderDate}</td>
                  <td className="px-4 py-2.5 text-gray-500">{o.partnerId}</td>
                  <td className="px-4 py-2.5 text-center">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLOR[o.status] ?? 'bg-gray-100 text-gray-600'}`}>
                      {STATUS_LABEL[o.status] ?? o.status}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-right font-medium text-gray-700">{o.totalAmount?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-gray-500 text-xs">{o.remark}</td>
                  <td className="px-4 py-2.5 text-center">
                    {o.status === 'DRAFT' && (
                      <button onClick={() => handleConfirm(o.id)}
                        className="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200">확정</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
            <h2 className="text-lg font-semibold mb-4">발주서 등록</h2>

            <div className="grid grid-cols-2 gap-3 mb-4">
              <div>
                <label className="block text-xs text-gray-600 mb-1">거래처 *</label>
                <select className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={partnerId} onChange={(e) => setPartnerId(e.target.value)}>
                  <option value="">선택</option>
                  {partners.filter(p => p.partnerType === 'SUPPLIER' || p.partnerType === 'BOTH').map(p => (
                    <option key={p.id} value={p.id}>{p.partnerName}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">발주일자 *</label>
                <input type="date" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={orderDate} onChange={(e) => setOrderDate(e.target.value)} />
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">납기예정일</label>
                <input type="date" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={expectedDate} onChange={(e) => setExpectedDate(e.target.value)} />
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">비고</label>
                <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={remark} onChange={(e) => setRemark(e.target.value)} />
              </div>
            </div>

            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">발주 품목</span>
              <button onClick={() => setLines([...lines, { itemId: '', orderQty: 1, unitPrice: 0 }])}
                className="px-3 py-1 text-xs bg-gray-100 rounded hover:bg-gray-200">+ 라인 추가</button>
            </div>
            <table className="w-full text-sm border-collapse mb-4">
              <thead>
                <tr className="bg-slate-50">
                  <th className="border px-2 py-1.5 text-left">품목</th>
                  <th className="border px-2 py-1.5 text-right w-20">수량</th>
                  <th className="border px-2 py-1.5 text-right w-28">단가</th>
                  <th className="border px-2 py-1.5 text-right w-28">소계</th>
                  <th className="border px-2 py-1.5 w-8"></th>
                </tr>
              </thead>
              <tbody>
                {lines.map((line, i) => (
                  <tr key={i}>
                    <td className="border px-2 py-1">
                      <select className="w-full text-sm focus:outline-none"
                        value={line.itemId} onChange={(e) => updateLine(i, 'itemId', e.target.value)}>
                        <option value="">품목 선택</option>
                        {items.filter(it => it.active).map(it => (
                          <option key={it.id} value={it.id}>{it.itemName} ({it.itemCode})</option>
                        ))}
                      </select>
                    </td>
                    <td className="border px-2 py-1">
                      <input type="number" min="1" className="w-full text-right text-sm focus:outline-none"
                        value={line.orderQty} onChange={(e) => updateLine(i, 'orderQty', +e.target.value)} />
                    </td>
                    <td className="border px-2 py-1">
                      <input type="number" min="0" className="w-full text-right text-sm focus:outline-none"
                        value={line.unitPrice} onChange={(e) => updateLine(i, 'unitPrice', +e.target.value)} />
                    </td>
                    <td className="border px-2 py-1 text-right text-gray-600">
                      {(line.orderQty * line.unitPrice).toLocaleString()}
                    </td>
                    <td className="border px-2 py-1 text-center">
                      {lines.length > 1 && (
                        <button onClick={() => setLines(lines.filter((_, idx) => idx !== i))}
                          className="text-red-400 hover:text-red-600 font-bold">×</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="bg-slate-50 font-semibold">
                  <td colSpan={3} className="border px-2 py-1.5 text-right text-gray-600">합계</td>
                  <td className="border px-2 py-1.5 text-right text-blue-700">{total.toLocaleString()}</td>
                  <td className="border"></td>
                </tr>
              </tfoot>
            </table>

            <div className="flex justify-end gap-2">
              <button onClick={() => setShowModal(false)} className="px-4 py-1.5 text-sm border rounded-lg hover:bg-gray-50">취소</button>
              <button onClick={handleCreate} disabled={saving}
                className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
