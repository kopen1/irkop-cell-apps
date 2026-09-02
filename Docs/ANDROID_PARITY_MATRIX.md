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
- Current migration phase: implementation only. Build/test/lint intentionally deferred.

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
- Implemented [x] — Verified [ ] Transfer pending confirmation
- Implemented [x] — Verified [ ] Bon customer
- Implemented [x] — Verified [ ] Single-date filter
- Implemented [x] — Verified [ ] Date-range filter
- Implemented [x] — Verified [ ] Search
- Implemented [x] — Verified [ ] Payment filter
- Implemented [x] — Verified [ ] Confirmation filter
- Implemented [x] — Verified [ ] Detail
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Atomic reversal
- Implemented [x] — Verified [ ] Receipt
- Implemented [x] — Verified [ ] Manual entry
- Implemented [x] — Verified [ ] Backdate <= 30 days
- Implemented [x] — Verified [ ] Idempotency key
- Implemented [x] — Verified [ ] Closed kasir -> 409

## Kasir
- Implemented [x] — Verified [ ] Opening
- Implemented [x] — Verified [ ] Per-account opening balance
- Implemented [x] — Verified [ ] One session/day
- Implemented [x] — Verified [ ] Closing
- Implemented [x] — Verified [ ] System vs real reconciliation
- Implemented [x] — Verified [ ] Old-session reminder
- Implemented [x] — Verified [ ] Closing creates no mutation

## Produk
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Search
- Implemented [x] — Verified [ ] Filter
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Category
- Implemented [x] — Verified [ ] Selling price
- Implemented [x] — Verified [ ] Cost price
- Implemented [x] — Verified [ ] Unit
- Implemented [x] — Verified [ ] Stock
- Implemented [x] — Verified [ ] Minimum stock

## Pelanggan
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Search
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Detail
- Implemented [x] — Verified [ ] Purchase history
- Implemented [x] — Verified [ ] Merge
- Implemented [x] — Verified [ ] Loyal customer ranking

## Kasbon
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Detail
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Belum lunas
- Implemented [x] — Verified [ ] Lunas
- Implemented [x] — Verified [ ] Settlement

## Pengeluaran
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Description
- Implemented [x] — Verified [ ] Amount
- Implemented [x] — Verified [ ] Payment method
- Implemented [x] — Verified [ ] Source account
- Implemented [x] — Verified [ ] Atomic reversal
- Implemented [x] — Verified [ ] Idempotency

## Service HP
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Customer
- Implemented [x] — Verified [ ] Device
- Implemented [x] — Verified [ ] Complaint
- Implemented [x] — Verified [ ] Cost
- Implemented [x] — Verified [ ] Entry date
- Implemented [x] — Verified [ ] Completion date
- Implemented [x] — Verified [ ] Masuk
- Implemented [x] — Verified [ ] Proses
- Implemented [x] — Verified [ ] Selesai
- Implemented [x] — Verified [ ] Diambil

## Gaji
- Implemented [x] — Verified [ ] Admin only
- Implemented [x] — Verified [ ] Employee list
- Implemented [x] — Verified [ ] Rate
- Implemented [x] — Verified [ ] Daily salary
- Implemented [x] — Verified [ ] Automatic opening entry
- Implemented [x] — Verified [ ] Karyawan blocked

## Akun Uang
- Implemented [x] — Verified [ ] List
- Implemented [x] — Verified [ ] Create
- Implemented [x] — Verified [ ] Edit
- Implemented [x] — Verified [ ] Delete
- Implemented [x] — Verified [ ] Balance
- Implemented [x] — Verified [ ] Account source validation

## Laporan
- Implemented [x] — Verified [ ] Monthly
- Implemented [x] — Verified [ ] Yearly
- Implemented [x] — Verified [ ] Omzet
- Implemented [x] — Verified [ ] Laba
- Implemented [x] — Verified [ ] Category recap
- Implemented [x] — Verified [ ] Kasbon
- Implemented [x] — Verified [ ] Expenses
- Implemented [x] — Verified [ ] Net
- Implemented [x] — Verified [ ] Previous month comparison
- Implemented [x] — Verified [ ] 12-month breakdown
- Implemented [x] — Verified [ ] Best-selling category
- Implemented [x] — Verified [ ] CSV export
- Implemented [x] — Verified [ ] Print
- Implemented [x] — Verified [ ] Manual transaction

## Pengaturan
- Implemented [x] — Verified [ ] General settings
- Implemented [x] — Verified [ ] Website name
- Implemented [x] — Verified [ ] Theme
- Implemented [x] — Verified [ ] Classic
- Implemented [x] — Verified [ ] Paper
- Implemented [x] — Verified [ ] Dark
- Implemented [x] — Verified [ ] User management
- Implemented [x] — Verified [ ] Permission management
- Implemented [x] — Verified [ ] NotifHook
- Implemented [x] — Verified [ ] API key
- Implemented [x] — Verified [ ] Notification source
- Implemented [x] — Verified [ ] Account master
- Implemented [x] — Verified [ ] Audit log

## NotifHook
- Implemented [x] — Verified [ ] POST `/api/notifhook`
- Implemented [x] — Verified [ ] X-API-Key
- Implemented [x] — Verified [ ] Idempotency key
- Implemented [x] — Verified [ ] Auto-confirm transfer
- Implemented [x] — Verified [ ] No fake package matcher

## Audit
- Implemented [x] — Verified [ ] data_before
- Implemented [x] — Verified [ ] data_after
- Implemented [x] — Verified [ ] action
- Implemented [x] — Verified [ ] related table
- Implemented [x] — Verified [ ] user
- Implemented [x] — Verified [ ] Log viewer

## Financial integrity
- Implemented [x] — Verified [ ] Transaction -> mutasi_saldo
- Implemented [x] — Verified [ ] Expense -> mutasi_saldo
- Implemented [x] — Verified [ ] Reports from backend
- Implemented [x] — Verified [ ] No frontend financial calculation
- Implemented [x] — Verified [ ] Atomic reversal
- Implemented [x] — Verified [ ] Idempotency
- Implemented [x] — Verified [ ] Closed kasir protection
