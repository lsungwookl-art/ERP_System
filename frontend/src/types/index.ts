export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  errorCode?: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Item {
  id: number
  itemCode: string
  itemName: string
  unit: string
  standardPrice: number
  standardCost: number
  safetyStock: number
  description: string
  active: boolean
  createdAt: string
}

export interface Partner {
  id: number
  partnerName: string
  businessNo: string
  partnerType: 'CUSTOMER' | 'SUPPLIER' | 'BOTH'
  representativeName: string
  address: string
  phone: string
  email: string
  active: boolean
}

export interface Warehouse {
  id: number
  warehouseName: string
  address: string
  description: string
  active: boolean
}

export interface StockBalance {
  id: number
  itemId: number
  warehouseId: number
  qty: number
  avgCost: number
  lastMovedAt: string
}

export interface StockMovement {
  id: number
  itemId: number
  warehouseId: number
  movementType: 'IN' | 'OUT' | 'ADJUST' | 'TRANSFER'
  qty: number
  unitCost: number
  beforeQty: number
  afterQty: number
  movementDate: string
  refType: string
  createdBy: string
}

export interface PurchaseOrder {
  id: number
  orderNo: string
  partnerId: number
  orderDate: string
  expectedDate: string
  status: string
  totalAmount: number
  remark: string
  items: PurchaseOrderItem[]
}

export interface PurchaseOrderItem {
  id: number
  itemId: number
  orderQty: number
  receivedQty: number
  unitPrice: number
  lineAmount: number
}

export interface PurchaseReceipt {
  id: number
  receiptNo: string
  purchaseOrderId: number
  warehouseId: number
  receiptDate: string
  totalAmount: number
  journalEntryId: number
}

export interface SalesOrder {
  id: number
  orderNo: string
  partnerId: number
  orderDate: string
  expectedDate: string
  status: string
  totalAmount: number
  remark: string
  items: SalesOrderItem[]
}

export interface SalesOrderItem {
  id: number
  itemId: number
  orderQty: number
  shippedQty: number
  unitPrice: number
}

export interface SalesShipment {
  id: number
  shipmentNo: string
  salesOrderId: number
  warehouseId: number
  shipmentDate: string
  totalAmount: number
  journalEntryId: number
}

export interface JournalEntry {
  id: number
  entryNo: string
  entryType: string
  entryDate: string
  isAuto: boolean
  status: string
  totalDebit: number
  totalCredit: number
  description: string
  lines: JournalEntryLine[]
}

export interface JournalEntryLine {
  id: number
  accountCode: string
  accountName: string
  lineType: 'DEBIT' | 'CREDIT'
  debitAmount: number
  creditAmount: number
  description: string
}

export interface Account {
  id: number
  accountCode: string
  accountName: string
  accountType: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE'
  active: boolean
}

export interface LedgerLine {
  entryDate: string
  entryNo: string
  description: string
  debit: number
  credit: number
  balance: number
}

export interface LedgerResponse {
  accountCode: string
  accountName: string
  openingBalance: number
  lines: LedgerLine[]
  closingBalance: number
}

export interface TrialBalanceRow {
  accountCode: string
  accountName: string
  accountType: string
  totalDebit: number
  totalCredit: number
  balance: number
}
