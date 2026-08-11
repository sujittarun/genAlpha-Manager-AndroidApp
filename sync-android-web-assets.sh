#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
# The web app is a SIBLING repo now, not a nested clone. The old
# web-app-repo/ copy was 10 commits stale and still pointed at the
# decommissioned Supabase project, so running this script used to
# overwrite the Android web assets with dead-database code.
WEB_REPO_DIR="${WEB_REPO_DIR:-$ROOT_DIR/../GenAlpha}"
ANDROID_WEB_DIR="$ROOT_DIR/android-app/app/src/main/assets/web"

if [ ! -d "$WEB_REPO_DIR" ]; then
  echo "Missing web app at $WEB_REPO_DIR. Expected the GenAlpha web repo as a sibling of this one."
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
