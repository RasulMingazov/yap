#!/bin/sh
# pr-watch.sh <pr>: waits for the checks and the Claude review of the head commit of a PR, then
# prints the checks, the review, the open threads and the merge state. For the orchestrator: run it
# in the background after each push; its end is the event.
pr="${1:?usage: pr-watch.sh <pr>}"
scripts="$(cd "$(dirname "$0")" && pwd)"
# A push registers its checks a few seconds later.
sleep 10
gh pr checks "$pr" --watch --interval 60 >/dev/null 2>&1
gh pr checks "$pr" 2>&1 | cut -f1,2
# The review comment can come a little after its check ends.
for _ in 1 2 3 4 5 6 7 8 9 10; do
  review="$(node "$scripts/pr-review.mjs" "$pr")"
  status=$?
  [ "$status" -ne 8 ] && break
  sleep 30
done
printf '%s\n' "$review"
echo "MERGE STATE · $(gh pr view "$pr" --json mergeStateStatus -q .mergeStateStatus)"
exit "$status"
