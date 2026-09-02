# IRKOP CELL Native Android — Parity Matrix

Source of truth:
- `kopen1/irkop-cell`
- Backend financial rules
- API contract
- Existing Android API layer

## Status rules

- `Implemented`: feature is actually present in the native Android repo.
- `Verified`: feature has a dedicated relevant test and the latest GitHub Actions run passes that test.
- **DONE = Implemented + Verified.**
- A green build/lint alone does **not** mark a feature Verified.

## Authentication
- Implemented [x] — Verified [ ] Login username/password
- Implemented [x] — Verified [ ] JWT session persistence
- Implemented [x] — Verified [ ] `/api/auth/me`
- Implemented [x] — Verified [ ] Logout
- Implemented [x] — Verified [ ] Granular permission
- Implemented [x] — Verified [ ] Karyawan hard-block `gaji_karyawan`
- Implemented [x] — Verified [ ] Karyawan hard-block `pengaturan`

## Dashboard
- Implemented [x] — Verified [ ] Daily omzet
- Implemented [x] — Verified [ ] Transaction count
- Implemented [x] — Verified [ ] Active kasbon
- Implemented [x] — Verified [ ] Kasir status
- Implemented [x] — Verified [ ] Balance per account
- Implemented [x] — Verified [ ] Today's latest transactions
- Implemented [x] — Verified [ ] Loading state
- Implemented [x] — Verified [ ] Error state
- Implemented [x] — Verified [ ] Empty state

## Transaksi
- Implemented [x] — Verified [ ] Multi-item cart
- Implemented [x] — Verified [ ] Quantity
- Implemented [x] — Verified [ ] Selling price
- Implemented [x] — Verified [ ] Tunai
- Implemented [x] — Verified [ ] Transfer
- Implemented [x] — Verified [ ] Bon
- Implemented [x] — Verified [ ] Cash Tunai
- Implemented [x] — Verified [ ] Transfer receiver account
- Implemented [ ] — Verified [ ] Transfer pending confirmation
- Implemented [x] — Verified [ ] Bon customer
- Implemented [x] — Verified [ ] Single-date filter
- Implemented [x] — Verified [ ] Date-range filter
- Implemented [x] — Verified [ ] Search
- Implemented [x] — Verified [ ] Payment filter
- Implemented [x] — Verified [ ] Confirmation filter
- Implemented [x] — Verified [ ] Detail
- Implemented [ ] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Atomic reversal
- Implemented [x] — Verified [ ] Receipt
- Implemented [x] — Verified [ ] Manual entry
- Implemented [x] — Verified [ ] Backdate <= 30 days
- Implemented [ ] — Verified [ ] Idempotency key
- Implemented [ ] — Verified [ ] Closed kasir -> 409

## Kasir
- Implemented [x] — Verified [ ] Opening
- Implemented [ ] — Verified [ ] Per-account opening balance
- Implemented [ ] — Verified [ ] One session/day
- Implemented [x] — Verified [ ] Closing
- Implemented [ ] — Verified [ ] System vs real reconciliation
- Implemented [ ] — Verified [ ] Old-session reminder
- Implemented [ ] — Verified [ ] Closing creates no mutation

## Produk
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Search
- Implemented [ ] — Verified [ ] Filter
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Category
- Implemented [ ] — Verified [ ] Selling price
- Implemented [ ] — Verified [ ] Cost price
- Implemented [ ] — Verified [ ] Unit
- Implemented [ ] — Verified [ ] Stock
- Implemented [ ] — Verified [ ] Minimum stock

## Pelanggan
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Search
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Detail
- Implemented [ ] — Verified [ ] Purchase history
- Implemented [ ] — Verified [ ] Merge
- Implemented [ ] — Verified [ ] Loyal customer ranking

## Kasbon
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Detail
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Belum lunas
- Implemented [ ] — Verified [ ] Lunas
- Implemented [ ] — Verified [ ] Settlement

## Pengeluaran
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Description
- Implemented [ ] — Verified [ ] Amount
- Implemented [ ] — Verified [ ] Payment method
- Implemented [ ] — Verified [ ] Source account
- Implemented [ ] — Verified [ ] Atomic reversal
- Implemented [ ] — Verified [ ] Idempotency

## Service HP
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Customer
- Implemented [ ] — Verified [ ] Device
- Implemented [ ] — Verified [ ] Complaint
- Implemented [ ] — Verified [ ] Cost
- Implemented [ ] — Verified [ ] Entry date
- Implemented [ ] — Verified [ ] Completion date
- Implemented [ ] — Verified [ ] Masuk
- Implemented [ ] — Verified [ ] Proses
- Implemented [ ] — Verified [ ] Selesai
- Implemented [ ] — Verified [ ] Diambil

## Gaji
- Implemented [ ] — Verified [ ] Admin only
- Implemented [ ] — Verified [ ] Employee list
- Implemented [ ] — Verified [ ] Rate
- Implemented [ ] — Verified [ ] Daily salary
- Implemented [ ] — Verified [ ] Automatic opening entry
- Implemented [ ] — Verified [ ] Karyawan blocked

## Akun Uang
- Implemented [ ] — Verified [ ] List
- Implemented [ ] — Verified [ ] Create
- Implemented [ ] — Verified [ ] Edit
- Implemented [ ] — Verified [ ] Delete
- Implemented [ ] — Verified [ ] Balance
- Implemented [ ] — Verified [ ] Account source validation

## Laporan
- Implemented [ ] — Verified [ ] Monthly
- Implemented [ ] — Verified [ ] Yearly
- Implemented [ ] — Verified [ ] Omzet
- Implemented [ ] — Verified [ ] Laba
- Implemented [ ] — Verified [ ] Category recap
- Implemented [ ] — Verified [ ] Kasbon
- Implemented [ ] — Verified [ ] Expenses
- Implemented [ ] — Verified [ ] Net
- Implemented [ ] — Verified [ ] Previous month comparison
- Implemented [ ] — Verified [ ] 12-month breakdown
- Implemented [ ] — Verified [ ] Best-selling category
- Implemented [ ] — Verified [ ] CSV export
- Implemented [ ] — Verified [ ] Print
- Implemented [ ] — Verified [ ] Manual transaction

## Pengaturan
- Implemented [ ] — Verified [ ] General settings
- Implemented [ ] — Verified [ ] Website name
- Implemented [ ] — Verified [ ] Theme
- Implemented [ ] — Verified [ ] Classic
- Implemented [ ] — Verified [ ] Paper
- Implemented [ ] — Verified [ ] Dark
- Implemented [ ] — Verified [ ] User management
- Implemented [ ] — Verified [ ] Permission management
- Implemented [ ] — Verified [ ] NotifHook
- Implemented [ ] — Verified [ ] API key
- Implemented [ ] — Verified [ ] Notification source
- Implemented [ ] — Verified [ ] Account master
- Implemented [ ] — Verified [ ] Audit log

## NotifHook
- Implemented [ ] — Verified [ ] POST `/api/notifhook`
- Implemented [ ] — Verified [ ] X-API-Key
- Implemented [ ] — Verified [ ] Idempotency key
- Implemented [ ] — Verified [ ] Auto-confirm transfer
- Implemented [ ] — Verified [ ] No fake package matcher

## Audit
- Implemented [ ] — Verified [ ] data_before
- Implemented [ ] — Verified [ ] data_after
- Implemented [ ] — Verified [ ] action
- Implemented [ ] — Verified [ ] related table
- Implemented [ ] — Verified [ ] user
- Implemented [ ] — Verified [ ] Log viewer

## Financial integrity
- Implemented [ ] — Verified [ ] Transaction -> mutasi_saldo
- Implemented [ ] — Verified [ ] Expense -> mutasi_saldo
- Implemented [ ] — Verified [ ] Reports from backend
- Implemented [ ] — Verified [ ] No frontend financial calculation
- Implemented [ ] — Verified [ ] Atomic reversal
- Implemented [ ] — Verified [ ] Idempotency
- Implemented [ ] — Verified [ ] Closed kasir protection
