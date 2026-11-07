# Design

## Context

`server/feature-characters` is the example of a feature module. The admin app has no table with
fields in its cells and no choice field yet. The canvas shows the levels as a table with fields,
and the prompt block of the chosen level below it.

## Goals / Non-Goals

**Goals:**

- Level values that #12 and #13 read for `{level.rules}`, and that #11 extends with a model.

**Non-Goals:**

- A template inside the block. The block is plain text. The other values are for the owner and for
  the later prompts, which name them as variables if they need them.
- Saving one level alone. The rating from of a level depends on its neighbours, so the table saves
  as one unit.

## Decisions

### `server/feature-levels`

The module has `model/`, `usecase/`, `persistence/` and `api/`, as `feature-characters`.

### The model

```text
LevelCode = A1 | A2 | B1 | B2 | C1 | C2 | NO_RATING        (enum, the order is the order of the table)
Idioms = NEVER | RARELY | SOMETIMES | FREELY
LevelSettings(ratingFrom: Int?, vocabulary, maxWords: Int, idioms, block)
Level(code, settings: LevelSettings?, range: RatingRange?)
RatingRange(from: Int, to: Int?)
```

One rule in `model/` checks a full table: exactly one entry per code (no code left out, no code
twice), every text trimmed and filled, the maximum words above zero, a rating from on every code but
`NO_RATING`, none on `NO_RATING`, and a rating from that grows from A1 to C2. A second rule derives
the ranges from the saved values.

Alternative: the vocabulary as a choice of A1 to C2. Rejected: the canvas shows values outside that
list (see "Deviations from the canvas").

### `{level.rules}`

`GetLevelRulesUseCase(code)` returns the value of `{level.rules}` for a level: its saved block, or
nothing for a level without settings. #9 builds it and tests it with the stub repository. #12 calls
it when it builds the reply prompt, and #13 when it builds the hints prompt. #12 owns the
end-to-end check of the issue: a changed block shows in the next reply prompt for that level. #12
also decides what a prompt does for a level without settings.

### The table

```sql
create table levels (
    code text primary key,
    rating_from integer,
    vocabulary text not null,
    max_words integer not null,
    idioms text not null,
    block text not null
);
```

The server adds no rows at start. A save writes the seven rows with an upsert by `code`
(`insert ... on conflict (code) do update`) in one transaction. It never deletes a row. A read
returns all seven codes, each with its row or with no settings.

The upsert sets only the columns of the save. So a column that a later change adds keeps its value,
and a foreign key to `levels.code` stays valid.

The migration is `V3__levels.sql`. The orchestrator fixed the order of the migrations: `V2` (#7),
`V3` (#9), `V4` (#10). The part with `V3` merges only after the part of #7 with `V2__topics.sql`.

### The model override of #11

#11 adds the override as an optional `model` in `LevelSettings` and in `LevelSettingsDto`, and as
the nullable column `levels.model` in its own migration. No value means the model of the task. The
Model column sits in the same Levels table as the other values, so `PUT /admin/levels` carries it
with them. The rule "every value is required" stays for the five values of this change. The upsert
keeps the column on a save that does not set it.

### The wire contract

- `LevelDto(code, settings: LevelSettingsDto?, range: RatingRangeDto?)`. `GET /admin/levels`
  returns a JSON array of seven.
- `LevelSettingsDto(ratingFrom, vocabulary, maxWords, idioms, block)`. The codes and the idioms are
  strings of the enum names.
- `PUT /admin/levels` takes a JSON array of `LevelInputDto(code, settings)` and returns the same
  array as `GET`. `LevelInputDto.settings` is not nullable.
- The mapper reads a name with `entries.find { it.name == value } ?: throw BadRequestException(...)`,
  not with `valueOf`. `StatusHandling.kt` turns `BadRequestException` into 400. `valueOf` throws
  `IllegalArgumentException`, which ends in 500.

### The admin app

- `feature/levels` has a remote repository, two use cases and `LevelsViewModel`.
- The form keeps each number as the owner types it. Save reads the numbers first. A number that
  does not read gives an inline error with the level code, and no call. A 400 gives the error that
  asks to fill every level with growing ratings.
- The chosen level is B1 on the first open, as the canvas shows. A click on a row changes it. The
  block field below the table edits the block of the chosen level.
- `core/design` gets `AdminSelect` for Idioms, and a mono text style for the block, as the canvas
  shows it. #10 uses the mono style. If #10's screen part lands first, it adds the style, and this
  change uses it.
- Save sits in the toolbar. It uses the actions slot of `ScreenToolbar` that #7 adds. This change
  adds no second slot.
- This change adds the sidebar group Rules with Levels. #10 adds Prompts to the group.
- Rating shows the range from the server as text, and below it the field for the rating from. An
  unsaved level shows no range.
- The screen takes the names of the idioms from the canvas.
- The note under the block follows the canvas: the server puts the block into the reply and hints
  prompts. The server gives the value now. The prompts use it when #12 and #13 land.

### Deviations from the canvas

The Levels screen follows the canvas, with these deliberate differences:

- Vocabulary is a text field, not a select. The canvas values are not a closed list: C2 has "any",
  and No rating has the span `A2–B1`. A select needs a closed list that the server owns. A short text
  holds every value of the canvas.
- Rating has a field for the rating from under the range. The canvas shows the range as text only.
  The issue says that the owner tunes the levels without a release, and each level has a rating
  range. With text only, the range can change only in a release.
- The Model column is not in the table. It comes with #11.

### Tests

- The repository runs against PostgreSQL: an empty read, a save, a second save, the clean bootstrap.
- The model tests cover each rejection of the table rule and the ranges.
- The use-case tests cover a rejected table and the value of `{level.rules}`.
- The route tests check 200, 400 for an unknown code and for a rejected table, 401, and the JSON.
- The admin tests cover the repository with the mock engine, and the view model and the mapper with
  stub use cases. The screen capture gets Levels in each state.

### The PRs

`tasks.md` has one group for each PR, in merge order, with the files of each part. Each PR is at
most 400 lines.

## Risks / Trade-offs

- [The table needs every value before the first save] → The owner fills seven rows once. The canvas
  gives the starting values.
- [Three open changes add migrations: `V2` (#7), `V3` (#9), `V4` (#10)] → They deploy in this
  order. `DatabaseMigrations.kt` ignores only pending migrations. A database with `V3` applied and
  `V2` added later fails the validation. The `Dockerfile` turns the reset flag on, so the server then
  clears the database, and the saved characters, topics and levels are lost. So part 2 of this change
  merges only after the `V2` part of #7, and the `V4` part of #10 merges after part 2.
- [`GetLevelRulesUseCase` has no caller until #12] → The orchestrator decided that #9 provides and
  tests the value. #12 is its caller.
