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
2. A partial unique index on `release_date` where `status = 'published'`. Drafts can share a date.

Choice: 2. It needs no change to the stored drafts, because no topic is published before `V5`. It
keeps the owner's drafts. It matches the example in `docs/testing/003-backend-integration.md`.
PostgreSQL enforces a partial unique index only for the rows that match its predicate [1]. The
repository maps the unique violation `23505` [2] to a typed `DateTaken` result inside the
transaction. A PostgreSQL test publishes two drafts of one date at once, with a latch.

### `V5__topic_status.sql`

```sql
alter table topics add column status text not null default 'draft'
    check (status in ('draft', 'published'));
create unique index topics_published_release_date on topics (release_date)
    where status = 'published';
```

V2 to V4 stay as they are. The reset flag does not run, because no applied migration changes.

### The release day

The day starts at midnight in `Europe/Moscow`. Moscow time is UTC+3 all year, with no daylight
saving, since 26 October 2014 [3]. The learners and the server are in Russia. Options were UTC (the
day changes at 03:00 for the learners) and a setting (no second time zone exists). `feature-topics`
holds the zone as a constant beside the release rule and binds a `java.time.Clock` [4] with
`Clock.system(zone)` in its Koin module. Tests bind a fixed clock.

### The model in `feature-topics`

```text
TopicStatus = DRAFT | PUBLISHED                  stored
ReleaseState = DRAFT | PUBLISHED | TODAY | OUT   derived from the status, the date and today
TopicField = TITLE | RELEASE_DATE | DURATION | DESCRIPTION | SETTING | INTERLOCUTOR | ROLE
           | OPENING_LINE | TRANSLATION | STEPS
Topic(content, status, topicId)
```

- `TopicDraftRule.gaps(content)` returns the draft fields that fail. `TopicPublishRule.gaps(content)`
  adds the publish fields. A use case accepts content when the set is empty. The names follow the
  rule guide in `docs/server/001-structure.md`.
- Create makes a draft. Update reads the topic, applies the rule of its status, and keeps the
  status. `PublishTopicUseCase` applies the publish rule, writes the content and the status in one
  transaction, and returns `Published`, `Invalid(gaps)`, `NotFound` or `DateTaken`.
- `TopicCalendar` builds the rows and To do from the topics, the character names, and today. The
  range is today and the 13 days after it. `GetTopicCalendarUseCase` reads the topics, the
  characters and the clock.
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

### The DTOs

```text
TopicDto(content, gaps: List<TopicFieldDto>, status: TopicStatusDto, topicId)
TopicStatusDto = DRAFT | PUBLISHED | TODAY | OUT
TopicRejectionDto(gaps: List<TopicFieldDto>)                  the body of every 400 of a topic write
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
- Create passes its date: `Screen.Topic(topicId, releaseDate)` in the navigation, and the
  `TopicViewModel` factory takes the date. `Main.kt` resolves the view models as now.
- `TopicViewModel` gets `PublishClicked`. One status slot shows, in this order: Saving… or
  Publishing…, the result of the last action (Saved, a rejection, a taken date), then the note from
  the last server answer.
- Publish is the primary button. Save draft keeps the secondary look. The `button` slot of
  `ToolbarSaveAction` holds both buttons, so the status stays on the left of both.
- `AdminTheme.Colors` gets `warn = #9A5200` from the canvas, and `Status.Tone` gets `WARNING`.
- `TopicCountText` in `feature/characters/presentation` picks the Russian form: 1 тема, 2 темы,
  5 тем. `feature/topics` already depends on `feature/characters`.

### Owner decisions to check

- One published topic per date; drafts can share a date.
- The day of release is the Moscow day.
- A published topic stays editable. Its save needs every field, and the button says Save.
- To do is in date order, with the empty days and the drafts mixed. The canvas shows the empty day
  first, but the nearest work first finds the gaps early.
- The calendar shows 14 days ahead, and To do lists drafts from today on. A past draft stays in All
  topics only.
- Publish is disabled for a new topic: a topic gets its identifier at the first save.
- The topic count counts drafts too.

### Deliberate differences from the canvas

- The sidebar shows no To do badge next to Topics.
- The note of a ready draft says publish only, not try and publish: Try comes with #15.
- The note of a draft names the missing fields, not the numbers of the steps.
- To do mixes empty days and drafts in date order.
- A published topic shows Save, not Save draft.

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
