#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYSTORE="${1:-$APP_DIR/irkop-cell-release.jks}"
ALIAS="${2:-irkop-cell}"

echo "Generating Android release keystore:"
echo "  $KEYSTORE"
echo "  alias: $ALIAS"
echo
echo "Use a strong password when prompted."
echo

keytool -genkeypair       -v       -keystore "$KEYSTORE"       -alias "$ALIAS"       -keyalg RSA       -keysize 4096       -validity 10000       -dname "CN=IRKOP CELL, OU=Mobile, O=IRKOP CELL, L=Indonesia, C=ID"

echo
echo "Keystore created."
echo "Keep the .jks file and passwords private."
echo "Do NOT commit the keystore to Git."
