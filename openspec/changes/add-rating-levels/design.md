# Design

## Context

`server/feature-characters` is the example of a feature module. The admin app has no table with
fields in its cells and no choice field yet.

## Goals / Non-Goals

**Goals:**

- Level values that #12 and #13 read for `{level.rules}`, and that #11 extends with a model.

**Non-Goals:**

- A template inside the block. The block is plain text. The other values are for the owner and for
  the later prompts, which name them as variables if they need them.
- Saving one level alone. The rating from of a level depends on its neighbours, so the table saves
  as one unit.

## Decisions

### The model

`server/feature-levels` has `model/`, `usecase/`, `persistence/` and `api/`, as `feature-characters`.

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

### `{level.rules}`

`GetLevelRulesUseCase(code)` returns the value of `{level.rules}` for a level: its saved block, or
nothing for a level without settings. #12 calls it for the reply prompt, #13 for the hints prompt.
#12 owns the end-to-end check of the issue and decides what a prompt does for a level without
settings.

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
returns all seven codes, each with its row or with no settings. The upsert sets only the columns of
the save, so a later column keeps its value and a foreign key to `levels.code` stays valid. The
migration is `V3__levels.sql` (see "Risks").

### The model override of #11

#11 adds the override as an optional `model` in `LevelSettings` and in `LevelSettingsDto`, and as
the nullable column `levels.model`. No value means the model of the task. `PUT /admin/levels`
carries it with the other values. "Every value is required" stays for the five values of this
change.

### The wire contract

- `LevelDto(code, settings: LevelSettingsDto?, range: RatingRangeDto?)`. `GET /admin/levels`
  returns a JSON array of seven.
- `LevelSettingsDto(ratingFrom, vocabulary, maxWords, idioms, block)`. The codes and the idioms are
  strings of the enum names.
- `PUT /admin/levels` takes a JSON array of `LevelInputDto(code, settings)` and returns the same
  array as `GET`. `LevelInputDto.settings` is not nullable.
- The mapper reads a name with `entries.find { it.name == value } ?: throw BadRequestException(...)`,
  so 400. `valueOf` throws `IllegalArgumentException`, which ends in 500.

### The admin app

- `feature/levels` has a remote repository, two use cases and `LevelsViewModel`.
- The form keeps each number as the owner types it. Save reads the numbers first. A number that
  does not read gives an inline error with the level code, and no call. A 400 gives the error that
  asks to fill every level with growing ratings.
- The chosen level is B1 on the first open, as the canvas shows. A click on a row changes it. The
  block field below the table edits the block of the chosen level.
- `core/design` gets `AdminSelect` for Idioms, and a mono text style for the block, as the canvas
  shows it. If #10's screen part lands first, it adds the mono style, and this change uses it.
- Save uses the actions slot of `ScreenToolbar` that #7 adds. This change adds no second slot.
- This change adds the sidebar group Rules with Levels. #10 adds Prompts to the group.
- Rating shows the range from the server as text, and below it the field for the rating from. An
  unsaved level shows no range.
- The screen takes the names of the idioms from the canvas.
- The note under the block follows the canvas. The prompts use the value when #12 and #13 land.

### Deviations from the canvas

- Vocabulary is a text field, not a select. The canvas values are not a closed list: C2 has "any",
  and No rating has the span `A2–B1`.
- Rating has a field for the rating from under the range. The canvas shows text only. The issue
  says that the owner tunes the levels without a release.
- The Model column is not in the table. It comes with #11.

## Risks / Trade-offs

- [The table needs every value before the first save] → The owner fills seven rows once. The canvas
  gives the starting values.
- [Migrations `V2` (#7), `V3` (#9), `V4` (#10)] → They deploy in this order. With `V3` applied, a
  later `V2` fails the validation, and the reset flag of the `Dockerfile` clears the database. So
  part 2 merges after the `V2` part of #7, and the `V4` part of #10 after part 2.
- [`GetLevelRulesUseCase` has no caller until #12] → The orchestrator's decision. #12 calls it.
