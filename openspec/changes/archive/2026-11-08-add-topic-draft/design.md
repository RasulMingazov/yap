# Design

## Context

`feature-characters` and the admin `feature/characters` are the examples. Goal: the topic model
that #8, #12 and #15 use with no second migration. Not here: the status and one topic per date.

## Decisions

### `server/feature-topics`

It calls `GetCharacterUseCase` of `feature-characters` to check the interlocutor, with no cycle.
A foreign key alone was rejected: the use case returns a typed `Invalid` and catches no database
error. The foreign key stays for integrity.

### The model and the tables

```text
TopicContent(title, releaseDate: LocalDate?, durationMinutes: Int?, description, setting,
             interlocutor: Interlocutor?, role, openingLine: OpeningLine, steps: List<Step>)
Interlocutor = Cast(characterId) | Guest(description)
```

`TopicContent.isDraftReady` is the draft rule: a title, a date and no duration under 1. The server
DTO mapper trims every text and drops the empty steps, so the model holds no trim function. The
create and the update use cases check the rule, then the character, and return `Invalid` for either
failure. Both apps use `java.time.LocalDate`.

`V2__topics.sql` adds `topics` (one column per field; `character_id` references `characters`;
`guest` is text; a check allows at most one of the two) and `topic_steps` (`topic_id`,
`position`, `task`, `pass_when`, `outcome`; cascade on delete). A guest with an empty description is
`guest = ''`. An update writes the row, deletes the steps and inserts the new ones in one
transaction. The list orders by `release_date`, then by `lower(title)`.

`TopicContentDto` is the body of the create and the update calls. The mapper trims every text and
drops a step whose texts are all empty. The date is an ISO string or
null; a date that does not parse is a 400. `InterlocutorDto` is sealed: `character`
(`characterId`) or `guest` (`description`). The server returns `TopicDto(topicId, content)`, and
the list is an array of it. The full topic in each row is fine before launch; #8 adds a row type.

### The admin app

- `feature/topics` mirrors `feature/characters`. `TopicsViewModel` lives for the session.
  `TopicViewModel` loads the topic and the cast (the characters `GetCharactersUseCase`) together.
- The client reads no rule. It sends an empty date or duration as null. Every 400 shows one error
  beside Save draft, which names the four fields the server checks.
- Release date is a popup button, as in the canvas. It opens the Material3 `DatePickerDialog`.
  The code formats the popup text in Russian. `ReleaseDateField` is in `feature/topics`.
- The interlocutor choice is `CastRadioRow` in `feature/topics`, not in the hot `core/design/`:
  a radio, the name and the meta on the right, as in the canvas.
- `ScreenToolbar` gets `actions: @Composable RowScope.() -> Unit = {}` after the title. Topics puts
  New topic there, Topic puts the status and Save draft. #9 and #10 put their Save there.

### Owner decisions

- The server drops an empty step: the issue has no delete, so an extra Add step leaves no trace.
- Topics comes before Characters, and the app opens on it: the topic is the daily work.
- Save draft is in the toolbar, as the canvas shows it for the topic.
- The form takes the full width until Try comes with #15.
- New topic is built here. #8 adds only Publish. The topic counts move to #8 with Led by.

### Deliberate differences from the canvas

- Topics shows only the columns Date and Topic. To do, empty days, Led by, Steps, Status and the
  note about publishing are the calendar of #8.
- The character rows show no topic count: #8 owns the counts. Guest shows its canvas note.
- The toolbar has no Publish (#8). Save draft keeps the secondary look of the canvas.
- The step cells are text fields in the canvas columns: the canvas draws filled cells, and the
  owner must edit them.

## Migration Plan

The parts merge in the order `V2` (#7, part [4/16]), `V3` (#9), `V4` (#10). The deploy applies
`V2__topics.sql` at start. A rollback is a revert: the reset flag rebuilds the database.
