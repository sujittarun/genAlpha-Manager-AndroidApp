#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
ANDROID_WEB_DIR="$ROOT_DIR/android-app/app/src/main/assets/web"

mkdir -p "$ANDROID_WEB_DIR/assets"

cp "$ROOT_DIR/index.html" "$ANDROID_WEB_DIR/index.html"
cp "$ROOT_DIR/styles.css" "$ANDROID_WEB_DIR/styles.css"
cp "$ROOT_DIR/script.js" "$ANDROID_WEB_DIR/script.js"
cp "$ROOT_DIR/supabase-config.js" "$ANDROID_WEB_DIR/supabase-config.js"
cp "$ROOT_DIR/manifest.webmanifest" "$ANDROID_WEB_DIR/manifest.webmanifest"
cp "$ROOT_DIR/sw.js" "$ANDROID_WEB_DIR/sw.js"
cp -R "$ROOT_DIR/assets/." "$ANDROID_WEB_DIR/assets/"

echo "Android web assets synced."
