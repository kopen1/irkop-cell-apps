# IRKOP CELL Native Android — Parity Matrix

Source of truth:

- `kopen1/irkop-cell`
- Backend financial rules
- API contract
- Existing Android API layer

## Authentication

- [ ] Login username/password
- [ ] JWT session persistence
- [ ] `/api/auth/me`
- [ ] Logout
- [ ] Granular permission
- [ ] Karyawan hard-block `gaji_karyawan`
- [ ] Karyawan hard-block `pengaturan`

## Dashboard

- [ ] Daily omzet
- [ ] Transaction count
- [ ] Active kasbon
- [ ] Kasir status
- [ ] Balance per account
- [ ] Today's latest transactions
- [ ] Loading state
- [ ] Error state
- [ ] Empty state

## Transaksi

- [ ] Multi-item cart
- [ ] Quantity
- [ ] Selling price
- [ ] Tunai
- [ ] Transfer
- [ ] Bon
- [ ] Cash Tunai
- [ ] Transfer receiver account
- [ ] Transfer pending confirmation
- [ ] Bon customer
- [ ] Single-date filter
- [ ] Date-range filter
- [ ] Search
- [ ] Payment filter
- [ ] Confirmation filter
- [ ] Detail
- [ ] Edit
- [ ] Delete
- [ ] Atomic reversal
- [ ] Receipt
- [ ] Manual entry
- [ ] Backdate <= 30 days
- [ ] Idempotency key
- [ ] Closed kasir -> 409

## Kasir

- [ ] Opening
- [ ] Per-account opening balance
- [ ] One session/day
- [ ] Closing
- [ ] System vs real reconciliation
- [ ] Old-session reminder
- [ ] Closing creates no mutation

## Produk

- [ ] List
- [ ] Search
- [ ] Filter
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Category
- [ ] Selling price
- [ ] Cost price
- [ ] Unit
- [ ] Stock
- [ ] Minimum stock

## Pelanggan

- [ ] List
- [ ] Search
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Detail
- [ ] Purchase history
- [ ] Merge
- [ ] Loyal customer ranking

## Kasbon

- [ ] List
- [ ] Detail
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Belum lunas
- [ ] Lunas
- [ ] Settlement

## Pengeluaran

- [ ] List
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Description
- [ ] Amount
- [ ] Payment method
- [ ] Source account
- [ ] Atomic reversal
- [ ] Idempotency

## Service HP

- [ ] List
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Customer
- [ ] Device
- [ ] Complaint
- [ ] Cost
- [ ] Entry date
- [ ] Completion date
- [ ] Masuk
- [ ] Proses
- [ ] Selesai
- [ ] Diambil

## Gaji

- [ ] Admin only
- [ ] Employee list
- [ ] Rate
- [ ] Daily salary
- [ ] Automatic opening entry
- [ ] Karyawan blocked

## Akun Uang

- [ ] List
- [ ] Create
- [ ] Edit
- [ ] Delete
- [ ] Balance
- [ ] Account source validation

## Laporan

- [ ] Monthly
- [ ] Yearly
- [ ] Omzet
- [ ] Laba
- [ ] Category recap
- [ ] Kasbon
- [ ] Expenses
- [ ] Net
- [ ] Previous month comparison
- [ ] 12-month breakdown
- [ ] Best-selling category
- [ ] CSV export
- [ ] Print
- [ ] Manual transaction

## Pengaturan

- [ ] General settings
- [ ] Website name
- [ ] Theme
- [ ] Classic
- [ ] Paper
- [ ] Dark
- [ ] User management
- [ ] Permission management
- [ ] NotifHook
- [ ] API key
- [ ] Notification source
- [ ] Account master
- [ ] Audit log

## NotifHook

- [ ] POST `/api/notifhook`
- [ ] X-API-Key
- [ ] Idempotency key
- [ ] Auto-confirm transfer
- [ ] No fake package matcher

## Audit

- [ ] data_before
- [ ] data_after
- [ ] action
- [ ] related table
- [ ] user
- [ ] Log viewer

## Financial integrity

- [ ] Transaction -> mutasi_saldo
- [ ] Expense -> mutasi_saldo
- [ ] Reports from backend
- [ ] No frontend financial calculation
- [ ] Atomic reversal
- [ ] Idempotency
- [ ] Closed kasir protection
