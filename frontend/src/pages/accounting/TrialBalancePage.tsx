import { useState } from 'react'
import client from '../../api/client'
import { TrialBalanceRow } from '../../types'

const TYPE_LABEL: Record<string, string> = {
  ASSET: '자산', LIABILITY: '부채', EQUITY: '자본', REVENUE: '수익', EXPENSE: '비용', UNKNOWN: '기타',
}

const TYPE_COLOR: Record<string, string> = {
  ASSET: 'bg-blue-100 text-blue-700',
  LIABILITY: 'bg-orange-100 text-orange-700',
  EQUITY: 'bg-purple-100 text-purple-700',
  REVENUE: 'bg-green-100 text-green-700',
  EXPENSE: 'bg-red-100 text-red-700',
}

export default function TrialBalancePage() {
  const [fromDate, setFromDate] = useState(() => new Date().getFullYear() + '-01-01')
  const [toDate, setToDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [rows, setRows] = useState<TrialBalanceRow[]>([])
  const [loading, setLoading] = useState(false)
  const [queried, setQueried] = useState(false)

  const fetchTrialBalance = async () => {
    setLoading(true)
    try {
      const res = await client.get('/accounting/trial-balance', {
        params: { fromDate, toDate },
      })
      setRows(res.data.data)
      setQueried(true)
    } finally {
      setLoading(false)
    }
  }

  const totalDebit = rows.reduce((s, r) => s + r.totalDebit, 0)
  const totalCredit = rows.reduce((s, r) => s + r.totalCredit, 0)
  const totalBalance = rows.reduce((s, r) => s + r.balance, 0)

  const fmt = (n: number) => n.toLocaleString()

  return (
    <div className="space-y-4">
      {/* 필터 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs text-gray-500 mb-1">시작일</label>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">종료일</label>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          onClick={fetchTrialBalance}
          className="px-5 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
        >
          조회
        </button>
      </div>

      {/* 시산표 */}
      {loading ? (
        <div className="text-center py-10 text-gray-400">로딩 중...</div>
      ) : queried ? (
        <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
          <div className="px-5 py-3 border-b bg-gray-50 flex justify-between items-center">
            <span className="font-semibold text-gray-700">합계잔액시산표</span>
            <span className="text-sm text-gray-500">{fromDate} ~ {toDate}</span>
          </div>

          {rows.length === 0 ? (
            <div className="px-4 py-10 text-center text-gray-400">해당 기간 데이터 없음</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-50 text-gray-600">
                  <th className="border-b px-4 py-2 text-left w-24">계정코드</th>
                  <th className="border-b px-4 py-2 text-left">계정과목</th>
                  <th className="border-b px-4 py-2 text-center w-20">유형</th>
                  <th className="border-b px-4 py-2 text-right w-36">차변 합계</th>
                  <th className="border-b px-4 py-2 text-right w-36">대변 합계</th>
                  <th className="border-b px-4 py-2 text-right w-36">잔액</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.accountCode} className="hover:bg-gray-50 border-b">
                    <td className="px-4 py-2 font-mono text-gray-500 text-xs">{row.accountCode}</td>
                    <td className="px-4 py-2 font-medium text-gray-800">{row.accountName}</td>
                    <td className="px-4 py-2 text-center">
                      <span className={`px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLOR[row.accountType] ?? 'bg-gray-100 text-gray-600'}`}>
                        {TYPE_LABEL[row.accountType] ?? row.accountType}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-right text-blue-700 font-medium">{fmt(row.totalDebit)}</td>
                    <td className="px-4 py-2 text-right text-orange-600 font-medium">{fmt(row.totalCredit)}</td>
                    <td className={`px-4 py-2 text-right font-semibold ${row.balance < 0 ? 'text-red-600' : 'text-gray-800'}`}>
                      {fmt(row.balance)}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="bg-slate-100 font-bold text-gray-700 border-t-2 border-slate-300">
                  <td colSpan={3} className="px-4 py-2.5 text-right">합계</td>
                  <td className="px-4 py-2.5 text-right text-blue-700">{fmt(totalDebit)}</td>
                  <td className="px-4 py-2.5 text-right text-orange-600">{fmt(totalCredit)}</td>
                  <td className={`px-4 py-2.5 text-right ${totalBalance < 0 ? 'text-red-600' : ''}`}>
                    {fmt(totalBalance)}
                  </td>
                </tr>
              </tfoot>
            </table>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-xl border p-10 text-center text-gray-400 shadow-sm">
          기간을 선택한 후 조회 버튼을 클릭하세요
        </div>
      )}
    </div>
  )
}
