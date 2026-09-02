# Android API Parity

Required API areas:

## Auth
- POST /api/auth/login
- GET /api/auth/me
- POST /api/auth/logout

## Kasir
- GET /api/kasir/current
- GET /api/kasir/reminder
- POST /api/kasir/opening
- POST /api/kasir/closing

## Transaksi
- GET /api/transaksi
- GET /api/transaksi/:id
- POST /api/transaksi
- PUT /api/transaksi/:id
- DELETE /api/transaksi/:id

Required query support:
- q
- tanggal
- tanggal_mulai
- tanggal_selesai
- metode_bayar
- status_konfirmasi

## Produk
- GET /api/produk
- POST /api/produk
- PUT /api/produk/:id
- DELETE /api/produk/:id

## Kategori
- GET /api/kategori
- POST /api/kategori
- PUT /api/kategori/:id
- DELETE /api/kategori/:id

## Pelanggan
- GET /api/pelanggan
- POST /api/pelanggan
- GET /api/pelanggan/:id
- POST /api/pelanggan/merge

## Kasbon
- GET /api/kasbon
- POST /api/kasbon
- PUT /api/kasbon/:id

## Pengeluaran
- GET /api/pengeluaran
- POST /api/pengeluaran
- GET /api/pengeluaran/:id
- PUT /api/pengeluaran/:id
- DELETE /api/pengeluaran/:id

## Service HP
- GET /api/service-hp
- POST /api/service-hp
- PUT /api/service-hp/:id

## Gaji
- GET /api/gaji
- POST /api/gaji
- PUT /api/gaji/:id
- GET /api/gaji/rate

## Users
- GET /api/users
- POST /api/users
- PUT /api/users/:id
- PUT /api/users/:id/permissions

## Akun
- GET /api/akun
- POST /api/akun
- PUT /api/akun/:id

## Settings
- GET /api/settings
- PUT /api/settings
- POST /api/settings/generate
- GET /api/settings/notifhook-source

## Logs
- GET /api/logs

## Reports
- GET /api/laporan/bulan
- GET /api/laporan/tahun
- GET /api/laporan/export

## NotifHook
- POST /api/notifhook
