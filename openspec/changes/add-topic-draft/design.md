# Design

## Context

`server/feature-characters` is the first feature module and the example for this one: `model/`,
`usecase/`, `persistence/` and `api/`, wired by hand in `app`. The admin app has the shell with
the sidebar, the Characters list and the Character form. Neither side has a date type yet.

## Goals / Non-Goals

**Goals:**

- The topic model that #8 publishes, #12 plays and #15 tries, without a second migration of its
  shape.

**Non-Goals:**

- A status column. All topics are drafts until #8 adds the status.
- A uniqueness rule for the date. #8 adds one topic per date.

## Decisions

### `server/feature-topics`

The module has the same four packages as `feature-characters`. It depends on
`feature-characters` and calls `GetCharacterUseCase` to check the interlocutor. The characters
module does not depend on topics, so there is no cycle.

Alternative: a foreign key only. Rejected as the only check: the use case returns a typed
`Invalid` without catching a database error. The foreign key stays for integrity.

### The model

```text
Topic(topicId, content)
TopicContent(title, releaseDate: LocalDate?, durationMinutes: Int?, description, setting,
             interlocutor: Interlocutor?, role, openingLine: OpeningLine, steps: List<Step>)
Interlocutor = Cast(characterId) | Guest(description)
OpeningLine(english, russian)
Step(task, passWhen, outcome)
```

One rule in `model/` trims every text, drops a step whose three fields are empty, and returns no
content when the title is empty, the date is missing or the duration is not positive. The create
and the update use cases both call it. Then they check the character. Each returns `Invalid` for
no content or an unknown character. The dates use `java.time.LocalDate`: both apps run on the JVM.

### The tables

```sql
create table topics (
    topic_id uuid primary key,
    title text not null,
    release_date date not null,
    duration_minutes integer,
    description text not null,
    setting text not null,
    character_id uuid references characters (character_id),
    guest text,
    role text not null,
    opening_line text not null,
    opening_line_russian text not null,
    check (character_id is null or guest is null)
);

create table topic_steps (
    topic_id uuid not null references topics (topic_id) on delete cascade,
    position integer not null,
    task text not null,
    pass_when text not null,
    outcome text not null,
    primary key (topic_id, position)
);
```

An update writes the topic row, deletes its steps and inserts the new ones in one transaction. The
list orders by `release_date`, then by `lower(title)`. The migration is `V2__topics.sql`.

### The wire contract

- `TopicContentDto` is the body of the create and the update calls. The date is an ISO string
  (`2026-10-24`) or null. A date that does not parse ends in `BadRequestException`, so 400.
- `InterlocutorDto` is a sealed class with the types `character` (`characterId`) and `guest`
  (`description`).
- `TopicDto(topicId, content)` is what the server returns. The list is a JSON array of `TopicDto`,
  as for characters. #8 adds a row type when the calendar needs the name of the interlocutor.

### The admin app

- `feature/topics` mirrors `feature/characters`: a remote repository that reads the saved server
  settings for each call, use cases, and two view models.
- `TopicsViewModel` lives for the session and keeps the shown list on a return, as
  `CharactersViewModel` does.
- `TopicViewModel` loads the topic and the cast together. Either failure shows the load error. The
  form keeps the date and the duration as the owner types them. Save draft reads the date as day,
  month and year, such as `24.10.2026`, and the duration as digits. A field that does not read
  gives an inline error and no call. A 400 gives the error that asks for the title and the date.
- The interlocutor choice lists the cast in the order of the server, then Guest. The admin feature
  calls the characters `GetCharactersUseCase`.
- `ScreenToolbar` gets an actions slot for Save draft and for New topic, as the canvas shows them.
- The steps are rows of three text fields with the step number. Add step appends an empty row. A
  new topic starts with one empty row.
- The Russian copy comes from the admin page of the canvas. The copy that the canvas does not show
  is chosen in the code.

### Tests

- The repository runs against PostgreSQL: create, read, update with fewer steps, the order of the
  steps, the date order of the list, the guest and the clean bootstrap.
- The model test covers the trim, the empty step, no title, no date and a zero duration.
- The use-case tests stub the repository and the characters repository: the unknown character, the
  rejected content and the unknown topic.
- The route tests check 200, 201, 400 for a bad date and an empty title, and 404.
- The admin tests cover the repository with the mock engine, and the view models and the mappers
  with stub use cases. The screen capture gets Topics and Topic in each state.

### The PRs

This change; the server model and persistence; the use cases; the DTOs and the routes; the admin
data layer; Topics with the sidebar change; the Topic view model; the Topic screen; the archive.
Each PR is at most 400 lines and stacks on the one before it until that one merges.

## Risks / Trade-offs

- [A free date field accepts typos] → The admin app reads the date before the call and names the
  field in the error. A date picker can come with the calendar of #8.
- [The full topic in the list grows with each day] → The list is small before launch. #8 adds a row
  type.

## Migration Plan

The deploy applies `V2__topics.sql` at start. A rollback is a revert: the reset flag rebuilds the
database from the remaining migrations.
