#!/usr/bin/env bash

set -euo pipefail

releases=$1
debug=${2:-}

[[ -n "$debug" ]] && tree

mkdir -p "$releases"
artifacts=(bf-x86_64-apple-darwin bf-x86_64-linux)

for artifact in "${artifacts[@]}"; do
  chmod +x "$artifact/bf"
  tar cvfz "$releases/$artifact.tar.gz" "$artifact/bf"
done
