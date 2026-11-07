# Design

## Context

`server/feature-characters` is the example of a feature module. The canvas shows the Prompts screen
with a list on the left and the editor, the variables and Versions on the right. Exposed has no time
column in the current build.

## Goals / Non-Goals

**Goals:**

- A current text that #12, #13 and #14 read at each call, so a save reaches the users at once.

**Non-Goals:**

- Concurrent saves of one prompt. The admin app has one owner. The primary key still rejects a
  second version with the same number.
- A view of an older text before Restore.

## Decisions

### `server/feature-prompts`

The module has `model/`, `usecase/`, `persistence/` and `api/`, as `feature-characters`.

### The model

```text
PromptKey = TOPIC_REPLY | TOPIC_CHECK | TOPIC_HINTS | TOPIC_REVIEW | CALIBRATION_CHECK | CALIBRATION_REPLY
PromptGroup = TOPIC | CALIBRATION
PromptVersion(number: Int, savedAt: Instant, text)
Prompt(key, current: PromptVersion?, versions: List<PromptVersion>)
```

`PromptKey` holds its group and its variables in code: the server substitutes them, so the code
owns the list. The texts live only in the database. The canvas names the variables `{episode.*}`.
The product calls it a topic, so the variables are `{topic.*}`, and `{step.rule}` is
`{step.passWhen}` after the field of #7.

### The tables

```sql
create table prompt_versions (
    prompt_key text not null,
    number integer not null,
    text text not null,
    saved_at timestamp with time zone not null,
    primary key (prompt_key, number)
);

create table current_prompts (
    prompt_key text primary key,
    number integer not null,
    foreign key (prompt_key, number) references prompt_versions (prompt_key, number)
);
```

A save inserts the version with `max(number) + 1` and upserts the current row in one transaction.
Restore updates the current row only when the version exists. The migration is `V4__prompts.sql`.
`core-database` gets `exposed-java-time` for `saved_at`.

### The wire contract

- `PromptSummaryDto(promptKey, group, currentNumber?)`: `GET /admin/prompts` returns an array of six.
- `PromptDto(promptKey, group, variables, text?, versions: List<PromptVersionDto>)`.
- `PromptVersionDto(number, savedAt, isCurrent)`. `savedAt` is an ISO instant.
- `PromptTextDto(text)` is the body of a save. A restore has no body.
- A key in the path that is not a `PromptKey` name ends in `NotFoundException`, so 404.

### The admin app

- `feature/prompts` has a remote repository, use cases for the list, one prompt, save and restore,
  and `PromptsViewModel`.
- The admin app maps each key to its Russian title and to when it is used, from the canvas. The
  server returns the order and the groups.
- The view model loads the list, then the chosen prompt. It keeps the typed text of each prompt by
  key until a save or a restore of that prompt.
- The editor uses the mono style for the text and a chip for each variable. `core/design` gets
  `Chip`. The mono style comes with #9 or with this change, whichever lands first.
- A version shows `v<n>`, its day, month and time in the local zone, and a label that it is live
  for the users for the current one, or Restore for an older one.

### Tests

- The repository runs against PostgreSQL: a read before a save, two saves, a restore, a save after
  a restore, an unknown version, the clean bootstrap.
- The use-case tests stub the repository: the trimmed text and the empty text.
- The route tests check 200, 201, 400, 401 and 404 for a key and for a version.
- The admin tests cover the repository with the mock engine, and the view model and the mapper with
  stub use cases. The screen capture gets Prompts in each state.

### The PRs

This change; the server model and persistence; the use cases, the DTOs and the routes; the admin
data layer; the view model; the screen; the archive. Each PR is at most 400 lines.

## Risks / Trade-offs

- [A saved typo reaches the users at once] → Restore returns the earlier text in one click. Try at a
  topic (#15) checks a text before a save.
- [The variables live in code] → A prompt that needs a new variable changes with the engine that
  substitutes it, in the same PR.
