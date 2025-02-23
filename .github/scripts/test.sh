#!/usr/bin/env bash

set -euo pipefail

declare -A examples=(
  ['helloworld']='Hello World!'
  ['addition']='7'
  ['bubblesort:978365412']='123456789'
  ['bubblesort:aZefcYbdX']='XYZabcdef'
  ['rot13:Uryyb, Jbeyq!']='Hello, World!'
  ['rot13:Hello, World!']='Uryyb, Jbeyq!'
)

for example in "${!examples[@]}"; do
  name="$example"
  input=""
  expected="${examples[$example]}"

  if [[ $example == *:* ]]; then
    name="${example%%:*}"
    input="${example##*:}"
  fi

  file="examples/$name.b"

  if [ ! -f "$file" ]; then
    echo "error: File $file doesn't exist"
    exit 1
  fi

  output=$(echo -n "$input" | scala-cli run . -q -- "$(< "$file")")

  echo "Running $file $input"
  if [ "$output" = "$expected" ]; then
    echo "✓ $file passed"
  else
    echo "✗ $file failed"
    echo "  expected: '$expected'"
    echo "  got:      '$output'"
    exit 1
  fi
done
