#!/usr/bin/env bash

set -euo pipefail

file="examples/$1.b"
expected=$2
input=${3:-}

if [ ! -f "$file" ]; then
  echo "error: file $file doesn't exist"
  exit 1
fi

if [ -n "$input" ]; then
  output=$(echo -n "$input" | scala-cli run . -q -- "$(< "$file")")
else
  output=$(scala-cli run . -q -- "$(< "$file")")
fi

if [ "$output" = "$expected" ]; then
  echo "✓ $file passed"
else
  echo "✗ $file failed"
  echo "  expected: '$expected'"
  echo "  got:      '$output'"
  exit 1
fi
