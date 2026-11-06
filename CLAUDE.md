# Yap

Gradle monorepo for Yap, an app for English speaking practice.

| Module | What it is |
| --- | --- |
| `server/app` | Ktor server. Holds scenarios, prompts and model keys. Reads its settings from environment variables and `.env`. Deployed to Timeweb. |
| `server/core-database` | PostgreSQL connection and Flyway migrations. |
| `server/core-llm` | Model provider interface and its adapters. Only DeepSeek for now. |
| `admin` | Compose Desktop app for the owner. Talks only to the server; holds no keys. |
| `contract-admin` | DTOs of the admin API, shared by `server` and `admin`. Data only, no logic. |

`convention-plugins` holds the shared Gradle setup. Versions live in `gradle/libs.versions.toml`.

## Rules

- Code guides live in `docs/`. Start at `docs/README.md` and read the guides for the area you change.
- No secrets in the repository. Keys and prompts live on the server and in its database.
- A DTO module holds only wire data. The server maps DTOs to its own models.
- Issues, PRs, specs and docs are in English, also the names of screens and buttons. The Russian
  copy lives only in the code and in the design canvas.
- Add a rule to this file only when code it applies to exists.

## Specs

OpenSpec specifies product behaviour. `openspec/specs/<capability>/spec.md` is the current truth.
A change in progress is a folder under `openspec/changes/`.

- Specs say what the product does. `CLAUDE.md`, `docs/` and the code say how it is built.
- Start a change with `/opsx:propose`. Build it with `/opsx:apply`.
- Run `/opsx:archive` as the last commit of the PR, so the specs update when it merges.
- A fix that changes no specified behaviour needs no change folder.

| Question | Source |
| --- | --- |
| What the business wants and what to do next | GitHub issues |
| Exact product behaviour | `openspec/specs/` |
| How the product works now | the code |
| How new work looks | the [design canvas](https://claude.ai/artifact/B4bBUESuzCQw6v3N6Nj8FB) |

## Verification

- `./gradlew build` — compilation, tests and Detekt. Run it before every push.
- `./gradlew :admin:run` — opens the admin window.
- `docker compose -f docker/compose.yaml up -d` — starts the local PostgreSQL that
  `./gradlew :server:app:run` needs. The PostgreSQL tests use Testcontainers and need Docker running.
- Run `git config core.hooksPath .githooks` once per clone. It enables the `pre-push` hook, which
  runs Detekt and blocks the push on a violation.
- CI (`.github/workflows/build.yml`) runs `build` and a `docker` image build on every non-draft PR
  into `develop` or `main` and on every push to them. Merge only when both checks are green.

## Code review

The `Claude review` workflow (`.github/workflows/claude-review.yml`) reviews every non-draft PR
and runs again after each push. It needs the repository secret `CLAUDE_CODE_OAUTH_TOKEN`.

When you are the PR author:

- Answer every review thread. Fix the point, or reply in the thread with the reason you keep the code.
- Push once per review round. Wait for the "Claude review:" summary comment, fix all threads of
  that round, then make one commit and one push.
- A thread holds at most 5 messages, review comment included. Do not write message 5: the reviewer
  writes it as "Owner decision needed". Then wait for the owner.

## Git

- Branch names start with the kind: `feature/...`, `tech/...` or `test/...`. A branch for an issue
  puts the issue number after the kind, for example `feature/6-characters`.
- Start every branch from `develop` and open every PR into `develop`. A release is a PR from
  `develop` into `main`.
- The title of a PR for an issue starts with `Issue #<number>:`, for example
  `Issue #6: Add the character list to the admin app`. The word `Issue` shows that the number
  is an issue number, not a PR number.
- The description of a PR for an issue starts with `Closes #<number>`, so GitHub closes the issue
  at the merge.
- Keep PR descriptions short: what changed and why, then what was verified.
- A PR adds at most 400 lines. When an issue does not fit, do not open a new issue: split the
  work into several PRs for the same issue. Put the part number after the issue number, for
  example `Issue #6 [1/3]: Add the character list to the admin app`. Only the last part closes
  the issue: the other parts start their description with `Part of #<number>`.
