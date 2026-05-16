import { useEffect, useState } from 'react'
import client from '../../api/client'
import { useAuthStore } from '../../store/authStore'

interface ApprovalLine {
  id: number
  sequence: number
  approverId: number
  approverName: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  comment: string
  processedAt: string
}

interface ApprovalRequest {
  id: number
  requestNo: string
  title: string
  content: string
  refType: string
  requesterName: string
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  lines: ApprovalLine[]
  createdAt: string
}

const STATUS_LABEL: Record<string, string> = {
  DRAFT: '기안', PENDING: '결재중', APPROVED: '승인', REJECTED: '반려', CANCELLED: '취소',
}
const STATUS_COLOR: Record<string, string> = {
  DRAFT: 'bg-gray-100 text-gray-600',
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  CANCELLED: 'bg-gray-100 text-gray-400',
}

export default function ApprovalPage() {
  const { user } = useAuthStore()
  const [list, setList] = useState<ApprovalRequest[]>([])
  const [selected, setSelected] = useState<ApprovalRequest | null>(null)
  const [filterStatus, setFilterStatus] = useState('')
  const [loading, setLoading] = useState(false)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ title: '', content: '' })
  const [comment, setComment] = useState('')

  const fetchList = async () => {
    setLoading(true)
    try {
      const params: Record<string, string> = { size: '50' }
      if (filterStatus) params.status = filterStatus
      const res = await client.get('/approvals', { params })
      setList(res.data.data.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchList() }, [filterStatus])

  const handleCreate = async () => {
    if (!form.title) return
    await client.post('/approvals', {
      title: form.title,
      content: form.content,
      approverIds: [user?.userId ?? 1],
    })
    setForm({ title: '', content: '' })
    setShowCreate(false)
    fetchList()
  }

  const handleApprove = async () => {
    if (!selected) return
    await client.post(`/approvals/${selected.id}/approve`, { comment })
    setComment('')
    setSelected(null)
    fetchList()
  }

  const handleReject = async () => {
    if (!selected) return
    await client.post(`/approvals/${selected.id}/reject`, { comment })
    setComment('')
    setSelected(null)
    fetchList()
  }

  const myPendingLine = selected?.lines.find(
    (l) => l.approverId === user?.userId && l.status === 'PENDING'
  )

  return (
    <div className="space-y-4">
      {/* 필터 + 버튼 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex flex-wrap gap-3 items-end justify-between">
        <div className="flex gap-2 flex-wrap">
          {['', 'PENDING', 'APPROVED', 'REJECTED'].map((s) => (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={`px-3 py-1 text-sm rounded-full border transition-colors ${
                filterStatus === s
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-600 hover:bg-gray-50'
              }`}
            >
              {s === '' ? '전체' : STATUS_LABEL[s]}
            </button>
          ))}
        </div>
        <button
          onClick={() => setShowCreate(!showCreate)}
          className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
        >
          + 결재 상신
        </button>
      </div>

      {/* 결재 상신 폼 */}
      {showCreate && (
        <div className="bg-blue-50 rounded-xl border border-blue-200 p-4 shadow-sm space-y-3">
          <h3 className="text-sm font-semibold text-blue-800">새 결재 상신</h3>
          <input
            type="text"
            placeholder="제목"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <textarea
            placeholder="내용"
            rows={3}
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="text-xs text-gray-500">* 결재자: 본인 계정 (데모용)</p>
          <div className="flex gap-2">
            <button onClick={handleCreate} className="px-4 py-1.5 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700">상신</button>
            <button onClick={() => setShowCreate(false)} className="px-4 py-1.5 bg-gray-200 text-gray-700 text-sm rounded-lg hover:bg-gray-300">취소</button>
          </div>
        </div>
      )}

      <div className="flex gap-4">
        {/* 목록 */}
        <div className="flex-1 bg-white rounded-xl border shadow-sm overflow-hidden">
          {loading ? (
            <div className="text-center py-10 text-gray-400">로딩 중...</div>
          ) : list.length === 0 ? (
            <div className="text-center py-10 text-gray-400">결재 내역 없음</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-50 text-gray-600">
                  <th className="border-b px-4 py-2 text-left w-32">결재번호</th>
                  <th className="border-b px-4 py-2 text-left">제목</th>
                  <th className="border-b px-4 py-2 text-left w-24">기안자</th>
                  <th className="border-b px-4 py-2 text-center w-20">상태</th>
                  <th className="border-b px-4 py-2 text-left w-32">등록일</th>
                </tr>
              </thead>
              <tbody>
                {list.map((r) => (
                  <tr
                    key={r.id}
                    onClick={() => setSelected(r)}
                    className={`cursor-pointer border-b hover:bg-blue-50 ${selected?.id === r.id ? 'bg-blue-50' : ''}`}
                  >
                    <td className="px-4 py-2 font-mono text-xs text-gray-500">{r.requestNo}</td>
                    <td className="px-4 py-2 font-medium text-gray-800">{r.title}</td>
                    <td className="px-4 py-2 text-gray-500">{r.requesterName}</td>
                    <td className="px-4 py-2 text-center">
                      <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLOR[r.status]}`}>
                        {STATUS_LABEL[r.status]}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-gray-400 text-xs">{r.createdAt?.slice(0, 10)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* 상세 */}
        {selected && (
          <div className="w-80 bg-white rounded-xl border shadow-sm p-4 space-y-4">
            <div>
              <div className="flex justify-between items-start">
                <h3 className="font-semibold text-gray-800">{selected.title}</h3>
                <button onClick={() => setSelected(null)} className="text-gray-400 hover:text-gray-600">✕</button>
              </div>
              <p className="text-xs text-gray-400 mt-1">{selected.requestNo}</p>
              <span className={`inline-block mt-1 px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLOR[selected.status]}`}>
                {STATUS_LABEL[selected.status]}
              </span>
            </div>

            {selected.content && (
              <div className="text-sm text-gray-600 bg-gray-50 rounded p-3 whitespace-pre-wrap">
                {selected.content}
              </div>
            )}

            {/* 결재선 */}
            <div>
              <p className="text-xs font-semibold text-gray-500 mb-2">결재선</p>
              {selected.lines.map((l) => (
                <div key={l.id} className="flex items-center gap-2 mb-1.5">
                  <span className="text-xs text-gray-600 w-16">{l.approverName}</span>
                  <span className={`px-1.5 py-0.5 rounded text-xs ${
                    l.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                    l.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                    'bg-yellow-100 text-yellow-700'
                  }`}>
                    {l.status === 'APPROVED' ? '승인' : l.status === 'REJECTED' ? '반려' : '대기'}
                  </span>
                  {l.comment && <span className="text-xs text-gray-400 truncate">{l.comment}</span>}
                </div>
              ))}
            </div>

            {/* 결재 처리 */}
            {myPendingLine && (
              <div className="space-y-2 pt-2 border-t">
                <p className="text-xs font-semibold text-blue-700">결재 처리</p>
                <textarea
                  placeholder="의견 (선택)"
                  rows={2}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  className="w-full border rounded px-2 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
                <div className="flex gap-2">
                  <button
                    onClick={handleApprove}
                    className="flex-1 py-1.5 bg-green-600 text-white text-xs rounded hover:bg-green-700"
                  >승인</button>
                  <button
                    onClick={handleReject}
                    className="flex-1 py-1.5 bg-red-500 text-white text-xs rounded hover:bg-red-600"
                  >반려</button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
