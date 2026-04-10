#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
WEB_REPO_DIR="$ROOT_DIR/web-app-repo"
ANDROID_WEB_DIR="$ROOT_DIR/android-app/app/src/main/assets/web"

if [ ! -d "$WEB_REPO_DIR" ]; then
  echo "Missing web-app-repo directory. Clone or copy the cricket-academy-manager repo into $WEB_REPO_DIR first."
  exit 1
fi

mkdir -p "$ANDROID_WEB_DIR/assets"

cp "$WEB_REPO_DIR/index.html" "$ANDROID_WEB_DIR/index.html"
cp "$WEB_REPO_DIR/styles.css" "$ANDROID_WEB_DIR/styles.css"
cp "$WEB_REPO_DIR/script.js" "$ANDROID_WEB_DIR/script.js"
cp "$WEB_REPO_DIR/supabase-config.js" "$ANDROID_WEB_DIR/supabase-config.js"
cp "$WEB_REPO_DIR/manifest.webmanifest" "$ANDROID_WEB_DIR/manifest.webmanifest"
cp "$WEB_REPO_DIR/sw.js" "$ANDROID_WEB_DIR/sw.js"
cp -R "$WEB_REPO_DIR/assets/." "$ANDROID_WEB_DIR/assets/"

echo "Android web assets synced."
