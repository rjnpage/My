#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${JAVA_HOME:-}" ]]; then
  for CANDIDATE in \
    /root/.local/share/mise/installs/java/17.0.2 \
    /usr/lib/jvm/java-17-openjdk-amd64 \
    /usr/lib/jvm/java-17-openjdk
  do
    if [[ -d "$CANDIDATE" ]]; then
      export JAVA_HOME="$CANDIDATE"
      break
    fi
  done
fi

if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/java" ]]; then
  echo "ERROR: JAVA_HOME must point to JDK 17." >&2
  exit 1
fi

export PATH="$JAVA_HOME/bin:$PATH"

BUILD_TYPE="${1:-debug}"
case "$BUILD_TYPE" in
  debug)
    TASK="assembleDebug"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    ;;
  release)
    TASK="assembleRelease"
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
    ;;
  *)
    echo "Usage: $0 [debug|release]" >&2
    exit 1
    ;;
esac

if [[ ! -f "gradlew" ]]; then
  echo "ERROR: Missing ./gradlew. Create Gradle wrapper first in an online environment:" >&2
  echo "  JAVA_HOME=<jdk17> gradle wrapper --gradle-version 8.14.3" >&2
  exit 1
fi

if [[ "$BUILD_TYPE" == "release" ]]; then
  REQUIRED=(SMARTQR_KEYSTORE_PATH SMARTQR_KEYSTORE_PASSWORD SMARTQR_KEY_ALIAS SMARTQR_KEY_PASSWORD)
  for VAR in "${REQUIRED[@]}"; do
    if [[ -z "${!VAR:-}" ]]; then
      echo "ERROR: $VAR is required for release builds." >&2
      exit 1
    fi
  done
fi

./gradlew --no-daemon "$TASK"

echo "APK generated: $APK_PATH"
