#!/bin/sh
# Runs `./gradlew build` (or the tasks given) with short output for agents: one line on success,
# or the failing lines on failure. The full log goes to a file whose path is printed.
# A passing full build of a clean tree marks its commit, so the agent gate does not build it again.
log="${TMPDIR:-/tmp}/build-$(git rev-parse --short HEAD 2>/dev/null || echo nogit)-$$.log"
./gradlew "${@:-build}" --console=plain > "$log" 2>&1
status=$?
if [ "$status" -eq 0 ]; then
  if [ "$#" -eq 0 ] && [ -z "$(git status --porcelain 2>/dev/null)" ]; then
    marks="$(git rev-parse --path-format=absolute --git-common-dir)/build-ok"
    mkdir -p "$marks" && touch "$marks/$(git rev-parse HEAD)"
  fi
  echo "BUILD OK · log: $log"
  exit 0
fi
echo "BUILD FAILED · log: $log"
awk '
  /What went wrong/ { wrong = 4 }
  wrong > 0 { print; wrong--; next }
  /^> (Task|Configure|Run with|Get more help|For more)/ { next }
  /^e: |FAILED|AssertionError|Exception:|detekt.*(issue|weighted)/ { print }
' "$log" | head -40
exit "$status"
