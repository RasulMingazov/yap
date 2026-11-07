#!/bin/sh
# merge-pr.sh <pr>: merges a reviewed PR when its checks pass, then cleans up after it.
# Prints one line per step. For the orchestrator; run it in the background.
pr="${1:?usage: merge-pr.sh <pr>}"
scripts="$(cd "$(dirname "$0")" && pwd)"
main="$(dirname "$(git rev-parse --path-format=absolute --git-common-dir)")"
branch="$(gh pr view "$pr" --json headRefName -q .headRefName)" || { echo "FAILED · cannot read PR #$pr"; exit 1; }

if [ "$(gh pr view "$pr" --json state -q .state)" != MERGED ]; then
  gh pr checks "$pr" >/dev/null 2>&1
  case $? in
    0) ;;
    8) echo "WAIT · checks still running on #$pr"; exit 1 ;;
    *) echo "FAILED · checks of #$pr"; gh pr checks "$pr" 2>&1 | grep -v -E '\bpass\b' | head -10; exit 1 ;;
  esac
  gh pr merge "$pr" --merge || { echo "FAILED · merge of #$pr"; exit 1; }
fi
echo "MERGED #$pr · $(gh pr view "$pr" --json mergeCommit -q .mergeCommit.oid | cut -c1-7)"

body="$(gh pr view "$pr" --json body -q .body)"
for issue in $(printf '%s\n' "$body" | grep -o -i -E '^(closes|part of) #[0-9]+' | grep -o '[0-9]*$'); do
  if printf '%s\n' "$body" | grep -q -i "^closes #$issue"; then stage=merged; else stage=build; fi
  node "$scripts/issue-state.mjs" "$issue" "stage=$stage" --log "merged #$pr" >/dev/null && echo "ISSUE #$issue · $stage"
done

# Move the PRs stacked on this branch to develop before the branch goes: GitHub closes them
# otherwise. A base change starts no checks, so merge develop into each one to push a commit.
for dep in $(gh pr list --base "$branch" --state open --json number -q '.[].number'); do
  gh pr edit "$dep" --base develop >/dev/null || { echo "FAILED · base of #$dep: branch $branch kept"; exit 1; }
  echo "RETARGETED #$dep · develop"
  if gh api -X PUT "repos/{owner}/{repo}/pulls/$dep/update-branch" >/dev/null 2>&1; then
    echo "UPDATED #$dep · develop merged in"
  else
    echo "NOT UPDATED #$dep · merge develop into it by hand"
  fi
done

gh api -X DELETE "repos/{owner}/{repo}/git/refs/heads/$branch" >/dev/null 2>&1 && echo "REMOTE BRANCH deleted · $branch"
worktree="$(git worktree list --porcelain | awk -v ref="refs/heads/$branch" '
  /^worktree / { path = substr($0, 10); entries++ }
  entries > 1 && $0 == "branch " ref { print path }')"
if [ -n "$worktree" ] && [ "$worktree" != "$main" ]; then
  if git worktree remove "$worktree" 2>/dev/null; then echo "WORKTREE removed · $worktree"; else echo "WORKTREE kept · $worktree"; fi
fi
git fetch -q origin develop
git merge-base --is-ancestor "$branch" origin/develop 2>/dev/null && git branch -D -q "$branch" && echo "LOCAL BRANCH deleted · $branch"
exit 0
