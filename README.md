# IRKOP CELL — Native Android

Native Android MVP for IRKOP CELL, using Kotlin + Jetpack Compose and the existing backend API.

## API
Default:
`https://konter.irkop.workers.dev/api/v1/`

Override in CI:
`gradle assembleDebug -PAPI_BASE_URL=https://example/api/v1/`

## Implemented in this sprint
- JWT login/session persistence
- Bearer authentication
- Dashboard + kasir current
- Opening / closing kasir
- Transaction cart with multiple products
- Payment modes: tunai, transfer, bon, cash_tunai
- Transfer receiver account validation
- Bon requires customer
- Manual entry/backdate field
- Product CRUD
- Category create/update
- Customer create + detail
- Kasbon repayment
- Expense creation with idempotency key
- Monthly report viewer
- GitHub Actions debug build, CI check, and tag release workflow

## Important
The Android project is generated from the current repository contract and frontend implementation. The local environment used to assemble this archive does not contain the Android SDK/Gradle cache, so final compilation should be verified by GitHub Actions.

Production release signing is intentionally not hardcoded. Add Android signing secrets before distributing a signed release APK.
