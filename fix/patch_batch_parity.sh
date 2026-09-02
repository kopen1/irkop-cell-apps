#!/usr/bin/env bash
set -e

python3 - <<'PY'
from pathlib import Path

p = Path('app/src/main/java/com/irkop/cell/MainActivity.kt')
s = p.read_text()
old = '''    val allDestinations = listOf(
        Triple(AuthPolicy.DASHBOARD, "Dashboard", Icons.Default.Home),
        Triple(AuthPolicy.TRANSAKSI, "Transaksi", Icons.Default.ReceiptLong),
        Triple(AuthPolicy.KASIR, "Kasir", Icons.Default.PointOfSale),
        Triple(AuthPolicy.LAPORAN, "Laporan", Icons.Default.Assessment)
    )'''
new = '''    val allDestinations = listOf(
        Triple(AuthPolicy.DASHBOARD, "Dashboard", Icons.Default.Home),
        Triple(AuthPolicy.TRANSAKSI, "Transaksi", Icons.Default.ReceiptLong),
        Triple(AuthPolicy.KASIR, "Kasir", Icons.Default.PointOfSale),
        Triple(AuthPolicy.LAPORAN, "Laporan", Icons.Default.Assessment),
        Triple("lainnya", "Lainnya", Icons.Default.MoreHoriz)
    )'''
if old not in s:
    raise SystemExit('MainScaffold destination block not found')
s = s.replace(old, new, 1)
old = '''            composable("laporan") {
                if (AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)) {
                    ReportScreen(repo)
                } else {
                    AccessDeniedScreen()
                }
            }
'''
new = old + '''
            composable("lainnya") {
                ParityExtrasScreen(user, repo)
            }
'''
if old not in s:
    raise SystemExit('MainScaffold laporan route not found')
s = s.replace(old, new, 1)
p.write_text(s)

m = Path('Docs/ANDROID_PARITY_MATRIX.md')
t = m.read_text()
changes = {
    '- Implemented [ ] — Verified [ ] Detail\n- Implemented [ ] — Verified [ ] Purchase history\n- Implemented [ ] — Verified [ ] Merge':
    '- Implemented [x] — Verified [ ] Detail\n- Implemented [x] — Verified [ ] Purchase history\n- Implemented [x] — Verified [ ] Merge',
    '- Implemented [x] — Verified [ ] List\n- Implemented [ ] — Verified [ ] Detail\n- Implemented [x] — Verified [ ] Create':
    '- Implemented [x] — Verified [ ] List\n- Implemented [x] — Verified [ ] Detail\n- Implemented [x] — Verified [ ] Create',
    '- Implemented [x] — Verified [ ] Net\n- Implemented [ ] — Verified [ ] Previous month comparison\n- Implemented [ ] — Verified [ ] 12-month breakdown\n- Implemented [ ] — Verified [ ] Best-selling category':
    '- Implemented [x] — Verified [ ] Net\n- Implemented [x] — Verified [ ] Previous month comparison\n- Implemented [x] — Verified [ ] 12-month breakdown\n- Implemented [x] — Verified [ ] Best-selling category',
    '- Implemented [ ] — Verified [ ] User management\n- Implemented [ ] — Verified [ ] Permission management':
    '- Implemented [x] — Verified [ ] User management\n- Implemented [x] — Verified [ ] Permission management',
}
# The Kasbon replacement above is intentionally no-op unless the exact context is present.
for a,b in changes.items():
    if a in t:
        t = t.replace(a,b,1)
m.write_text(t)
PY

git diff --check
git add app/src/main/java/com/irkop/cell/MainActivity.kt app/src/main/java/com/irkop/cell/ParityExtras.kt Docs/ANDROID_PARITY_MATRIX.md fix/patch_batch_parity.sh
git commit -m "feat: wire batch parity screens"
git push origin main
