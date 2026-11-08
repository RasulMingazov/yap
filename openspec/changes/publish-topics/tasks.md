# Tasks

One group is one PR, in merge order: its size, its files (hot first). `S` is
`server/feature-topics/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/topics`, `C` is
`server/feature-characters/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/characters`,
`K` is `contract-admin/src/main/kotlin/app/yap/contract/admin`, `A` is
`admin/src/{main,test}/kotlin/app/yap/admin`, `H` is `A/feature/characters`, `T` is
`A/feature/topics`. A task marked «Owner check» needs a server with a database; the owner runs it.

An admin part adds at most 300 lines, tests included. The UI state of a screen comes in a part
before the screen. The captures come in their own part.

## 1. [1/26] The plan · this PR

- [ ] 1.1 Write the change; verify with `openspec validate publish-topics --strict`

## 2. [2/26] The publish rule and the release state · ~300 · `S/model/` (`TopicField`, `TopicGap`, `TopicStatus`, `ReleaseState`, `TopicDraftRule`, `TopicPublishRule`, the Moscow zone), `S/usecase/` (`CreateTopicUseCase`, `UpdateTopicUseCase`)

- [x] 2.1 Make `TopicDraftRule` return the gaps, and add `TopicPublishRule` with `Missing`, `IncompleteSteps` (1-based numbers) and `PastDate`; the use cases accept an empty set; verify with the rule tests for each field, no steps, the steps 3 and 5 without an outcome, a guest without a description, an unknown character, a draft with a past date, a published topic that keeps its past date, a published topic moved into the past, and the unchanged use case tests
- [x] 2.2 Add `TopicStatus` and `ReleaseState` from the status, the date and today; verify with the tests for Out, Today, Published, a draft of today, and 22:30 UTC on 23 October as 24 October in Moscow

## 3. [3/26] The status in the database · ~250 · `server/feature-topics/src/main/resources/db/migration/V5__topic_status.sql`, `S/model/` (`Topic`, `StubTopic`, `UpdateTopicOutcome`), `S/persistence/` (`TopicWrite`, `TopicsTable`, `TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`), `S/usecase/UpdateTopicUseCase.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only), `server/core-database/src/testFixtures/kotlin/app/yap/server/core/database/TestDatabase.kt` (`withRestartableDatabase`)

- [x] 3.1 Add `V5__topic_status.sql`, the status in `Topic` and the table, and an update by identifier and content that writes `where status = <the status it read>` and keeps it; its result is a `TopicWrite` (`Written`, `NotFound`, `StaleStatus` or `DateTaken`) from the start, and `UpdateTopicUseCase` maps it to `UpdateTopicOutcome`: the transaction sets `maxAttempts = 1`, the `topics` row is its first write, nothing inside the block catches the exception, and the repository maps the `ExposedSQLException` with SQLState `23505` to `DateTaken` outside the block; verify with the PostgreSQL tests for a new draft and a restart, two drafts of one date, an update that keeps the status, an update with a stale status (writes nothing), and the clean bootstrap (CI runs them)

## 4. [4/26] Publish in the database · ~240 · `S/persistence/` (`TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`) and the PostgreSQL test

- [ ] 4.1 Add `publish` with the same write rules as the update of [3/26]: `maxAttempts = 1`, the `topics` row as the first write, no catch inside the block, and `23505` mapped to `DateTaken` outside it; verify with the PostgreSQL tests for a publish and a restart, a second published topic on a date (stores nothing), a save of a published topic onto a taken date, and two publishes at once with a latch (one published, the other unchanged)
- [ ] 4.2 Add `hasPublishedOn(date, exceptTopicId)`; verify with the PostgreSQL tests for a date with another published topic, with only the topic itself, and with drafts only

## 5. [5/26] The topic counts on the server · ~310 · `K/CharacterSummaryDto.kt`, `docs/server/001-structure.md` (the port rule), `C/model/CharacterSummary.kt`, `C/usecase/` (`CharacterTopicCounts` port, `GetCharacterSummariesUseCase`), `C/api/` (mapper, routes), `C/testFixtures` (stub port), `C/FeatureCharactersModule.kt`, `S/persistence/DatabaseCharacterTopicCounts.kt`, `S/FeatureTopicsModule.kt`, `H/data/` (decode `CharacterSummaryDto`, keep the domain)

- [ ] 5.1 Add the port, its query in `feature-topics`, `GetCharacterSummariesUseCase` and the counts in `GET /admin/characters`; `GetCharactersUseCase` stays without counts; verify with the PostgreSQL test for drafts and published topics and a character without topics, the route test for the list with counts, and `AppModulesTest`
- [ ] 5.2 Add the port rule to `docs/server/001-structure.md`; verify that the guide names where the interface lives and who binds it
- [ ] 5.3 Decode the summary in `RemoteCharacterRepository` in the same PR, so the admin list keeps working; verify with the mock engine test for the list

## 6. [6/26] The calendar on the server · ~340 · `S/model/` (`TopicCalendar`, its rows, `LedBy`, `Todo`), `S/usecase/GetTopicCalendarUseCase.kt`, `S/FeatureTopicsModule.kt` (the `Clock`)

- [ ] 6.1 Add `TopicCalendar` with the 14 days from today, the rows, the complete steps and To do with the gaps of each draft; verify with the model tests for the scenarios The rows, No topics yet, To do, A draft on a date with a published topic, The steps of a row, and a draft with the steps 3–5 incomplete, the title order on one date, and both interlocutors
- [ ] 6.2 Add the use case with the clock and `GetCharactersUseCase` for the names; verify with a stub test with a fixed clock and the character names

## 7. [7/26] Publish and save · ~340 · `S/usecase/` (`PublishTopicUseCase`, `UpdateTopicUseCase`, `CreateTopicUseCase`, `GetTopicUseCase`), `S/model/` (the outcomes, `TopicDetails`), `S/FeatureTopicsModule.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only)

- [ ] 7.1 Add `PublishTopicUseCase` and the rule of the status in `UpdateTopicUseCase`, with the stored topic and today for `PastDate`; the outcomes carry the gaps and `DateTaken`; a stale status runs the update once more with the publish rule; verify with the stub tests for a publish, the gaps, a past date, an unknown topic, a taken date, a publish of a published topic, a save of a published topic with an empty setting (stores nothing), a save of a draft that stays a draft, and a stale status
- [ ] 7.2 Return `TopicDetails` (the topic, the release state, the publish gaps, and `DateTaken` for a draft whose date has another published topic) from get, create, update and publish; verify with the stub tests with a fixed clock for a ready draft, a taken date and a published topic

## 8. [8/26] The topic DTOs · ~370 · `K/` (`TopicDto`, `TopicStatusDto`, `TopicFieldDto`, `TopicGapDto`, `TopicRejectionDto`, `TopicCalendarDto`, `CalendarRowDto`, `CalendarTopicDto`, `LedByDto`, `TodoDto`), `S/api/` (`TopicDtoMapper`, `TopicCalendarDtoMapper`), `T/` tests that build `TopicDto`

- [ ] 8.1 Add the DTOs and the server mappers; verify with the mapper tests for each status, each field, each gap, both interlocutors, both To do entries and an empty day
- [ ] 8.2 Fill the new `TopicDto` fields in the admin tests; verify with `./gradlew build`

## 9. [9/26] The topic routes · ~380, at risk · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `S/api/TopicRoutes.kt` and its test

At risk: if it grows over 400, slice 1 is the calendar route and slice 2 is the publish route with
the 400 body and 409, as two PRs.

- [ ] 9.1 Add `GET /admin/topics/calendar`, `POST /admin/topics/{topicId}/publish`, the 400 body and 409; keep `GET /admin/topics` until [14/26]; verify with the route tests for the calendar with a fixed clock, publish 200, a publish of a published topic, 400 with the gaps and a past date, 401, 404 and 409, a GET of a draft with a taken date, and a PUT of a published topic with 400 and 409

## 10. [10/26] The admin topic counts · ~260 · `H/domain/` (`CharacterSummary`, the repository, `GetCharactersUseCase`), `H/data/`, `H/presentation/` (`CharactersUiStateMapper`, `TopicCountText`), `H/presentation/ui/CharactersScreen.kt` (the count on the right), `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`)

- [ ] 10.1 Add the count to the list domain, the Characters rows and the interlocutor choice; verify with the mapper tests for 0, 1, 2, 5, 11, 12 and 21 topics, the view model tests of both screens, and `./gradlew build`

## 11. [11/26] The captures of the counts · ~60 · `A/ScreenCaptureTest.kt`

- [ ] 11.1 Capture Characters and the Topic screen with counts; verify with the images

## 12. [12/26] The admin calendar domain · ~220 · `T/domain/` (`TopicCalendar`, `CalendarRow`, `CalendarTopic`, `LedBy`, `Todo`, `TopicField`, `TopicGap`, `ReleaseState`), `T/data/mapper/TopicCalendarMapper.kt`

- [ ] 12.1 Add the domain and the DTO mapper; verify with the mapper tests for each status, each gap, both interlocutors, both To do entries and an empty day

## 13. [13/26] The admin calendar call · ~200 · `T/data/api/TopicApi.kt`, `T/data/repository/`, `T/domain/repository/`, `T/domain/usecase/GetTopicCalendarUseCase.kt`, `T/FeatureTopicsModule.kt`

- [ ] 13.1 Add the API call, the repository call and the use case; verify with the mock engine tests for 200, a failed call and a call without saved settings

## 14. [14/26] The Topics view model on the calendar · ~250 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `T/presentation/` (`TopicsViewModel`, `TopicsUiStateMapper`: the rows of Date and Topic only), removals: `T/domain/usecase/GetTopicsUseCase.kt`, `T/domain/LoadTopicsResult.kt`, the admin `list`, `GET /admin/topics` in `S/api/TopicRoutes.kt` and its test, and `S/usecase/GetTopicsUseCase.kt`

- [ ] 14.1 Load the calendar in `TopicsViewModel`, keep it on a failed reload and hold the reload failure for the banner; verify with the view model tests for the load, a return, a failed reload that keeps the calendar, Retry after a failed load and Retry after a failed reload
- [ ] 14.2 Remove the list route, its use cases and the admin `list`; verify with `./gradlew build`

## 15. [15/26] The Topics UI state · ~300 · `A/core/design/` (`AdminTheme` `warn`, the `WARNING` branch of `StatusText`), `A/core/presentation/Status.kt` (`WARNING`), `T/presentation/` (`TopicsViewModel.UiState`, `TopicsUiStateMapper`, `TopicGapText`), `T/presentation/ui/TopicsScreen.kt` (compile only)

- [ ] 15.1 Add the `WARNING` tone with its color and its `StatusText` branch; verify with `./gradlew build` and the unchanged capture images
- [ ] 15.2 Map the rows, To do, the tones, Led by, 2 of 5, the empty day, the reload banner and nothing to do; `TopicGapText` names the fields and the steps 3–5; verify with the mapper tests for each status, a ready draft, a draft with the steps 3–5 incomplete, an empty day, nothing to do, and the banner over a kept calendar

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

## 21. [21/26] The Topic view model: Publish and the questions · ~300 · `T/presentation/TopicViewModel.kt` and its test

- [ ] 21.1 Add `PublishClicked` (opens the question), `PublishConfirmed`, `QuestionDismissed`, the busy flag and the outcomes; verify with the view model tests for the question, Cancel (sends nothing), a publish, the gaps, a taken date, a failed publish (one snackbar), and a second click while busy
- [ ] 21.2 Ask before a save that moves the date of a Published or Today topic, with `SaveConfirmed`; verify with the view model tests for a move, the same date, an Out topic (no question) and Cancel

## 22. [22/26] The Topic view model: edits after an answer · ~150 · `T/presentation/TopicViewModel.kt` and its test

- [ ] 22.1 Mark that the form changed after the last answer, keep the note and the rejection error, and clear the mark on the next answer; verify with the view model tests for an edit after a note, after a rejected draft, after a rejected publish, and a taken date on Save

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
