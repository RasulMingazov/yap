# Process

`/orchestrate` runs the open backlog with the agents in `.claude/agents/`. The command and the
agents hold no facts about this project. The command reads this file. The agents read `CLAUDE.md`
and `docs/`, and know nothing about the command: each one works the same when the owner calls it.

## Limits

- Context limit: 500k. Over it, the orchestrator takes no new issue and finishes the ones in
  progress.
- Agents at once: 3. Never two on one module.
- Issues in a wave: 8.
- Plan review rounds: 2.
- Merge: on. The owner turns it off for a run in chat.

## Models

| Agent | Model | Effort |
| --- | --- | --- |
| `planner` | `opus` | `high` |
| `plan-reviewer` | `opus` | `medium` |
| `executor` | `sonnet`; `opus` for a part with an admin screen | default |
| `code-reviewer` | `opus` | `medium` |
| `ux-review`, `ui-review` | `opus` | `medium` |
| `process-critic` | `opus` | default |

An executor that needs more than two review rounds on `sonnet` resumes on `opus`.

## Reviews by area

| The diff changes | Reviewers |
| --- | --- |
| `openspec/changes/` only (a plan) | `plan-reviewer`, the Claude review |
| code | `code-reviewer` in the first round, the Claude review in each round |
| `admin/**/presentation/ui/` or `admin/**/core/design/` | also `ux-review` and `ui-review` |

## Hot files

Only one running chain may change one of these. Each plan names the files of each part.

- `gradle/libs.versions.toml`, `settings.gradle.kts`, `convention-plugins/`
- Flyway migrations: the orchestrator reserves the next `V<n>` for each plan.
- `contract-admin/`
- `server/app/src/main/kotlin/app/yap/server/Application.kt`
- `admin/src/main/kotlin/app/yap/admin/Main.kt`, `admin/**/navigation/`, `admin/**/core/design/`
- `CLAUDE.md`, `docs/`

## Labels

Kind: `feature`, `tech`, `bug`. Area: `server`, `admin`, `mobile`.

## Run files

`docs/process/runs/<run>/` stays local: `.gitignore` keeps it out of the repository.
