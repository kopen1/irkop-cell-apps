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


## Production hardening
- Friendly HTTP error handling for 400/401/403/404/409/500.
- Annual reports and admin API declarations are wired.
- Service HP create/update API declarations are wired.
- Release workflow uses a deterministic Gradle version.
- Release APK remains unsigned until a keystore is supplied through GitHub Secrets.

## Signed Android Release

Release signing is designed around a one-time GitHub-generated keystore.

1. In GitHub, open **Actions → Android Release → Run workflow**.
2. The workflow generates `irkop-cell-release.jks` using the production environment signing secrets and uploads it as a short-lived artifact.
3. Download the artifact and store the `.jks` file securely as the master backup.
4. Encode that exact file with `./scripts/encode-keystore.sh ./irkop-cell-release.jks`.
5. Put the Base64 output into the production secret `ANDROID_KEYSTORE_BASE64`.
6. Keep `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` in GitHub Secrets.
7. Only after those four secrets are configured should you push a `v*` tag. The workflow then builds and verifies the signed APK and AAB and creates the GitHub Release.

Required production environment secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Optional production environment variable:

- `API_BASE_URL` (defaults to `https://konter.irkop.workers.dev/api/v1/`)

The keystore is never committed to Git. Keep the downloaded `.jks` backup offline and protected. Do not generate a replacement keystore for an existing app unless you intentionally plan a signing-key migration.

### CI JVM compatibility

Android Java and Kotlin compilation are explicitly aligned to JVM 17 using the Kotlin JVM toolchain and Android `compileOptions`, matching the GitHub Actions Java 17 environment.


### Keystore bootstrap

Run the manual `Generate Android Keystore` workflow once after setting the three signing secrets. Download the resulting `.jks` artifact and keep it as the master backup. Do not generate a new signing key for subsequent releases.
