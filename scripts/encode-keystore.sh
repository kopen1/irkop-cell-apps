#!/usr/bin/env bash
set -euo pipefail

KEYSTORE="${1:-./irkop-cell-release.jks}"

test -f "$KEYSTORE" || {
  echo "Keystore not found: $KEYSTORE" >&2
  exit 1
}

echo "Copy the following output into GitHub secret ANDROID_KEYSTORE_BASE64:"
echo
if base64 --help 2>&1 | grep -q -- '-w'; then
  base64 -w 0 "$KEYSTORE"
else
  base64 "$KEYSTORE" | tr -d '\\n'
fi
echo
