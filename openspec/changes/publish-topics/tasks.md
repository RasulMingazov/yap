# Tasks

One group is one PR, in merge order: its size, its files (hot first). `S` is
`server/feature-topics/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/topics`, `C` is
`server/feature-characters/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/characters`,
`K` is `contract-admin/src/main/kotlin/app/yap/contract/admin`, `A` is
`admin/src/{main,test}/kotlin/app/yap/admin`, `H` is `A/feature/characters`, `T` is
`A/feature/topics`. A task marked «Owner check» needs a server with a database; the owner runs it.

An admin part adds at most 300 lines, tests included. The UI state of a screen comes in a part
before the screen. The captures come in their own part.

A group with slices goes out as one PR per slice, in slice order, titled
`Issue #8 [<n>/26] slice <k>: ...`. The numbers of the groups stay as they are.

The readability audit of 2026-11-08 (owner-approved) adds rules to the open parts: `DataState`
holds raw facts only, one `Event` per field of the Topic form, the shared `LoadResult<T>`, DTO
mappers that map 1:1, and the Moscow day in its own function. `design.md` («Readability rules»)
names the shapes.

## 1. [1/26] The plan · this PR

- [ ] 1.1 Write the change; verify with `openspec validate publish-topics --strict`

## 2. [2/26] The publish rule and the release state · ~300 · `S/model/` (`TopicField`, `TopicGap`, `TopicStatus`, `ReleaseState`, `TopicDraftRule`, `TopicPublishRule`, the Moscow zone), `S/usecase/` (`CreateTopicUseCase`, `UpdateTopicUseCase`)

- [x] 2.1 Make `TopicDraftRule` return the gaps, and add `TopicPublishRule` with `Missing`, `IncompleteSteps` (1-based numbers) and `PastDate`; the use cases accept an empty set; verify with the rule tests for each field, no steps, the steps 3 and 5 without an outcome, a guest without a description, an unknown character, a draft with a past date, a published topic that keeps its past date, a published topic moved into the past, and the unchanged use case tests
- [x] 2.2 Add `TopicStatus` and `ReleaseState` from the status, the date and today; verify with the tests for Out, Today, Published, a draft of today, and 22:30 UTC on 23 October as 24 October in Moscow

## 3. [3/26] The status in the database · ~250 · `server/feature-topics/src/main/resources/db/migration/V5__topic_status.sql`, `S/model/` (`Topic`, `StubTopic`, `UpdateTopicOutcome`), `S/persistence/` (`TopicWrite`, `TopicsTable`, `TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`), `S/usecase/UpdateTopicUseCase.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only), `server/core-database/src/testFixtures/kotlin/app/yap/server/core/database/TestDatabase.kt` (`withRestartableDatabase`)

- [x] 3.1 Add `V5__topic_status.sql`, the status in `Topic` and the table, and an update by identifier and content that writes `where status = <the status it read>` and keeps it; its result is a `TopicWrite` (`Written`, `NotFound`, `StaleStatus` or `DateTaken`) from the start, and `UpdateTopicUseCase` maps it to `UpdateTopicOutcome`: the transaction sets `maxAttempts = 1`, the `topics` row is its first write, nothing inside the block catches the exception, and the repository maps the `ExposedSQLException` with SQLState `23505` to `DateTaken` outside the block; verify with the PostgreSQL tests for a new draft and a restart, two drafts of one date, an update that keeps the status, an update with a stale status (writes nothing), and the clean bootstrap (CI runs them)

## 4. [4/26] Publish in the database · ~240 · `S/persistence/` (`TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`) and the PostgreSQL test

- [x] 4.1 Add `publish` with the same write rules as the update of [3/26]: `maxAttempts = 1`, the `topics` row as the first write, no catch inside the block, and `23505` mapped to `DateTaken` outside it; verify with the PostgreSQL tests for a publish and a restart, a second published topic on a date (stores nothing), a save of a published topic onto a taken date, and two publishes at once with a latch (one published, the other unchanged)
- [x] 4.2 Add `hasPublishedOn(date, exceptTopicId)`; verify with the PostgreSQL tests for a date with another published topic, with only the topic itself, and with drafts only

## 5. [5/26] The topic counts on the server · ~310 · `K/CharacterSummaryDto.kt`, `docs/server/001-structure.md` (the port rule), `C/model/CharacterSummary.kt`, `C/usecase/` (`CharacterTopicCounts` port, `GetCharacterSummariesUseCase`), `C/api/` (mapper, routes), `C/testFixtures` (stub port), `C/FeatureCharactersModule.kt`, `S/persistence/DatabaseCharacterTopicCounts.kt` and its PostgreSQL test, `S/FeatureTopicsModule.kt`, `server/app/src/main/kotlin/app/yap/server/Application.kt`, `server/app/src/test/kotlin/app/yap/server/StubAppModules.kt`, `H/data/` (decode `CharacterSummaryDto`, keep the domain)

- [x] 5.1 Add the port, its query in `feature-topics`, `GetCharacterSummariesUseCase` and the counts in `GET /admin/characters`; `GetCharactersUseCase` stays without counts; verify with the PostgreSQL test for drafts and published topics and a character without topics, the route test for the list with counts, and `AppModulesTest`
- [x] 5.2 Add the port rule to `docs/server/001-structure.md`; verify that the guide names where the interface lives and who binds it
- [x] 5.3 Decode the summary in `RemoteCharacterRepository` in the same PR, so the admin list keeps working; verify with the mock engine test for the list

## 6. [6/26] The calendar on the server · ~340 · `S/model/` (`TopicCalendar`, its rows, `LedBy`, `Todo`), `S/usecase/GetTopicCalendarUseCase.kt`, `S/FeatureTopicsModule.kt` (the `Clock`)

- [x] 6.1 Add `TopicCalendar` with the 14 days from today, the rows, the complete steps and To do with the gaps of each draft; verify with the model tests for the scenarios The rows, No topics yet, To do, A draft on a date with a published topic, The steps of a row, and a draft with the steps 3–5 incomplete, the title order on one date, and both interlocutors
- [x] 6.2 Add the use case with the clock and `GetCharactersUseCase` for the names; verify with a stub test with a fixed clock and the character names

## 7. [7/26] Publish and save · ~340 · `S/usecase/` (`PublishTopicUseCase`, `UpdateTopicUseCase`, `CreateTopicUseCase`, `GetTopicUseCase`), `S/model/` (the outcomes, `TopicDetails`), `S/FeatureTopicsModule.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only)

- [x] 7.1 Add `PublishTopicUseCase` and the rule of the status in `UpdateTopicUseCase`, with the stored topic and today for `PastDate`; the outcomes carry the gaps and `DateTaken`; a stale status runs the update once more with the publish rule; move the `date` extension of `TopicCalendarRule` into `Topic` as an `internal` member, with its `requireNotNull`, because `publishedDate` reads the date; verify with the stub tests for a publish, the gaps, a past date, an unknown topic, a taken date, a publish of a published topic, a save of a published topic with an empty setting (stores nothing), a save of a draft that stays a draft, a save of a published topic that is out with its date, and a stale status
- [x] 7.2 Return `TopicDetails` (the topic, the release state, the publish gaps, and `DateTaken` for a draft whose date has another published topic) from get, create, update and publish; verify with the stub tests with a fixed clock for a ready draft, a taken date and a published topic

## 8. [8/26] The topic DTOs · three slices

### Slice 1 · The input rule and the Moscow day · ~160 · `docs/server/001-structure.md` (the mapper rule), `S/api/TopicDtoMapper.kt` (`toModel` 1:1), `S/model/` (`TopicContent.normalized()`, `TopicDraftRule`, `ReleaseState`, `ReleaseDay.kt` with the Moscow zone and `moscowToday(clock)`), `S/usecase/` (`CreateTopicUseCase`, `GetTopicUseCase`, `UpdateTopicUseCase`, `PublishTopicUseCase`, `GetTopicCalendarUseCase`), their tests, `TopicContentTest`, `ReleaseDayTest`, `TopicDetailsRuleTest`, `StubTopic.stubPaddedContent()`

It merges after the PR of 7.2 (`TopicDetails`, merged), whose use cases read `ReleaseState.today` too.

- [x] 8.1 Make `TopicContentDto.toModel()` map each field 1:1 (no `trim()`, no step filter); add `TopicContent.normalized()` in `S/model/` (trims every text, the opening line, the steps and the guest description, then drops the empty steps); `CreateTopicUseCase`, `UpdateTopicUseCase` (also its second run) and `PublishTopicUseCase` call it before the rule; change the mapper rule in `docs/server/001-structure.md` to «the DTO mapper maps 1:1; a rule in `model/` normalizes the text input and the use case applies it first; the character, level and prompt mappers follow in the tech PR «server: map DTOs 1:1 in the character, level and prompt mappers»»; verify with the mapper tests (padded texts stay padded, an empty step stays), the `TopicContent` tests moved from the mapper tests (spaces, an empty step after a filled one, a guest of spaces), and a use case test each for create, update and publish that stores a padded title trimmed and drops an empty step
- [x] 8.2 Write `TopicDraftRule` with `durationMinutes != null && durationMinutes < 1`; a zero duration stays `Missing(DURATION)`; verify with the unchanged tests `GIVEN a duration of 0 minutes WHEN checking THEN the duration is missing` and `GIVEN only a title and a date WHEN checking THEN there are no gaps` (no duration)
- [x] 8.3 Move `today(clock)` out of the `ReleaseState` enum into `moscowToday(clock)` in `S/model/ReleaseDay.kt` with the `Europe/Moscow` constant; `ReleaseState` keeps only the values and `of`; `CreateTopicUseCase`, `GetTopicUseCase`, `UpdateTopicUseCase`, `PublishTopicUseCase`, `GetTopicCalendarUseCase` and `TopicDetailsRuleTest` call it (`TopicDetailsRule` takes `today` as a parameter and stays); verify with the 22:30 UTC on 23 October test moved to `ReleaseDayTest` and the unchanged `ReleaseState` tests

### Slice 2 · The topic DTOs · ~330 · `K/` (`TopicDto`, `TopicStatusDto`, `TopicFieldDto`, `TopicGapDto`), `S/api/` (`TopicDtoMapper`, `TopicRoutes`: compile only), `S/usecase/GetTopicsUseCase.kt` (it returns `TopicDetails`, because `TopicDto` carries the state and the gaps), their tests, `T/` tests that build `TopicDto`

- [x] 8.4 Add the topic DTOs and the server mapper of `TopicDetails`; the mapper maps the fields 1:1 and decides nothing; verify with the mapper tests for each status, each field, each gap and both interlocutors, and a test of `GetTopicsUseCase` with a taken date
- [x] 8.5 Fill the new `TopicDto` fields in the admin tests; verify with `./gradlew build`

### Slice 3 · The calendar DTOs · ~250 · `K/` (`TopicCalendarDto`, `CalendarRowDto`, `CalendarTopicDto`, `LedByDto`, `TodoDto`), `S/api/TopicCalendarDtoMapper.kt` and its test, `S/model/StubTopic.kt` (`stubCalendar`, `stubCalendarTopic`), `S/api/TopicDtoMapper.kt` (`toDto` of the gaps and the state: `internal`)

- [x] 8.6 Add the calendar DTOs and the server mapper; verify with the mapper tests for both interlocutors, both To do entries and an empty day

## 9. [9/26] The topic routes · ~380, at risk · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `K/TopicRejectionDto.kt`, `S/api/TopicRoutes.kt` and its test, `server/app/src/test/kotlin/app/yap/server/ApplicationTest.kt`

At risk: if it grows over 400, slice 1 is the calendar route and slice 2 is the publish route with
the 400 body and 409, as two PRs.

- [x] 9.1 Add `TopicRejectionDto`, `GET /admin/topics/calendar`, `POST /admin/topics/{topicId}/publish`, the 400 body and 409; keep `GET /admin/topics` until [14/26]; verify with the route tests for the calendar with a fixed clock, publish 200, a publish of a published topic, 400 with the gaps and a past date, 401, 404 and 409, a GET of a draft with a taken date, and a PUT of a published topic with 400 and 409

## 10. [10/26] The admin topic counts · ~260 · `H/domain/` (`CharacterSummary`, the repository, `GetCharactersUseCase`), `H/data/`, `H/presentation/` (`CharactersViewModel`, `CharactersUiStateMapper`, `TopicCountText`), `H/presentation/ui/CharactersScreen.kt` (the count on the right), `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`), `A/ScreenCaptureTest.kt` (compile only)

- [x] 10.1 Add the count to the list domain, the Characters rows and the interlocutor choice; verify with the mapper tests for 0, 1, 2, 5, 11, 12 and 21 topics, the view model tests of both screens, and `./gradlew build`

## 11. [11/26] The captures of the counts · ~60 · `A/ScreenCaptureTest.kt`

- [ ] 11.1 Capture Characters and the Topic screen with counts; verify with the images

## 12. [12/26] The admin calendar domain · ~220 · `T/domain/` (`TopicCalendar`, `CalendarRow`, `CalendarTopic`, `LedBy`, `Todo`, `TopicField`, `TopicGap`, `ReleaseState`), `T/data/mapper/TopicCalendarMapper.kt`

- [ ] 12.1 Add the domain and the DTO mapper; verify with the mapper tests for each status, each gap, both interlocutors, both To do entries and an empty day

## 13. [13/26] The admin calendar call · ~200 · `T/data/api/TopicApi.kt`, `T/data/repository/`, `T/domain/repository/`, `T/domain/usecase/GetTopicCalendarUseCase.kt`, `T/FeatureTopicsModule.kt`

- [ ] 13.1 Add the API call, the repository call and the use case; they return the shared `LoadResult<TopicCalendar>` (`A/core/common/LoadResult.kt`), with no topic-only load result; verify with the mock engine tests for 200, a failed call and a call without saved settings

## 14. [14/26] The Topics view model on the calendar · ~250 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `T/presentation/` (`TopicsViewModel`, `TopicsUiStateMapper`: the rows of Date and Topic only), `A/ScreenCaptureTest.kt` (the Topics `DataState`), removals: `T/domain/usecase/GetTopicsUseCase.kt`, `T/domain/LoadTopicsResult.kt`, the admin `list`, `GET /admin/topics` in `S/api/TopicRoutes.kt` and its test, and `S/usecase/GetTopicsUseCase.kt`

- [ ] 14.1 Load the calendar in `TopicsViewModel`, keep it on a failed reload and hold the reload failure for the banner; `DataState` holds raw facts only, as `CharactersViewModel` does: `calendar: TopicCalendar?`, `isLoadFailed`, `isLoading`; no stored load result, no `null` that means Loading, no `loaded()` that decides what to keep; the mapper decides Loading, Error, Content and the banner; verify with the view model tests for the load, a return, a failed reload that keeps the calendar, Retry after a failed load and Retry after a failed reload, and the unchanged Topics capture images
- [ ] 14.2 Remove the list route, its use cases and the admin `list`; verify with `./gradlew build`

## 15. [15/26] The Topics UI state · ~300 · `A/core/design/` (`AdminTheme` `warn`, the `WARNING` branch of `StatusText`), `A/core/presentation/Status.kt` (`WARNING`), `T/presentation/` (`TopicsViewModel.UiState`, `TopicsUiStateMapper`, `TopicGapText`), `T/presentation/ui/TopicsScreen.kt` (compile only)

- [ ] 15.1 Add the `WARNING` tone with its color and its `StatusText` branch; verify with `./gradlew build` and the unchanged capture images
- [ ] 15.2 Map the rows, To do, the tones, Led by, 2 of 5, the empty day, the reload banner and nothing to do from the raw facts of [14/26]; `TopicGapText` names the fields and the steps 3–5; verify with the mapper tests for each status, a ready draft, a draft with the steps 3–5 incomplete, an empty day, nothing to do, and the banner over a kept calendar

## 16. [16/26] The Topics screen · ~280 · `A/core/design/` (the column widths), `T/presentation/ui/` (`TopicsScreen`, `TodoSection`)

Create opens a new topic without a date until [17/26].

- [ ] 16.1 Build To do, the header All topics, the five columns, the empty day row, the note and the reload banner with Retry from the UI state; verify with `./gradlew build` and the UI state tests of [15/26]

## 17. [17/26] Create on an empty day · ~150 · `A/Main.kt`, `A/navigation/` (`Screen.Topic` with the date), `T/FeatureTopicsModule.kt`, `T/presentation/TopicViewModel.kt`, `T/presentation/ui/TopicsScreen.kt`

- [ ] 17.1 Pass the date of Create to a new topic; verify with the navigation test for Create, and the view model test for a new topic with a date (Release date shows the long date)

## 18. [18/26] The captures of Topics · ~100 · `A/ScreenCaptureTest.kt`

- [ ] 18.1 Capture Topics with every status, an empty day, 2 of 5, a draft with the steps 3–5, nothing to do, the reload banner, loading and the error; verify with the images

## 19. [19/26] The admin publish domain · ~220 · `T/domain/` (`Topic` with the release state and the gaps, `SaveTopicResult` with the gaps and `DateTaken`, `PublishTopicResult`), `T/data/mapper/TopicMapper.kt`, `T/presentation/TopicViewModel.kt` (compile only)

- [ ] 19.1 Add the domain and the mapper; verify with the mapper tests for each status and each gap

## 20. [20/26] The admin publish call · ~220 · `T/data/api/TopicApi.kt`, `T/data/repository/RemoteTopicRepository.kt`, `T/domain/repository/`, `T/domain/usecase/PublishTopicUseCase.kt`, `T/FeatureTopicsModule.kt`

- [ ] 20.1 Add the publish call and the use case, and read the 400 body and 409 of the save; verify with the mock engine tests for publish 200, 400 with the gaps, 404, 409 and a failed call, and PUT 400 with the gaps and 409

## 21. [21/26] The Topic view model · three slices

### Slice 1 · Raw facts and the shared load result · ~250 · `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`) and their tests, `T/domain/` (`GetTopicUseCase`, the repository; removal: `LoadTopicResult.kt`), `T/data/repository/RemoteTopicRepository.kt` and its test, the test stubs, `A/ScreenCaptureTest.kt` (the Topic `DataState`)

Behaviour stays as it is; the existing tests keep their names and expectations.

- [ ] 21.1 Make `GetTopicUseCase` and the repository return `LoadResult<Topic>` and remove `LoadTopicResult`; verify with the existing mock engine tests for a saved topic and an unknown topic
- [ ] 21.2 Keep only raw facts in `DataState`: `topic: Topic?`, `cast` (the list type of [10/26], `List<CharacterSummary>?`), `form`, `guestDescription`, `isLoadFailed`, `isLoading`, `isSaving`, `saveResult: SaveTopicResult?`; no `topicId`: the load takes it from the constructor and the save reads `topic?.topicId`; remove the `Load` enum, `SaveStatus`, `saved()` and `withForm()`; the view model sets the facts in named private functions (`onTopicLoaded`, `onTopicSaved`); the mapper decides Loading, Error, Content and the save status; a named function (`SaveTopicResult?.keptAfterEdit()`) says that a rejected save stays until the next click and Saved clears at an edit; verify with the view model and mapper tests, which change only their `DataState` input while their names and expected `UiState` stay, a mapper test for each save result, and the unchanged Topic capture images

### Slice 2 · One event per field · ~270 · `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`), `T/presentation/ui/` (`TopicScreen`, `InterlocutorSection`, `StepsSection`), their tests

- [ ] 21.3 Replace `FormChanged(form)` and `StepChanged(index, step)` with one event per field: `TitleChanged`, `DescriptionChanged`, `SettingChanged`, `RoleChanged`, `OpeningEnglishChanged`, `OpeningTranslationChanged`, `StepTaskChanged(index, text)`, `StepPassWhenChanged(index, text)`, `StepOutcomeChanged(index, text)`, beside the kept `DurationChanged`, `ReleaseDatePicked`, `InterlocutorChosen`, `GuestDescriptionChanged` and `AddStepClicked`; the view model does each `copy`; `UiState.Content` carries `Fields` (the texts, the date and the step texts) instead of the domain `TopicContent`, and the screen builds no `copy()`; verify with a view model test for each event, the mapper test for the fields, the unchanged Topic capture images, and `./gradlew build`

### Slice 3 · Publish and the questions · ~300 · `T/presentation/TopicViewModel.kt` and its test

- [ ] 21.4 Add `PublishClicked` (opens the question), `PublishConfirmed`, `QuestionDismissed`, the busy flag and the outcomes; verify with the view model tests for the question, Cancel (sends nothing), a publish, the gaps, a taken date, a failed publish (one snackbar), and a second click while busy
- [ ] 21.5 Ask before a save that moves the date of a Published or Today topic, with `SaveConfirmed`; the open question and the publish result are raw facts in `DataState` (`question`, `isPublishing`, `publishResult: PublishTopicResult?`); verify with the view model tests for a move, the same date, an Out topic (no question) and Cancel

## 22. [22/26] The Topic view model: edits after an answer · ~150 · `T/presentation/TopicViewModel.kt` and its test

- [ ] 22.1 Mark that the form changed after the last answer with the raw fact `isEditedAfterAnswer` in `DataState`, keep the note and the rejection error through `keptAfterEdit()`, and clear the mark on the next answer; verify with the view model tests for an edit after a note, after a rejected draft, after a rejected publish, and a taken date on Save

## 23. [23/26] The Topic UI state: the status and the notes · ~300 · `T/presentation/TopicUiStateMapper.kt` and its test, `T/presentation/TopicViewModel.kt` (the `UiState` fields), `T/presentation/ui/TopicScreen.kt` (compile only)

- [ ] 23.1 Map the status slot in its order, Publish (disabled for a new topic, hidden when published), Save draft or Save, and the subtitle in the feminine forms; verify with the mapper tests for a new topic, a draft, each release state and Publishing…
- [ ] 23.2 Map the notes: save a draft to publish, all fields filled, the gaps with the steps 3–5, a past date, a taken date, an upcoming topic, a topic of today with edits seen at once, a topic that is out, and the lead after an edit; verify with a mapper test for each

## 24. [24/26] The Topic UI state: the errors and the questions · ~250 · `T/presentation/TopicUiStateMapper.kt` and its test, `T/presentation/TopicViewModel.kt` (the `UiState` fields)

- [ ] 24.1 Map each rejection in three parts (what happened, the changes are not saved, the next step) for Publish 400, Save draft 400, Save 400 and 409, with the lead after an edit; verify with a mapper test for each
- [ ] 24.2 Map the Publish question with the date and the move question with the stored date; verify with the mapper tests for both

## 25. [25/26] The Topic toolbar and the questions · ~250 · `T/presentation/ui/` (`TopicScreen`, `PublishQuestion`)

- [ ] 25.1 Add Publish beside Save draft, with the label and the visibility of the state, and the question panel below the toolbar with its two buttons; verify with `./gradlew build` and the UI state tests of [23/26] and [24/26]

## 26. [26/26] The captures of Topic and the archive · ~150 · `A/ScreenCaptureTest.kt`, `openspec/`

- [ ] 26.1 Capture Topic as a new topic, a ready draft, a draft with gaps, a past date, a taken date, the Publish question, Publishing…, a rejected publish, published, today, and the move question; verify with the images
- [ ] 26.2 Owner check: run the admin app against a server with a database; publish a topic, see Published in the calendar and after a restart, see the taken date on a second draft of its date, and see the counts in Characters
- [ ] 26.3 Run `/opsx:archive` as the last commit; verify that `openspec/specs/topics/spec.md` and `openspec/specs/characters/spec.md` update
