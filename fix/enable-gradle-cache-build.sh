#!/usr/bin/env bash
set -euo pipefail

# File ini adalah panduan perubahan workflow cache.
# Jalankan dari root repository jika ingin menerapkannya secara lokal.
# GitHub Actions sendiri memakai gradle/actions/setup-gradle@v4, yang
# menyimpan Gradle User Home/cache antar-run selama cache tersedia.
#
# Tujuan: download dependency Gradle cukup sekali per cache key, sehingga
# build berikutnya dapat langsung masuk ke assembleDebug tanpa mengunduh
# ulang dependency yang sama.

workflow='.github/workflows/android-build.yml'

if [[ ! -f "$workflow" ]]; then
  echo "ERROR: $workflow tidak ditemukan."
  exit 1
fi

if ! grep -q 'gradle/actions/setup-gradle@v4' "$workflow"; then
  echo "ERROR: setup-gradle@v4 belum ada di workflow."
  exit 1
fi

# setup-gradle@v4 sudah menyediakan caching Gradle User Home.
# Jangan menambahkan cache manual yang tumpang tindih karena dapat membuat
# cache tidak konsisten dan justru memperlambat build.

echo "Gradle cache sudah aktif melalui gradle/actions/setup-gradle@v4."
echo "Tidak perlu download dependency Gradle berulang selama cache hit."
echo "Catatan: GitHub-hosted runner bersifat ephemeral, jadi cache bukan jaminan permanen satu kali seumur hidup."
