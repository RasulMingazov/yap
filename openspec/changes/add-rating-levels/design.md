# Design

## Context

`server/feature-characters` is the example of a feature module. The admin app has no table with
fields in its cells and no choice field yet. The canvas shows the levels as a table with fields,
and the prompt block of the chosen level below it.

## Goals / Non-Goals

**Goals:**

- Level values that #12 and #13 read for `{level.rules}` and #11 extends with a model.

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

One rule in `model/` checks a full table: one entry per code, every text trimmed and filled, the
maximum words above zero, a rating from on every code but `NO_RATING`, none on `NO_RATING`, and a
rating from that grows from A1 to C2. A second rule derives the ranges from the saved values.

Alternative: the vocabulary as a choice of A1 to C2. Rejected: No rating uses a span, such as
`A2–B1`, on the canvas. A short text covers both.

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

The server adds no rows at start. A save deletes the rows and inserts seven new ones in one
transaction. A read returns all seven codes, each with its row or with no settings. The migration is
`V3__levels.sql`: #7 takes `V2`. If #7 merges later, the numbers stay as they are.

### The wire contract

- `LevelDto(code, settings: LevelSettingsDto?, range: RatingRangeDto?)`. `GET /admin/levels`
  returns a JSON array of seven.
- `LevelSettingsDto(ratingFrom, vocabulary, maxWords, idioms, block)`. The codes and the idioms are
  strings of the enum names. A string that is not a name ends in `BadRequestException`, so 400.
- `PUT /admin/levels` takes a JSON array of `LevelInputDto(code, settings)` and returns the same
  array as `GET`.

### The admin app

- `feature/levels` has a remote repository, two use cases and `LevelsViewModel`.
- The form keeps each number as the owner types it. Save reads the numbers first. A number that
  does not read gives an inline error with the level code, and no call. A 400 gives the error that
  asks to fill every level with growing ratings.
- The chosen level is A1 on the first open. A click on a row changes it. The block field below the
  table edits the block of the chosen level.
- `core/design` gets `AdminSelect` for Idioms, and a mono text style for the block, as the canvas
  shows it.
- Rating shows the range from the server as text, and below it the field for the rating from. An
  unsaved level shows no range.
- The screen takes the names of the idioms from the canvas.

### Tests

- The repository runs against PostgreSQL: an empty read, a save, a second save, the clean bootstrap.
- The model tests cover each rejection of the table rule and the ranges.
- The route tests check 200, 400 for an unknown code and for a rejected table, and the JSON.
- The admin tests cover the repository with the mock engine, and the view model and the mapper with
  stub use cases. The screen capture gets Levels in each state.

### The PRs

This change; the server model and persistence; the use cases, the DTOs and the routes; the admin
data layer; the view model; the screen with the sidebar group; the archive. Each PR is at most 400
lines.

## Risks / Trade-offs

- [The table needs every value before the first save] → The owner fills seven rows once. The canvas
  gives the starting values.
- [Two open changes add migrations] → The versions are fixed now: `V2` for topics, `V3` for levels.
