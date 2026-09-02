#!/usr/bin/env bash
set -euo pipefail

# Configure APK builds so successive APKs can update the previously installed APK.
# IMPORTANT: Android update requires the same applicationId, the same signing
# certificate, and a versionCode that is >= the installed version.
#
# This script only prepares the source/workflow changes. It is intentionally not
# executed automatically by this commit.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLE="$ROOT/app/build.gradle.kts"
WORKFLOW="$ROOT/.github/workflows/android-build.yml"

python3 - "$GRADLE" "$WORKFLOW" <<'PY'
from pathlib import Path
import sys

gradle = Path(sys.argv[1])
workflow = Path(sys.argv[2])

g = gradle.read_text()
old = '''        versionCode = 1\n        versionName = "1.0.0"'''
new = '''        // Keep the applicationId stable and let CI provide a monotonically\n        // increasing versionCode so a newly downloaded APK can update the\n        // previously installed APK instead of requiring uninstall first.\n        versionCode = (project.findProperty("VERSION_CODE") as String?)?.toInt() ?: 1\n        versionName = project.findProperty("VERSION_NAME") as String? ?: "1.0.0"'''
if old not in g:
    raise SystemExit("ERROR: expected versionCode/versionName block not found")
g = g.replace(old, new, 1)
gradle.write_text(g)

w = workflow.read_text()
old_step = '''      - name: Build debug APK\n        shell: bash\n        run: |\n          set -e\n          ./gradlew assembleDebug'''
new_step = '''      - name: Build debug APK\n        shell: bash\n        run: |\n          set -e\n          # GitHub run_number is monotonically increasing for this workflow,\n          # so each APK gets a newer versionCode than the previous build.\n          ./gradlew assembleDebug -PVERSION_CODE="${GITHUB_RUN_NUMBER}" -PVERSION_NAME="1.0.${GITHUB_RUN_NUMBER}"'''
if old_step not in w:
    raise SystemExit("ERROR: expected Build debug APK step not found")
w = w.replace(old_step, new_step, 1)
workflow.write_text(w)
PY

echo "Prepared updatable APK versioning."
echo "NOTE: versionCode alone is not enough: the APKs must also use the same signing certificate."
echo "For production/distributed APKs, configure a persistent release keystore in GitHub Actions secrets and build the signed release APK."
