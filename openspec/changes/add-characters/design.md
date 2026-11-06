# Design

## Context

The server has `app` and `core-database`: a HikariCP pool and Flyway migrations, with no feature
tables and no Exposed. The admin app has one screen, Server, which saves the server address and
the admin key. `docs/server/001-structure.md` and `docs/server/002-persistence.md` set the module
and persistence rules this change follows.

## Goals / Non-Goals

**Goals:**

- The first feature module and the first feature table, as examples for the next features.
- One admin screen layout with a sidebar, so later screens only add an item.

**Non-Goals:**

- A navigation library in the admin app. Three screens do not need one.
- Concurrent edits of one character. The admin app has one owner.

## Decisions

### `server/feature-characters`

The module has `model/` (the character and the profile rule), `usecase/` (one class per scenario),
`persistence/` (the repository, the table and the migration) and `api/` (the routes and the DTO
mappers). `app` builds the repository and the use cases and mounts the routes behind the admin key.

### Exposed in `core-database`

`core-database` connects Exposed 1.6.0 to the existing pool. It gives features one suspending
transaction that runs `suspendTransaction` on `Dispatchers.IO`, because JDBC blocks the thread.
`openDatabase` returns this database object, and `app` closes it at stop as it closes the pool now.

Alternative: plain JDBC in each repository. Rejected: the persistence guide names Exposed.

### The table and the identifier

```sql
create table characters (
    character_id uuid primary key,
    name text not null,
    history text not null,
    personality text not null,
    manner text not null
);
```

The server makes the identifier with `UUID.randomUUID()` before the insert, so the use case knows
it without a second query. The list query orders by `lower(name)`. The migration is
`V1__characters.sql`, the first version of the global Flyway sequence.

### The profile rule

One function in `model/` trims the four fields and returns no profile when a field is empty. The
create and the update use cases both call it and return `Invalid` for no profile. The routes map
`Invalid` to 400 and an unknown character to 404.

### The wire contract

`CharacterProfileDto(name, history, personality, manner)` is the body of the create and the update
calls. `CharacterDto(characterId, profile)` is what the server returns. The list is a JSON array of
`CharacterDto`. A row of Characters needs the name and the personality, and the list is short, so a
separate list item type has no use yet.

### The admin app

- A shell composable holds the sidebar and the current screen: Characters, Character with an
  identifier or none for a new character, or Server. The shell picks the start screen from the
  saved server settings.
- `CharactersViewModel` loads the list. `CharacterViewModel` loads one character, keeps the four
  fields as the owner types them and saves them.
- The admin characters repository reads the saved address and key for each call. It maps 400 to
  `Invalid`, 404 to `NotFound`, and every other failure to `Failed`.
- The error that asks to fill in every field stays in the UI state until the next save. The failure
  of Save is a snackbar, because the owner can only try again.
- The Russian copy comes from the admin page of the design canvas. The copy that the design does
  not show, such as the errors and the empty list, is chosen in the code.

### Tests

- The repository runs against PostgreSQL with Testcontainers: create, read, update, the name order
  and the clean bootstrap. Locally without Docker the suite is skipped; CI runs it.
- The use-case tests stub the repository: the trimmed profile, the rejected profile and the unknown
  character.
- The route tests use `testApplication` with the real use cases on a stub repository: the status
  codes and the JSON.
- The admin tests cover the repository with the Ktor mock engine, and the view models and the UI
  state mappers with stub use cases.

### The PRs

The change lands in seven PRs for #6: this change; the server persistence; the server use cases;
the server API; the admin data layer; the sidebar and Characters; the Character form and the archive.

## Risks / Trade-offs

- [No Docker on the owner's machine] → The PostgreSQL suite runs in CI, which fails when Docker is
  missing there. The PR says that the suite did not run locally.
- [`V1__characters.sql` changes before the launch] → `DATABASE_RESET_ON_MIGRATION_CHANGE` rebuilds
  the database on deploy, as the persistence guide says.

## Migration Plan

The deploy applies `V1__characters.sql` at start. A rollback is a revert: the reset flag rebuilds
the database from the remaining migrations.
