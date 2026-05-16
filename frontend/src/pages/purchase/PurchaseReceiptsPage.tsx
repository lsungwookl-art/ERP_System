import { useEffect, useState } from 'react'
import client from '../../api/client'
import { PurchaseReceipt, PurchaseOrder, Warehouse, Item } from '../../types'

const today = new Date().toISOString().slice(0, 10)

interface ReceiptLine {
  orderItemId: number
  itemId: number
  itemName: string
  orderQty: number
  receivedQty: number
  qty: number
  unitPrice: number
}

export default function PurchaseReceiptsPage() {
  const [receipts, setReceipts] = useState<PurchaseReceipt[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [saving, setSaving] = useState(false)

  const [confirmedOrders, setConfirmedOrders] = useState<PurchaseOrder[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [itemMap, setItemMap] = useState<Record<number, Item>>({})

  const [orderId, setOrderId] = useState('')
  const [warehouseId, setWarehouseId] = useState('')
  const [receiptDate, setReceiptDate] = useState(today)
  const [remark, setRemark] = useState('')
  const [lines, setLines] = useState<ReceiptLine[]>([])
  const [loadingOrder, setLoadingOrder] = useState(false)

  const fetchReceipts = async () => {
    setLoading(true)
    try {
      const res = await client.get('/purchase/receipts', { params: { size: 100 } })
      setReceipts(res.data.data.content)
    } finally { setLoading(false) }
  }

  useEffect(() => { fetchReceipts() }, [])

  const openModal = async () => {
    setOrderId(''); setWarehouseId(''); setReceiptDate(today); setRemark(''); setLines([])
    const [oRes, wRes, iRes] = await Promise.all([
      client.get('/purchase/orders', { params: { size: 200 } }),
      client.get('/warehouses'),
      client.get('/items', { params: { size: 200 } }),
    ])
    const allOrders: PurchaseOrder[] = oRes.data.data.content
    setConfirmedOrders(allOrders.filter(o => o.status === 'CONFIRMED' || o.status === 'PARTIALLY_RECEIVED'))
    setWarehouses(wRes.data.data)
    const imap: Record<number, Item> = {}
    iRes.data.data.content.forEach((it: Item) => { imap[it.id] = it })
    setItemMap(imap)
    setShowModal(true)
  }

  const handleOrderChange = async (oid: string) => {
    setOrderId(oid)
    setLines([])
    if (!oid) return
    setLoadingOrder(true)
    try {
      const res = await client.get(`/purchase/orders/${oid}`)
      const order: PurchaseOrder = res.data.data
      setLines(order.items.map(item => ({
        orderItemId: item.id,
        itemId: item.itemId,
        itemName: itemMap[item.itemId]?.itemName ?? `품목 #${item.itemId}`,
        orderQty: item.orderQty,
        receivedQty: item.receivedQty,
        qty: Math.max(0, item.orderQty - item.receivedQty),
        unitPrice: item.unitPrice,
      })))
    } finally { setLoadingOrder(false) }
  }

  const updateQty = (i: number, qty: number) => {
    setLines(lines.map((l, idx) => idx === i ? { ...l, qty } : l))
  }

  const handleCreate = async () => {
    if (!orderId) return alert('발주서를 선택해주세요.')
    if (!warehouseId) return alert('창고를 선택해주세요.')
    const validLines = lines.filter(l => l.qty > 0)
    if (validLines.length === 0) return alert('입고수량이 0보다 큰 라인이 없습니다.')
    setSaving(true)
    try {
      await client.post('/purchase/receipts', {
        orderId: +orderId, warehouseId: +warehouseId, receiptDate, remark,
        items: validLines.map(l => ({ orderItemId: l.orderItemId, itemId: l.itemId, qty: +l.qty, unitPrice: +l.unitPrice })),
      })
      setShowModal(false); fetchReceipts()
    } catch (err: any) {
      alert(err.response?.data?.message ?? '저장 실패')
    } finally { setSaving(false) }
  }

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <p className="text-sm text-gray-500">입고 처리 시 재고가 증가하고 회계 전표(차변: 재고자산 / 대변: 매입채무)가 자동 생성됩니다.</p>
        <div className="flex gap-2 shrink-0 ml-4">
          <button onClick={fetchReceipts} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">새로고침</button>
          <button onClick={openModal} className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">+ 입고처리</button>
        </div>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : receipts.length === 0 ? (
          <div className="text-center py-16 text-gray-400">입고 내역이 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-40">입고번호</th>
                <th className="border-b px-4 py-2.5 w-28">입고일자</th>
                <th className="border-b px-4 py-2.5 w-24">발주서ID</th>
                <th className="border-b px-4 py-2.5 w-24">창고ID</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">입고금액</th>
                <th className="border-b px-4 py-2.5 w-24 text-center">전표ID</th>
              </tr>
            </thead>
            <tbody>
              {receipts.map((r) => (
                <tr key={r.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-2.5 font-mono text-xs text-gray-600">{r.receiptNo}</td>
                  <td className="px-4 py-2.5 text-gray-600">{r.receiptDate}</td>
                  <td className="px-4 py-2.5 text-gray-500">{r.purchaseOrderId}</td>
                  <td className="px-4 py-2.5 text-gray-500">{r.warehouseId}</td>
                  <td className="px-4 py-2.5 text-right font-medium text-gray-700">{r.totalAmount?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-center">
                    {r.journalEntryId ? <span className="text-blue-600 font-mono text-xs">#{r.journalEntryId}</span> : <span className="text-gray-300">-</span>}
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
            <h2 className="text-lg font-semibold mb-4">입고 처리</h2>

            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="col-span-2">
                <label className="block text-xs text-gray-600 mb-1">발주서 *</label>
                <select className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={orderId} onChange={(e) => handleOrderChange(e.target.value)}>
                  <option value="">선택</option>
                  {confirmedOrders.map(o => (
                    <option key={o.id} value={o.id}>{o.orderNo} ({o.orderDate})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">창고 *</label>
                <select className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={warehouseId} onChange={(e) => setWarehouseId(e.target.value)}>
                  <option value="">선택</option>
                  {warehouses.map(w => <option key={w.id} value={w.id}>{w.warehouseName}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">입고일자 *</label>
                <input type="date" className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={receiptDate} onChange={(e) => setReceiptDate(e.target.value)} />
              </div>
              <div className="col-span-2">
                <label className="block text-xs text-gray-600 mb-1">비고</label>
                <input className="w-full border rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                  value={remark} onChange={(e) => setRemark(e.target.value)} />
              </div>
            </div>

            {loadingOrder && <div className="text-center py-4 text-gray-400 text-sm">발주서 로딩 중...</div>}

            {lines.length > 0 && (
              <div className="mb-4">
                <span className="text-sm font-medium text-gray-700 block mb-2">입고 품목</span>
                <table className="w-full text-sm border-collapse">
                  <thead>
                    <tr className="bg-slate-50">
                      <th className="border px-2 py-1.5 text-left">품목명</th>
                      <th className="border px-2 py-1.5 text-right w-20">발주수량</th>
                      <th className="border px-2 py-1.5 text-right w-20">기입고</th>
                      <th className="border px-2 py-1.5 text-right w-24">이번입고수량</th>
                      <th className="border px-2 py-1.5 text-right w-28">단가</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((line, i) => {
                      const remaining = line.orderQty - line.receivedQty
                      return (
                        <tr key={i} className={remaining <= 0 ? 'opacity-40' : ''}>
                          <td className="border px-2 py-1 text-gray-700">{line.itemName}</td>
                          <td className="border px-2 py-1 text-right text-gray-500">{line.orderQty.toLocaleString()}</td>
                          <td className="border px-2 py-1 text-right text-gray-400">{line.receivedQty.toLocaleString()}</td>
                          <td className="border px-2 py-1">
                            <input type="number" min="0" max={remaining}
                              className="w-full text-right text-sm focus:outline-none"
                              disabled={remaining <= 0}
                              value={line.qty}
                              onChange={(e) => updateQty(i, +e.target.value)} />
                          </td>
                          <td className="border px-2 py-1 text-right text-gray-600">{line.unitPrice.toLocaleString()}</td>
                        </tr>
                      )
                    })}
                  </tbody>
                  <tfoot>
                    <tr className="bg-slate-50 font-semibold">
                      <td colSpan={4} className="border px-2 py-1.5 text-right text-gray-600">입고금액 합계</td>
                      <td className="border px-2 py-1.5 text-right text-blue-700">
                        {lines.filter(l => l.qty > 0).reduce((s, l) => s + l.qty * l.unitPrice, 0).toLocaleString()}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            )}

            <div className="flex justify-end gap-2">
              <button onClick={() => setShowModal(false)} className="px-4 py-1.5 text-sm border rounded-lg hover:bg-gray-50">취소</button>
              <button onClick={handleCreate} disabled={saving || lines.length === 0}
                className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {saving ? '처리 중...' : '입고 확정'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
