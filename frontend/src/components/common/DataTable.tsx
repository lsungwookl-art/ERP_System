import { AgGridReact } from '@ag-grid-community/react'
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model'
import { ModuleRegistry, ColDef } from '@ag-grid-community/core'
import 'ag-grid-community/styles/ag-grid.css'
import 'ag-grid-community/styles/ag-theme-quartz.css'

ModuleRegistry.registerModules([ClientSideRowModelModule])

interface Props<T> {
  rowData: T[]
  columnDefs: ColDef<T>[]
  onRowClick?: (row: T) => void
  height?: number
}

export default function DataTable<T>({ rowData, columnDefs, onRowClick, height = 500 }: Props<T>) {
  return (
    <div className="ag-theme-quartz" style={{ height }}>
      <AgGridReact
        rowData={rowData}
        columnDefs={columnDefs}
        defaultColDef={{
          sortable: true,
          filter: true,
          resizable: true,
          flex: 1,
          minWidth: 80,
        }}
        onRowClicked={(e) => e.data && onRowClick?.(e.data)}
        rowSelection="single"
        pagination={false}
        suppressCellFocus={false}
        animateRows
      />
    </div>
  )
}
