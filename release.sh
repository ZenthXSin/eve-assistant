#!/bin/sh
# Release script: tag & push to trigger GitHub Actions
# Usage: ./release.sh v1.0.0
set -e
if [ -z "$1" ]; then echo "Usage: $0 <tag> (e.g. v1.0.0)"; exit 1; fi
git add -A
git commit -m "release $1" || true
git tag -a "$1" -m "Eve Assistant Mod $1"
git push origin main --tags