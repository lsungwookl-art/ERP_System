import { useEffect, useState } from 'react'
import client from '../../api/client'
import { Account, LedgerResponse } from '../../types'

export default function LedgerPage() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [accountCode, setAccountCode] = useState('')
  const [fromDate, setFromDate] = useState(() => new Date().getFullYear() + '-01-01')
  const [toDate, setToDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [ledger, setLedger] = useState<LedgerResponse | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    client.get('/accounting/accounts').then((r) => {
      const list: Account[] = r.data.data
      setAccounts(list)
      if (list.length > 0) setAccountCode(list[0].accountCode)
    })
  }, [])

  const fetchLedger = async () => {
    if (!accountCode) return
    setLoading(true)
    try {
      const res = await client.get('/accounting/ledger', {
        params: { accountCode, fromDate, toDate },
      })
      setLedger(res.data.data)
    } finally {
      setLoading(false)
    }
  }

  const fmt = (n: number) => n === 0 ? '' : n.toLocaleString()
  const fmtBalance = (n: number) => n.toLocaleString()

  const accountTypeLabel: Record<string, string> = {
    ASSET: '자산', LIABILITY: '부채', EQUITY: '자본', REVENUE: '수익', EXPENSE: '비용',
  }

  return (
    <div className="space-y-4">
      {/* 필터 */}
      <div className="bg-white rounded-xl border p-4 shadow-sm flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs text-gray-500 mb-1">계정과목</label>
          <select
            value={accountCode}
            onChange={(e) => setAccountCode(e.target.value)}
            className="border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[200px]"
          >
            {accounts.map((a) => (
              <option key={a.id} value={a.accountCode}>
                {a.accountCode} {a.accountName} ({accountTypeLabel[a.accountType] ?? a.accountType})
              </option>
            ))}
          </select>
        </div>
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
          onClick={fetchLedger}
          className="px-5 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700"
        >
          조회
        </button>
      </div>

      {/* 원장 테이블 */}
      {loading ? (
        <div className="text-center py-10 text-gray-400">로딩 중...</div>
      ) : ledger ? (
        <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
          <div className="px-5 py-3 border-b bg-gray-50 flex justify-between items-center">
            <span className="font-semibold text-gray-700">
              [{ledger.accountCode}] {ledger.accountName} 원장
            </span>
            <span className="text-sm text-gray-500">
              {fromDate} ~ {toDate}
            </span>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-50 text-gray-600">
                <th className="border-b px-4 py-2 text-left w-28">날짜</th>
                <th className="border-b px-4 py-2 text-left w-36">전표번호</th>
                <th className="border-b px-4 py-2 text-left">적요</th>
                <th className="border-b px-4 py-2 text-right w-32">차변</th>
                <th className="border-b px-4 py-2 text-right w-32">대변</th>
                <th className="border-b px-4 py-2 text-right w-36">잔액</th>
              </tr>
            </thead>
            <tbody>
              {/* 전기이월 */}
              <tr className="bg-blue-50 font-medium text-blue-800">
                <td className="border-b px-4 py-2">{fromDate}</td>
                <td className="border-b px-4 py-2">—</td>
                <td className="border-b px-4 py-2">전기이월</td>
                <td className="border-b px-4 py-2 text-right"></td>
                <td className="border-b px-4 py-2 text-right"></td>
                <td className="border-b px-4 py-2 text-right">
                  {fmtBalance(ledger.openingBalance)}
                </td>
              </tr>
              {ledger.lines.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                    해당 기간 거래 내역 없음
                  </td>
                </tr>
              ) : (
                ledger.lines.map((line, i) => (
                  <tr key={i} className="hover:bg-gray-50 border-b">
                    <td className="px-4 py-2 text-gray-600">{line.entryDate}</td>
                    <td className="px-4 py-2 text-blue-600 font-mono text-xs">{line.entryNo}</td>
                    <td className="px-4 py-2 text-gray-600">{line.description}</td>
                    <td className="px-4 py-2 text-right text-blue-700 font-medium">{fmt(line.debit)}</td>
                    <td className="px-4 py-2 text-right text-orange-600 font-medium">{fmt(line.credit)}</td>
                    <td className="px-4 py-2 text-right font-semibold">{fmtBalance(line.balance)}</td>
                  </tr>
                ))
              )}
            </tbody>
            <tfoot>
              <tr className="bg-slate-100 font-bold text-gray-700">
                <td colSpan={3} className="px-4 py-2 text-right">기말 잔액</td>
                <td className="px-4 py-2 text-right text-blue-700">
                  {ledger.lines.reduce((s, l) => s + l.debit, 0).toLocaleString()}
                </td>
                <td className="px-4 py-2 text-right text-orange-600">
                  {ledger.lines.reduce((s, l) => s + l.credit, 0).toLocaleString()}
                </td>
                <td className="px-4 py-2 text-right">{fmtBalance(ledger.closingBalance)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      ) : (
        <div className="bg-white rounded-xl border p-10 text-center text-gray-400 shadow-sm">
          계정과목과 기간을 선택한 후 조회 버튼을 클릭하세요
        </div>
      )}
    </div>
  )
}
