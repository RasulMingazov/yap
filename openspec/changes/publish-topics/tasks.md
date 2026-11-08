# Tasks

One group is one PR, in merge order: its size, its files (hot first). `S` is
`server/feature-topics/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/topics`, `C` is
`server/feature-characters/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/characters`,
`K` is `contract-admin/src/main/kotlin/app/yap/contract/admin`, `A` is
`admin/src/{main,test}/kotlin/app/yap/admin`, `H` is `A/feature/characters`, `T` is
`A/feature/topics`. A task marked «Owner check» needs a server with a database; the owner runs it.

## 1. [1/18] The plan · this PR

- [ ] 1.1 Write the change; verify with `openspec validate publish-topics --strict`

## 2. [2/18] The publish rule and the release state · ~260 · `S/model/` (`TopicField`, `TopicStatus`, `ReleaseState`, `TopicDraftRule`, `TopicPublishRule`, the Moscow zone), `S/usecase/` (`CreateTopicUseCase`, `UpdateTopicUseCase`)

- [ ] 2.1 Make `TopicDraftRule` return the gaps, and add `TopicPublishRule`; the use cases accept an empty set; verify with the rule tests for each field, a guest without a description, a step without an outcome, an unknown character, and the unchanged use case tests
- [ ] 2.2 Add `TopicStatus` and `ReleaseState` from the status, the date and today; verify with the tests for Out, Today, Published, a draft of today, and 22:30 UTC on 23 October as 24 October in Moscow

## 3. [3/18] The status in the database · ~250 · `server/feature-topics/src/main/resources/db/migration/V5__topic_status.sql`, `S/model/Topic.kt`, `S/model/StubTopic.kt`, `S/persistence/` (`TopicsTable`, `TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`), `S/usecase/UpdateTopicUseCase.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only)

- [ ] 3.1 Add `V5__topic_status.sql`, the status in `Topic` and the table, and an update by identifier and content that writes `where status = <the status it read>` and keeps it; its result is `Updated`, `NotFound`, `StaleStatus` or `DateTaken` from the start: the transaction sets `maxAttempts = 1`, the `topics` row is its first write, nothing inside the block catches the exception, and the repository maps the `ExposedSQLException` with SQLState `23505` to `DateTaken` outside the block; verify with the PostgreSQL tests for a new draft and a restart, two drafts of one date, an update that keeps the status, an update with a stale status (writes nothing), and the clean bootstrap (CI runs them)

## 4. [4/18] Publish in the database · ~200 · `S/persistence/` (`TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`) and the PostgreSQL test

- [ ] 4.1 Add `publish` with the same write rules as the update of [3/18]: `maxAttempts = 1`, the `topics` row as the first write, no catch inside the block, and `23505` mapped to `DateTaken` outside it; verify with the PostgreSQL tests for a publish and a restart, a second published topic on a date (stores nothing), a save of a published topic onto a taken date, and two publishes at once with a latch (one published, the other unchanged)

## 5. [5/18] The topic counts on the server · ~310 · `K/CharacterSummaryDto.kt`, `docs/server/001-structure.md` (the port rule), `C/model/CharacterSummary.kt`, `C/usecase/` (`CharacterTopicCounts` port, `GetCharacterSummariesUseCase`), `C/api/` (mapper, routes), `C/testFixtures` (stub port), `C/FeatureCharactersModule.kt`, `S/persistence/DatabaseCharacterTopicCounts.kt`, `S/FeatureTopicsModule.kt`, `H/data/` (decode `CharacterSummaryDto`, keep the domain)

- [ ] 5.1 Add the port, its query in `feature-topics`, `GetCharacterSummariesUseCase` and the counts in `GET /admin/characters`; `GetCharactersUseCase` stays without counts; verify with the PostgreSQL test for drafts and published topics and a character without topics, the route test for the list with counts, and `AppModulesTest`
- [ ] 5.2 Add the port rule to `docs/server/001-structure.md`; verify that the guide names where the interface lives and who binds it
- [ ] 5.3 Decode the summary in `RemoteCharacterRepository` in the same PR, so the admin list keeps working; verify with the mock engine test for the list

## 6. [6/18] The calendar on the server · ~340 · `S/model/` (`TopicCalendar`, its rows, `LedBy`, `Todo`), `S/usecase/GetTopicCalendarUseCase.kt`, `S/FeatureTopicsModule.kt` (the `Clock`)

- [ ] 6.1 Add `TopicCalendar` with the 14 days from today, the rows, the complete steps and To do; verify with the model tests for the scenarios The rows, No topics yet, To do, A draft on a date with a published topic and The steps of a row, the title order on one date, and both interlocutors
- [ ] 6.2 Add the use case with the clock and `GetCharactersUseCase` for the names; verify with a stub test with a fixed clock and the character names

## 7. [7/18] Publish and save · ~320 · `S/usecase/` (`PublishTopicUseCase`, `UpdateTopicUseCase`, `CreateTopicUseCase`, `GetTopicUseCase`), `S/model/` (the outcomes, `TopicDetails`), `S/FeatureTopicsModule.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only)

- [ ] 7.1 Add `PublishTopicUseCase` and the rule of the status in `UpdateTopicUseCase`; the outcomes carry the gaps and `DateTaken`; a stale status runs the update once more with the publish rule; verify with the stub tests for a publish, the gaps, an unknown topic, a taken date, a publish of a published topic, a save of a published topic with an empty setting (stores nothing), a save of a draft that stays a draft, and a stale status
- [ ] 7.2 Return `TopicDetails` (the topic, the release state, the publish gaps) from get, create, update and publish; verify with the stub tests with a fixed clock

## 8. [8/18] The topic DTOs · ~350 · `K/` (`TopicDto`, `TopicStatusDto`, `TopicFieldDto`, `TopicRejectionDto`, `TopicCalendarDto`, `CalendarRowDto`, `CalendarTopicDto`, `LedByDto`, `TodoDto`), `S/api/` (`TopicDtoMapper`, `TopicCalendarDtoMapper`), `T/` tests that build `TopicDto`

- [ ] 8.1 Add the DTOs and the server mappers; verify with the mapper tests for each status, each field, both interlocutors, both To do entries and an empty day
- [ ] 8.2 Fill the new `TopicDto` fields in the admin tests; verify with `./gradlew build`

## 9. [9/18] The topic routes · ~380, at risk · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `S/api/TopicRoutes.kt` and its test

At risk: if it grows over 400, slice 1 is the calendar route and slice 2 is the publish route with
the 400 body and 409, as two PRs.

- [ ] 9.1 Add `GET /admin/topics/calendar`, `POST /admin/topics/{topicId}/publish`, the 400 body and 409; keep `GET /admin/topics` until [12/18]; verify with the route tests for the calendar with a fixed clock, publish 200, a publish of a published topic, 400 with the gaps, 401, 404 and 409, and a PUT of a published topic with 400 and 409

## 10. [10/18] The admin topic counts · ~280 · `H/domain/` (`CharacterSummary`, the repository, `GetCharactersUseCase`), `H/data/`, `H/presentation/` (`CharactersUiStateMapper`, `TopicCountText`), `H/presentation/ui/CharactersScreen.kt`, `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`), `A/ScreenCaptureTest.kt`

- [ ] 10.1 Add the count to the list domain, the Characters rows and the interlocutor choice; verify with the mapper tests for 0, 1, 2, 5, 11, 12 and 21 topics, and the view model tests of both screens
- [ ] 10.2 Capture Characters and the Topic screen with counts; verify with the images

## 11. [11/18] The admin calendar data · ~330 · `T/domain/` (`TopicCalendar`, `CalendarRow`, `CalendarTopic`, `LedBy`, `Todo`, `TopicField`, `ReleaseState`), `T/data/api/TopicApi.kt`, `T/data/mapper/TopicCalendarMapper.kt`, `T/data/repository/`, `T/domain/usecase/GetTopicCalendarUseCase.kt`, `T/FeatureTopicsModule.kt`

- [ ] 11.1 Add the domain, the mapper, the API call, the repository call and the use case; verify with the mapper tests and the mock engine tests for 200, a failed call and a call without saved settings

## 12. [12/18] The Topics view model on the calendar · ~350 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `A/core/design/` (`AdminTheme` `warn`, the `WARNING` branch of `StatusText`), `A/core/presentation/Status.kt` (`WARNING`), `T/presentation/` (`TopicsViewModel`, `TopicsUiStateMapper`, `TopicFieldText`), `T/presentation/ui/TopicsScreen.kt` (compile only: Date and Topic), removals: `T/domain/usecase/GetTopicsUseCase.kt`, `T/domain/LoadTopicsResult.kt`, the admin `list`, `GET /admin/topics` in `S/api/TopicRoutes.kt` and its test, and `S/usecase/GetTopicsUseCase.kt`

- [ ] 12.1 Add the `WARNING` tone with its color and its `StatusText` branch; verify with `./gradlew build` and the unchanged capture images
- [ ] 12.2 Load the calendar in `TopicsViewModel` and map the rows and To do with their copy and tones; verify with the mapper tests for each status, Led by, 2 of 5, an empty day, a ready draft, a draft with gaps and nothing to do, and the view model tests for the load, a return, a failed reload that keeps the calendar, and Retry
- [ ] 12.3 Remove the list route, its use cases and the admin `list`; verify with `./gradlew build`

## 13. [13/18] The Topics screen · ~310 · `A/core/design/` (the column widths), `T/presentation/ui/` (`TopicsScreen`, `TodoSection`), `A/ScreenCaptureTest.kt`

Slice 1 (~230): To do, the header All topics, the five columns, the empty day row and the note.
Slice 2 (~80): the captures. Create opens a new topic without a date until [14/18].

- [ ] 13.1 Build To do and All topics from the UI state; verify with `./gradlew build` and the UI state tests of [12/18]
- [ ] 13.2 Capture Topics with every status, an empty day, 2 of 5, nothing to do, loading and the error; verify with the images

## 14. [14/18] Create on an empty day · ~150 · `A/Main.kt`, `A/navigation/` (`Screen.Topic` with the date), `T/FeatureTopicsModule.kt`, `T/presentation/TopicViewModel.kt`, `T/presentation/ui/TopicsScreen.kt`

- [ ] 14.1 Pass the date of Create to a new topic; verify with the navigation test for Create, and the view model test for a new topic with a date (Release date shows the long date)

## 15. [15/18] The admin publish data · ~330 · `T/domain/` (`Topic` with the release state and the gaps, `SaveTopicResult` with the gaps and `DateTaken`, `PublishTopicResult`), `T/data/` (`TopicApi.publish`, the mapper, the repository), `T/domain/usecase/PublishTopicUseCase.kt`, `T/FeatureTopicsModule.kt`, `T/presentation/TopicViewModel.kt` (compile only)

- [ ] 15.1 Add the domain, the mapper and the publish call; verify with the mapper tests for each status and field, and the mock engine tests for publish 200, 400 with the gaps, 404, 409 and a failed call, and PUT 400 and 409

## 16. [16/18] The Topic view model publish · ~200 · `T/presentation/TopicViewModel.kt` and its test

- [ ] 16.1 Add `PublishClicked`, the busy flag and the outcomes; verify with the view model tests for a publish, the gaps, a taken date on Publish and on Save, a failed publish (one snackbar), an edit after a rejected publish, and a second click while busy

## 17. [17/18] The Topic publish state · ~180 · `T/presentation/TopicUiStateMapper.kt` and its test, `T/presentation/TopicViewModel.kt` (the `UiState` fields)

- [ ] 17.1 Map the status slot in its order, Publish, Published, Save, and the subtitle; verify with the mapper tests for a new topic, a ready draft, a draft with gaps, Publishing…, each release state, and a rejected save of a published topic

## 18. [18/18] The Topic toolbar and the archive · ~220 · `T/presentation/ui/TopicScreen.kt`, `A/ScreenCaptureTest.kt`, `openspec/`

- [ ] 18.1 Add Publish beside Save draft, with the label of the state; verify with `./gradlew build` and the UI state tests of [17/18]
- [ ] 18.2 Capture Topic as a ready draft, a draft with gaps, Publishing…, a rejected publish, a taken date and published; verify with the images
- [ ] 18.3 Owner check: run the admin app against a server with a database; publish a topic, see Published in the calendar and after a restart, see a 409 for a second publish on its date, and see the counts in Characters
- [ ] 18.4 Run `/opsx:archive` as the last commit; verify that `openspec/specs/topics/spec.md` and `openspec/specs/characters/spec.md` update
