#!/usr/bin/env bash
# Reuse the checksummed FFmpeg bundle from the approved EVATEX 1.7.0 release.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
python3 "$SCRIPT_DIR/fetch_ffmpeg.py"
python3 "$SCRIPT_DIR/fetch_platform_ffmpeg.py"
