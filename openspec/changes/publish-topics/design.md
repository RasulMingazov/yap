# Design

## Context

#7 built the topic model, `V2__topics.sql`, the admin API and the Topics and Topic screens. Every
topic is a draft. Topics shows Date and Topic. The Topic toolbar has Save draft only. The server
runs in the Timeweb Moscow region. The admin JSON client does not ignore unknown keys.

## Goals / Non-Goals

**Goals:** the status and Publish; the calendar and To do from one server read; the topic counts.

**Non-Goals:** unpublish; the mobile topic of the day; Try (#15); the sidebar To do badge.

## Decisions

### One published topic per date

Options:

1. A unique index on `release_date` for every topic. The calendar has one row per day. A second
   draft for a date fails. If the database has two drafts on one date, `V5` fails at deploy.
2. A partial unique index on `release_date` where `status = 'PUBLISHED'`. Drafts can share a date.

Choice: 2. It needs no change to the stored drafts, because no topic is published before `V5`. It
keeps the owner's drafts. It matches the example in `docs/testing/003-backend-integration.md`.
PostgreSQL enforces a partial unique index only for the rows that match its predicate [1].

After a unique violation, PostgreSQL aborts the transaction, and Exposed retries a failed
transaction by default. So the update and the publish set `maxAttempts = 1` in their
transaction. The `topics` row is the first write of each; the steps are deleted and inserted after
it. Nothing inside the transaction block catches the exception, so a failed transaction stops at
that first statement and rolls back. The repository catches the `ExposedSQLException` with
SQLState `23505` [2] outside the transaction block and returns a typed `DateTaken`. The update
result has `DateTaken` from [3/26], the part that adds the index. A PostgreSQL test publishes two
drafts of one date at once, with a latch, and checks that exactly one is published and the other
keeps its stored fields.

### `V5__topic_status.sql`

```sql
alter table topics add column status text not null default 'DRAFT'
    check (status in ('DRAFT', 'PUBLISHED'));
create unique index topics_published_release_date on topics (release_date)
    where status = 'PUBLISHED';
```

The column holds the enum names, as the levels and prompts tables do.

V2 to V4 stay as they are. `docs/server/002-persistence.md` says to edit a migration in place
until launch, but then the reset flag clears the database, and `yap_prod` holds the owner's drafts.
A new `V5`, reserved for #8, keeps them. The reset flag does not run, because no applied migration
changes.

### The release day

The day starts at midnight in `Europe/Moscow`. Moscow time is UTC+3 all year, with no daylight
saving, since 26 October 2014 [3]. The learners and the server are in Russia. Options were UTC (the
day changes at 03:00 for the learners) and a setting (no second time zone exists). `feature-topics`
holds the zone as a constant in `model/ReleaseDay.kt` and binds a `java.time.Clock` [4] with
`Clock.systemUTC()`; `moscowToday(clock)` in the same file reads the Moscow day. The `ReleaseState`
enum holds only its values and `of`. Tests bind a fixed clock.

### The model in `feature-topics`

```text
TopicStatus = DRAFT | PUBLISHED                  stored
ReleaseState = DRAFT | PUBLISHED | TODAY | OUT   derived from the status, the date and today
TopicField = TITLE | RELEASE_DATE | DURATION | DESCRIPTION | SETTING | INTERLOCUTOR | ROLE
           | OPENING_LINE | TRANSLATION | STEPS
TopicGap = Missing(field) | IncompleteSteps(numbers) | PastDate | DateTaken
Topic(content, status, topicId)
```

- The use cases normalize the content first: `TopicContent.normalized()` trims every text and
  drops the empty steps. The DTO mapper maps the fields 1:1.
- `TopicDraftRule.gaps(content)` returns the draft gaps. A duration is a gap when it is set and
  below 1: `durationMinutes != null && durationMinutes < 1`. A zero duration stays
  `Missing(DURATION)`. `TopicPublishRule.gaps(content, stored,
  today)` adds the publish gaps. `Missing(STEPS)` means no step; `IncompleteSteps` holds the
  1-based numbers of the steps that miss a field. `PastDate` comes for a draft with a date before
  today, and for a published topic whose new date is before today; a stored past date of a
  published topic stays valid. A use case accepts content when the set is empty. The names follow
  the rule guide in `docs/server/001-structure.md`.
- `DateTaken` is not a rule gap: the use case asks the repository
  (`hasPublishedOn(date, exceptTopicId)`), and `TopicDetailsRule` adds `DateTaken` to the
  details of a draft when another published topic has the same date. The rule stays pure: it
  takes `isDateTaken` and `today` from the use case. The note shows it before the click. A write that still meets the index answers 409.
- Create makes a draft. Update reads the topic, applies the rule of its status, and writes
  `where topic_id = ? and status = <the status it read>`. No row written for a topic that exists
  means a publish came between the read and the write. The status only moves from draft to
  published, so the use case runs once more with the publish rule, and that run cannot mismatch.
- `PublishTopicUseCase` applies the publish rule, writes the content and the status in one
  transaction, and returns `Published`, `Invalid(gaps)`, `NotFound` or `DateTaken`. A publish of a
  published topic is its save: the same rule, the same write, and the status stays.
- `TopicCalendar` builds the rows and To do from the topics, the character names, and today. The
  range is today and the 13 days after it. A draft whose date has a published topic is not in To
  do: its Publish would answer 409. `GetTopicCalendarUseCase` reads the topics, the characters and
  the clock.
- Each topic answer carries the release state and the publish gaps, so the Topic screen shows them
  without a second rule in the client.

### The topic counts

`feature-characters` cannot read `topics`: `feature-topics` depends on it. Options:

1. A port `CharacterTopicCounts` in `feature-characters`, implemented by `feature-topics` with one
   `group by character_id` query and bound in `featureTopicsModule()`.
2. The admin app counts the topics of each character from the calendar. That is a derived number
   in the client, against the thin client rule.
3. A SQL subquery on `topics` in the characters repository. It couples the characters module to a
   table that it does not own.

Choice: 1. Only `GET /admin/characters` carries the count, as `CharacterSummaryDto(characterId,
profile, topicCount)`. One character, create and update keep `CharacterDto`. Both consumers, the
Characters list and the interlocutor choice, read the list.

- `GetCharacterSummariesUseCase` serves the route and calls the port. `GetCharactersUseCase` stays
  without counts, so the calendar reads the names with no count query.
- `docs/server/001-structure.md` allows a feature to use another feature only through its use
  cases and model types. Part [5/26] adds the port rule there: the feature that needs the data owns
  the interface in its `usecase/`; the feature that holds the data implements it and binds it in
  its Koin module; the dependency between the Gradle modules keeps its direction.

### The DTOs

```text
TopicDto(content, gaps: List<TopicGapDto>, status: TopicStatusDto, topicId)
TopicStatusDto = DRAFT | PUBLISHED | TODAY | OUT
TopicGapDto = missing(field: TopicFieldDto) | incompleteSteps(numbers) | pastDate | dateTaken
TopicRejectionDto(gaps: List<TopicGapDto>)                    the body of every 400 of a topic write
TopicCalendarDto(rows: List<CalendarRowDto>, todos: List<TodoDto>)
CalendarRowDto(date, topic: CalendarTopicDto?)
CalendarTopicDto(completeSteps, ledBy: LedByDto?, status, steps, title, topicId)
LedByDto = character(name) | guest
TodoDto = emptyDay(date) | draft(date, gaps, title, topicId)
```

The admin app maps each field and status to its Russian copy and tone. It derives no rule.

### The routes

- `GET /admin/topics/calendar` replaces `GET /admin/topics`. The old route, its use case and the
  admin `list` go away in the part that moves Topics to the calendar, so `develop` keeps a working
  Topics screen between the merges.
- `POST /admin/topics/{topicId}/publish` takes `TopicContentDto`: Publish saves the form and
  publishes it in one call. Options were a call without a body, which publishes the stored version
  and drops unsaved edits, and two calls, which can fail between them.
- 400 carries `TopicRejectionDto`. 409 is a taken date. 404 is an unknown topic.

### The admin app

- `TopicsViewModel` loads the calendar on `Event.Opened` and keeps it on a failed reload, as now.
  `TopicsUiStateMapper` builds the To do entries and the rows; its only public function is `invoke`.
  The shapes of the view models follow «Readability rules» below.
- Create passes its date: `Screen.Topic(topicId, releaseDate)` in the navigation, and the
  `TopicViewModel` factory takes the date. `Main.kt` resolves the view models as now.
- `TopicViewModel` gets `PublishClicked`, `PublishConfirmed`, `SaveConfirmed` and
  `QuestionDismissed`. One status slot shows, in this order: Saving… or Publishing…, the result of
  the last action (Saved, a rejection, a taken date), then the note from the last server answer.
- The questions (Publish, and the move of the date of a Published or Today topic) are a panel
  inside the screen below the toolbar, not a window: `PublishQuestion` in `T/presentation/ui/`.
  The view model holds which question is open; the mapper builds its text. Comparing the form
  date with the stored date is a render choice on the status that the server sent, not a rule.
- After a field edit, the note and a rejection error stay with a lead that they are about the
  version that the server saw. Options were to hide them until the next answer, but the error of a
  rejected draft already stays until the next click (#7), so both keep the same behaviour.
- A rejection error has three parts: what happened, that the changes are not saved, the next step.
  Save draft builds its 400 text from the gaps too.
- A failed reload of the calendar shows a banner with Retry above the kept calendar.
- The copy agrees with the feminine «тема»: «Опубликована», «Вышла», «· опубликована». A Today
  note says that users see edits at once.
- Publish is the primary button and hides for a published topic. Save draft keeps the secondary
  look. The `button` slot of `ToolbarSaveAction` holds both buttons, so the status stays on the
  left of both.
- `AdminTheme.Colors` gets `warn = #9A5200` from the canvas, and `Status.Tone` gets `WARNING`.
- `TopicCountText` in `feature/characters/presentation` picks the Russian form: 1 тема, 2 темы,
  5 тем. `feature/topics` already depends on `feature/characters`.

### Readability rules

The owner approved the readability audit of 2026-11-08. The open parts follow it. The behaviour
does not change.

- `DataState` holds raw facts only (`docs/admin/001-view-models.md`), as `CharactersViewModel`
  does. The mapper decides every screen state.

  ```text
  TopicsViewModel.DataState(calendar: TopicCalendar?, isLoadFailed, isLoading)
  TopicViewModel.DataState(cast: List<CharacterSummary>?, form: TopicContent, guestDescription,
      isLoadFailed, isLoading, isSaving, saveResult: SaveTopicResult?, topic: Topic?)
  ```

  `cast` has the list type that [10/26] leaves. `isLoading` is the busy flag of the load, so no
  `null` means Loading. The view model takes the identifier of the topic to load from its
  constructor, and the save reads `topic?.topicId`; `DataState` holds no copy of it.

  No `Load` enum, no `SaveStatus`, no stored `LoadTopicsResult`, no `loaded()`, `saved()` or
  `withForm()`. The view model sets the facts in named private functions. The private function
  `SaveTopicResult?.keptAfterEdit()` says that a rejected save stays until the next click and that
  Saved clears at an edit. [21/26] slice 3 adds `question`, `isPublishing` and `publishResult`;
  [22/26] adds `isEditedAfterAnswer`.
- The Topic form sends one `Event` per field: `TitleChanged`, `DescriptionChanged`,
  `SettingChanged`, `RoleChanged`, `OpeningEnglishChanged`, `OpeningTranslationChanged`,
  `StepTaskChanged`, `StepPassWhenChanged`, `StepOutcomeChanged`, beside `DurationChanged`,
  `ReleaseDatePicked`, `InterlocutorChosen`, `GuestDescriptionChanged` and `AddStepClicked`. The
  view model does the `copy`. `UiState.Content` carries `Fields` with the texts, not the domain
  `TopicContent`. The Character screen follows in the tech PR
  «admin: one event per field on the Character screen».
- The admin loads return the shared `LoadResult<T>`: `LoadResult<TopicCalendar>` and
  `LoadResult<Topic>`. `LoadTopicResult` and `LoadTopicsResult` go away.
- The server DTO mappers map the fields 1:1. Normalizing is a rule in `model/`
  (`TopicContent.normalized()`), and the use cases apply it before the other rules.
  `docs/server/001-structure.md` changes its mapper rule in [8/26] slice 1. The character, level
  and prompt mappers still trim; they follow in the tech PR
  «server: map DTOs 1:1 in the character, level and prompt mappers».
- «Today in Moscow» is its own function, `moscowToday(clock)`, not a member of the `ReleaseState`
  enum.
- A zero duration stays `Missing(DURATION)`. A separate gap for it would change the spec and is
  not part of this change.

### Owner decisions to check

- One published topic per date; drafts can share a date.
- The day of release is the Moscow day.
- A published topic stays editable. Its save needs every field, and the button says Save.
- To do is in date order, with the empty days and the drafts mixed. The canvas shows the empty day
  first, but the nearest work first finds the gaps early.
- The calendar shows 14 days ahead, and To do lists drafts from today on. A past draft stays in All
  topics only.
- Publish is disabled for a new topic: a topic gets its identifier at the first save.
- A publish of a published topic acts as its save. Options were 409, which makes the API reject a
  call that changes nothing, and 400.
- A draft on a date that has a published topic is not in To do. All topics still shows it.
- The topic count counts drafts too.

### Deliberate differences from the canvas

- The sidebar shows no To do badge next to Topics.
- The note of a ready draft says publish only, not try and publish: Try comes with #15.
- To do mixes empty days and drafts in date order.
- A published topic shows Save, not Save draft, and hides Publish instead of a disabled Published.
- The copy uses the feminine forms: «Опубликована», «Вышла»; the canvas has «Опубликован», «Вышел».
- Publish and a move of the date of a published topic ask first, in a panel inside the screen.
- The one word for the interlocutor («Ведёт», «Собеседник», «Персонаж») waits for the owner (m5).

## Risks / Trade-offs

- [The admin JSON client fails on an unknown key] → `GET /admin/characters` changes its DTO and its
  admin decoding in the same part. New fields of `TopicDto` come with the admin test fixtures that
  build it.
- [A deploy between the server and the admin parts] → the old list route stays until the admin
  moves to the calendar. Releases go from `develop` to `main` only after the last part.
- [Two publishes of one date at once] → the partial unique index; a PostgreSQL test with a latch.
- [The clock in tests] → every time rule takes `today` or a `Clock`; no test reads the system time.

## Migration Plan

`V5__topic_status.sql` is new. #11 takes `V6` when it is planned at the same time. Existing topics
become drafts. A rollback is a revert of the parts and a drop of the column and the index.

## Sources

1. PostgreSQL, Partial Indexes: https://www.postgresql.org/docs/current/indexes-partial.html
2. PostgreSQL, Error Codes (`23505 unique_violation`):
   https://www.postgresql.org/docs/current/errcodes-appendix.html
3. Moscow Time, UTC+3 without DST since 26 October 2014: https://en.wikipedia.org/wiki/Moscow_Time
4. Java SE 21, `java.time.Clock`:
   https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html
