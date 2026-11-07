# Design

## Context

`server/feature-characters` is the example of a feature module. The canvas shows the Prompts screen
with a list on the left and the editor, the variables and Versions on the right. Two other plans
change the same shared files first:

- #7 (`add-topic-draft`) adds `V2__topics.sql` and `exposed-java-time` in its part 2/9, and the
  actions slot of `ScreenToolbar` in its part 6/9.
- #9 (`add-rating-levels`) adds `V3__levels.sql` in its part 2/7, and the mono style and the
  sidebar group Rules in its part 6/7.

## Goals / Non-Goals

**Goals:**

- A current text that #12, #13 and #14 read at each call, so a save reaches the users at once.
- One meaning for each variable, so #12, #13 and #14 fill it the same way.

**Non-Goals:**

- Concurrent saves of one prompt. The admin app has one owner. The primary key still rejects a
  second version with the same number.
- A view of an older text before Restore.
- The substitution itself. #12, #13 and #14 build it.

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

### The variables

Each variable is replaced by plain text. Text in braces that is not a variable of the prompt stays
as it is.

| Variable | Value |
| --- | --- |
| `{interlocutor}` | A character of the cast: three lines, `Name: <name>`, `Personality: <personality>`, `Manner: <manner>`. A guest: the guest description of the topic. |
| `{topic.title}` | The title of the topic. |
| `{topic.setting}` | The setting of the topic. |
| `{topic.role}` | The role of the interlocutor in the scene. |
| `{level.rules}` | The prompt block of the level of the learner (#9). A learner without a rating and the calibration get the block of No rating. |
| `{step.n}` | The number of the current step, from 1, in digits. |
| `{step.task}` | The task of the current step. In the calibration, the current question. |
| `{step.passWhen}` | The Pass when condition of the current step. |
| `{steps}` | All steps of the topic, one per line: `<n>. <task>`. |
| `{last.line}` | The last English line of the interlocutor before the answer. |
| `{answers}` | Step check: the answers of the learner in the current step, one per line, the newest last. Review: all answers of the topic, one per line: `<step n>. <answer>`. |
| `{answer}` | The answer of the learner to the current calibration question. |
| `{hint.level}` | `1` for the start of a phrase, `2` for a full example phrase (#13). |
| `{check.verdict}` | The verdict of the check that ran before the reply: `accepted`, `continue`, `unclear` or `not_accepted` (#12). |

A guest has no name, personality or manner. So Character reply has one `{interlocutor}` for both
kinds of interlocutor, not `{character.*}`.

### How an engine sends a prompt

The prompt, with its variables replaced, is the first `ModelMessage` with `ModelRole.SYSTEM`.

- Character reply and Sam's reply: after the system message comes the dialogue, oldest first. A line
  of the interlocutor or Sam is `ASSISTANT`. A line of the learner is `USER`. So the line of the
  learner needs no variable.
- Step check, Hints, Review and Answer check: the system message is the whole request. Their
  variables carry the dialogue that they need.

### Sam's reply and the level

Sam's reply gets `{level.rules}`. The server fills it with the block of No rating.

- Option A: a fixed level, such as A2–B1, in the text of the prompt, as the canvas shows.
- Option B: `{level.rules}` of No rating. #9 says that No rating is for the calibration.

Choice: B. The owner sets the speech of a learner without a rating in one place, the Levels screen.
The text of a prompt cannot go out of step with that block.

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

The migrations merge in the order `V2` (#7), `V3` (#9), `V4` (#10). Part 2/8 starts after part 2/7
of #9 merges, and that part starts after part 2/9 of #7.

`exposed-java-time` gives the `saved_at` column. Part 2/9 of #7 adds it to
`gradle/libs.versions.toml`, because `release_date` needs it first. Part 2/8 of this change adds it
only to `server/feature-prompts/build.gradle.kts`, and to the catalog only if it is absent.

### The wire contract

- `PromptSummaryDto(promptKey, group)`: `GET /admin/prompts` returns an array of six, in order.
- `PromptDto(promptKey, group, variables, text?, versions: List<PromptVersionDto>)`.
- `PromptVersionDto(number, savedAt, isCurrent)`. `savedAt` is an ISO instant.
- `PromptTextDto(text)` is the body of a save. A restore has no body.
- A key in the path that is not a `PromptKey` name ends in `NotFoundException`, so 404.

The list has no version number: the list on the canvas shows only the title and when the prompt is
used. The editor gets its version from `PromptDto`.

### The admin app

- `feature/prompts` has a remote repository, use cases for the list, one prompt, save and restore,
  and `PromptsViewModel`.
- The admin app maps each key to its Russian title and to when it is used, from the canvas. The
  server returns the order and the groups.
- The view model loads the list, then the chosen prompt. It keeps the typed text of each prompt by
  key until a save or a restore of that prompt.
- Save sends the typed text as it is. The admin app does not check it. The server trims it and
  rejects an empty text with 400. The admin app maps the 400 to the error beside Save that asks for
  the text.
- The editor uses the mono style of #9 for the text and a chip for each variable. `core/design`
  gets `Chip`.
- The toolbar puts Save in the actions slot of `ScreenToolbar` from #7.
- The sidebar group Rules comes from #9. This change adds only Prompts to it, after Levels.
- A version shows `v<n>`, its day, month and time in the local zone, and a label that it is live
  for the users for the current one, or Restore for an older one.

### Tests

- The model test checks the six keys: their order, their groups and the variables of each one.
- The repository runs against PostgreSQL: a read before a save, two saves, a restore, a save after
  a restore, an unknown version, the clean bootstrap.
- The use-case tests stub the repository: the trimmed text, the empty text and the unknown version.
- The route tests check 200, 201, 400, 401 and 404 for a key and for a version, and the six prompts
  of the list.
- The admin tests cover the repository with the mock engine, and the view model and the mapper with
  stub use cases. The screen capture gets Prompts in each state.

### The PRs

| Part | Content | Lines, about |
| --- | --- | --- |
| 1/8 | This change | — |
| 2/8 | The server model and persistence | 330 |
| 3/8 | The server use cases | 200 |
| 4/8 | The DTOs and the routes | 330 |
| 5/8 | The admin data layer and use cases | 380 |
| 6/8 | The view model | 380 |
| 7/8 | The screen | 350 |
| 8/8 | The archive | — |

Each PR is at most 400 lines.

## Risks / Trade-offs

- [A saved typo reaches the users at once] → Restore returns the earlier text in one click. Try at a
  topic (#15) checks a text before a save.
- [The variables live in code] → A prompt that needs a new variable changes with the engine that
  substitutes it, in the same PR.
- [The screen part waits for two other chains] → The server and the data parts do not wait. Only
  part 7/8 needs #7 part 6/9 and #9 part 6/7.
