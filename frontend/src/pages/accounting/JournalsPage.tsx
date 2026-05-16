import { useEffect, useState } from 'react'
import client from '../../api/client'
import { JournalEntry } from '../../types'

export default function JournalsPage() {
  const [journals, setJournals] = useState<JournalEntry[]>([])
  const [selected, setSelected] = useState<JournalEntry | null>(null)
  const [loading, setLoading] = useState(false)

  const fetchJournals = async () => {
    setLoading(true)
    try {
      const res = await client.get('/accounting/journals', { params: { size: 100 } })
      setJournals(res.data.data.content)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchJournals() }, [])

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-4 shadow-sm flex justify-between items-center">
        <span className="text-sm text-gray-500">전체 <strong>{journals.length}</strong>건 — 행 클릭 시 분개 상세 표시</span>
        <button onClick={fetchJournals} className="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">새로고침</button>
      </div>

      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : journals.length === 0 ? (
          <div className="text-center py-16 text-gray-400">전표가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600 text-left">
                <th className="border-b px-4 py-2.5 w-40">전표번호</th>
                <th className="border-b px-4 py-2.5 w-28">전표일자</th>
                <th className="border-b px-4 py-2.5 w-28">유형</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">자동분개</th>
                <th className="border-b px-4 py-2.5 w-20 text-center">상태</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">차변합계</th>
                <th className="border-b px-4 py-2.5 w-32 text-right">대변합계</th>
                <th className="border-b px-4 py-2.5">적요</th>
              </tr>
            </thead>
            <tbody>
              {journals.map((j) => (
                <tr
                  key={j.id}
                  onClick={() => setSelected(selected?.id === j.id ? null : j)}
                  className={`border-b cursor-pointer hover:bg-blue-50 ${selected?.id === j.id ? 'bg-blue-50' : ''}`}
                >
                  <td className="px-4 py-2.5 font-mono text-xs text-gray-600">{j.entryNo}</td>
                  <td className="px-4 py-2.5 text-gray-600">{j.entryDate}</td>
                  <td className="px-4 py-2.5 text-gray-500">{j.entryType}</td>
                  <td className="px-4 py-2.5 text-center">
                    <span className={`px-1.5 py-0.5 rounded text-xs ${j.isAuto ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-500'}`}>
                      {j.isAuto ? '자동' : '수동'}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-center">
                    <span className="px-1.5 py-0.5 rounded text-xs bg-green-100 text-green-700">{j.status}</span>
                  </td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{j.totalDebit?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-right text-gray-700">{j.totalCredit?.toLocaleString()}</td>
                  <td className="px-4 py-2.5 text-gray-500 text-xs">{j.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selected && (
        <div className="bg-white rounded-xl border p-4 shadow-sm">
          <h3 className="font-semibold text-gray-700 mb-3">
            분개 상세 — {selected.entryNo}
            <span className="ml-2 text-xs text-blue-500">{selected.isAuto ? '(자동분개)' : '(수동)'}</span>
          </h3>
          <table className="w-full text-sm border-collapse">
            <thead>
              <tr className="bg-gray-50">
                <th className="border px-3 py-1.5 text-left w-16">구분</th>
                <th className="border px-3 py-1.5 text-left w-24">계정코드</th>
                <th className="border px-3 py-1.5 text-left">계정과목</th>
                <th className="border px-3 py-1.5 text-right w-32">차변</th>
                <th className="border px-3 py-1.5 text-right w-32">대변</th>
                <th className="border px-3 py-1.5 text-left">적요</th>
              </tr>
            </thead>
            <tbody>
              {(selected.lines ?? []).map((line) => (
                <tr key={line.id} className="hover:bg-blue-50">
                  <td className="border px-3 py-1.5">
                    <span className={`px-1.5 py-0.5 rounded text-xs ${
                      line.lineType === 'DEBIT' ? 'bg-blue-100 text-blue-700' : 'bg-orange-100 text-orange-700'
                    }`}>{line.lineType === 'DEBIT' ? '차변' : '대변'}</span>
                  </td>
                  <td className="border px-3 py-1.5 font-mono text-xs">{line.accountCode}</td>
                  <td className="border px-3 py-1.5">{line.accountName}</td>
                  <td className="border px-3 py-1.5 text-right text-blue-700">
                    {line.debitAmount > 0 ? line.debitAmount.toLocaleString() : ''}
                  </td>
                  <td className="border px-3 py-1.5 text-right text-orange-600">
                    {line.creditAmount > 0 ? line.creditAmount.toLocaleString() : ''}
                  </td>
                  <td className="border px-3 py-1.5 text-gray-500 text-xs">{line.description}</td>
                </tr>
              ))}
            </tbody>
            <tfoot className="font-semibold">
              <tr className="bg-gray-50">
                <td colSpan={3} className="border px-3 py-1.5 text-right text-gray-600">합계</td>
                <td className="border px-3 py-1.5 text-right text-blue-700">{selected.totalDebit?.toLocaleString()}</td>
                <td className="border px-3 py-1.5 text-right text-orange-600">{selected.totalCredit?.toLocaleString()}</td>
                <td className="border px-3 py-1.5"></td>
              </tr>
            </tfoot>
          </table>
        </div>
      )}
    </div>
  )
}
