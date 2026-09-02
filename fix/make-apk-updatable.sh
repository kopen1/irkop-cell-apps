#!/usr/bin/env bash
set -euo pipefail

# This script documents and verifies the update-safe APK configuration.
# It is intentionally NOT executed automatically.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLE="$ROOT/app/build.gradle.kts"
WORKFLOW="$ROOT/.github/workflows/android-build.yml"

if ! grep -q 'APP_VERSION_CODE' "$GRADLE"; then
  echo 'ERROR: APP_VERSION_CODE belum tersedia di app/build.gradle.kts.'
  exit 1
fi

if ! grep -q 'irkop-cell-debug-keystore-v1' "$WORKFLOW"; then
  echo 'ERROR: stable debug signing-key cache belum tersedia di workflow.'
  exit 1
fi

if ! grep -q 'github.run_number' "$WORKFLOW"; then
  echo 'ERROR: workflow belum memakai nomor run sebagai versionCode.'
  exit 1
fi

echo 'APK update-safe configuration PASS.'
echo 'applicationId tetap com.irkop.cell.debug untuk debug APK.'
echo 'versionCode naik mengikuti GitHub Actions run number.'
echo 'debug.keystore dipertahankan melalui Actions cache irkop-cell-debug-keystore-v1.'
echo 'Jangan menghapus cache signing tersebut selama APK lama masih digunakan.'
