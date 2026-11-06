# Yap

Gradle monorepo for Yap, an app for English speaking practice.

| Module | What it is |
| --- | --- |
| `server` | Ktor server. Holds scenarios, prompts and model keys. Deployed to Timeweb. |
| `admin` | Compose Desktop app for the owner. Talks only to the server; holds no keys. |
| `contract-admin` | DTOs of the admin API, shared by `server` and `admin`. Data only, no logic. |

`convention-plugins` holds the shared Gradle setup. Versions live in `gradle/libs.versions.toml`.

## Rules

- Code guides live in `docs/`. Start at `docs/README.md` and read the guides for the area you change.
- No secrets in the repository. Keys and prompts live on the server and in its database.
- A DTO module holds only wire data. The server maps DTOs to its own models.
- Add a rule to this file only when code it applies to exists.

## Verification

- `./gradlew build` — compilation, tests and Detekt. Run it before every push.
- `./gradlew :admin:run` — opens the admin window.
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

- Branch names start with the kind: `feature/...`, `tech/...` or `test/...`.
- Start every branch from `develop` and open every PR into `develop`. A release is a PR from
  `develop` into `main`.
- Keep PR descriptions short: what changed and why, then what was verified.
- A PR adds at most 400 lines. When an issue does not fit, do not open a new issue: split the
  work into several PRs for the same issue and number them in the title, for example `[1/3]`,
  `[2/3]`, `[3/3]`.
