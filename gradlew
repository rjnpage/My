#!/usr/bin/env sh

set -eu

APP_HOME=$(cd "${0%/*}" && pwd -P)
PROPS_FILE="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPS_FILE" ]; then
  echo "Missing $PROPS_FILE"
  exit 1
fi

DIST_URL=$(grep '^distributionUrl=' "$PROPS_FILE" | cut -d= -f2- | sed 's#\\:#:#g')
DIST_NAME=$(basename "$DIST_URL" .zip)
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/$DIST_NAME"
GRADLE_BIN=$(find "$DIST_DIR" -type f -path '*/bin/gradle' 2>/dev/null | head -n1 || true)

if [ -z "$GRADLE_BIN" ]; then
  TMP_ZIP="$GRADLE_USER_HOME/wrapper/$DIST_NAME.zip"
  mkdir -p "$(dirname "$TMP_ZIP")" "$DIST_DIR"
  echo "Downloading Gradle distribution: $DIST_URL"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DIST_URL" -o "$TMP_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$DIST_URL" -O "$TMP_ZIP"
  else
    echo "Neither curl nor wget is available"
    exit 1
  fi
  unzip -qo "$TMP_ZIP" -d "$DIST_DIR"
  GRADLE_BIN=$(find "$DIST_DIR" -type f -path '*/bin/gradle' | head -n1)
fi

exec "$GRADLE_BIN" "$@"
