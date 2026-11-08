# Tasks

One group is one PR, in merge order: its size, its files (hot first). `S` is
`server/feature-topics/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/topics`, `C` is
`server/feature-characters/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/characters`,
`K` is `contract-admin/src/main/kotlin/app/yap/contract/admin`, `A` is
`admin/src/{main,test}/kotlin/app/yap/admin`, `H` is `A/feature/characters`, `T` is
`A/feature/topics`. A task marked «Owner check» needs a server with a database; the owner runs it.

## 1. [1/16] The plan · this PR

- [ ] 1.1 Write the change; verify with `openspec validate publish-topics --strict`

## 2. [2/16] The publish rule and the release state · ~260 · `S/model/` (`TopicField`, `TopicStatus`, `ReleaseState`, `TopicDraftRule`, `TopicPublishRule`, the Moscow zone), `S/usecase/` (`CreateTopicUseCase`, `UpdateTopicUseCase`)

- [ ] 2.1 Make `TopicDraftRule` return the gaps, and add `TopicPublishRule`; the use cases accept an empty set; verify with the rule tests for each field, a guest without a description, a step without an outcome, an unknown character, and the unchanged use case tests
- [ ] 2.2 Add `TopicStatus` and `ReleaseState` from the status, the date and today; verify with the tests for Out, Today, Published, a draft of today, and 22:30 UTC on 23 October as 24 October in Moscow

## 3. [3/16] The status in the database · ~390, at risk · `server/feature-topics/src/main/resources/db/migration/V5__topic_status.sql`, `S/model/Topic.kt`, `S/persistence/` (`TopicsTable`, `TopicRepository`, `DatabaseTopicRepository`, `StubTopicRepository`), `S/model/StubTopic.kt`, `S/usecase/UpdateTopicUseCase.kt`, `S/api/` (`TopicRoutes`, `TopicDtoMapper`: compile only)

- [ ] 3.1 Add `V5__topic_status.sql`, the status in `Topic` and the table, an update by identifier and content that keeps the status, and `publish`; map `23505` to `DateTaken`; verify with the PostgreSQL tests for a new draft, a publish and a restart, two drafts of one date, a second published topic (stores nothing), two publishes at once with a latch, an update that keeps the status, and the clean bootstrap (CI runs them)

## 4. [4/16] The topic counts on the server · ~300 · `K/CharacterSummaryDto.kt`, `C/model/CharacterSummary.kt`, `C/usecase/` (`CharacterTopicCounts` port, `GetCharactersUseCase`), `C/api/` (mapper, routes), `C/testFixtures` (stub port), `S/persistence/DatabaseCharacterTopicCounts.kt`, `S/FeatureTopicsModule.kt`, `H/data/` (decode `CharacterSummaryDto`, keep the domain)

- [ ] 4.1 Add the port, its query in `feature-topics` and the counts in `GET /admin/characters`; verify with the PostgreSQL test for drafts and published topics and a character without topics, the route test for the list with counts, and `AppModulesTest`
- [ ] 4.2 Decode the summary in `RemoteCharacterRepository` in the same PR, so the admin list keeps working; verify with the mock engine test for the list

## 5. [5/16] The calendar on the server · ~340 · `S/model/` (`TopicCalendar`, its rows, `LedBy`, `Todo`), `S/usecase/GetTopicCalendarUseCase.kt`, `S/FeatureTopicsModule.kt` (the `Clock`)

- [ ] 5.1 Add `TopicCalendar` with the 14 days from today, the rows, the complete steps and To do; verify with the model tests for the scenarios The rows, No topics yet, To do and The steps of a row, the title order on one date, and both interlocutors
- [ ] 5.2 Add the use case with the clock; verify with a stub test with a fixed clock and the character names

## 6. [6/16] Publish and save · ~300 · `S/usecase/` (`PublishTopicUseCase`, `UpdateTopicUseCase`, `CreateTopicUseCase`, `GetTopicUseCase`), `S/model/` (the outcomes, `TopicDetails`), `S/FeatureTopicsModule.kt`

- [ ] 6.1 Add `PublishTopicUseCase` and the rule of the status in `UpdateTopicUseCase`; the outcomes carry the gaps and `DateTaken`; verify with the stub tests for a publish, the gaps, an unknown topic, a taken date, a save of a published topic with an empty setting (stores nothing), and a save of a draft that stays a draft
- [ ] 6.2 Return `TopicDetails` (the topic, the release state, the publish gaps) from get, create, update and publish; verify with the stub tests with a fixed clock

## 7. [7/16] The topic DTOs · ~350 · `K/` (`TopicDto`, `TopicStatusDto`, `TopicFieldDto`, `TopicRejectionDto`, `TopicCalendarDto`, `CalendarRowDto`, `CalendarTopicDto`, `LedByDto`, `TodoDto`), `S/api/` (`TopicDtoMapper`, `TopicCalendarDtoMapper`), `T/` tests that build `TopicDto`

- [ ] 7.1 Add the DTOs and the server mappers; verify with the mapper tests for each status, each field, both interlocutors, both To do entries and an empty day
- [ ] 7.2 Fill the new `TopicDto` fields in the admin tests; verify with `./gradlew build`

## 8. [8/16] The topic routes · ~380 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `S/api/TopicRoutes.kt` and its test

- [ ] 8.1 Add `GET /admin/topics/calendar`, `POST /admin/topics/{topicId}/publish`, the 400 body and 409; keep `GET /admin/topics` until [11/16]; verify with the route tests for the calendar with a fixed clock, publish 200, 400 with the gaps, 401, 404 and 409, and a PUT of a published topic with 400 and 409

## 9. [9/16] The admin topic counts · ~280 · `H/domain/` (`CharacterSummary`, the repository, `GetCharactersUseCase`), `H/data/`, `H/presentation/` (`CharactersUiStateMapper`, `TopicCountText`), `H/presentation/ui/CharactersScreen.kt`, `T/presentation/` (`TopicViewModel`, `TopicUiStateMapper`), `A/ScreenCaptureTest.kt`

- [ ] 9.1 Add the count to the list domain, the Characters rows and the interlocutor choice; verify with the mapper tests for 0, 1, 2, 5, 11, 12 and 21 topics, and the view model tests of both screens
- [ ] 9.2 Capture Characters and the Topic screen with counts; verify with the images

## 10. [10/16] The admin calendar data · ~330 · `T/domain/` (`TopicCalendar`, `CalendarRow`, `CalendarTopic`, `LedBy`, `Todo`, `TopicField`, `ReleaseState`), `T/data/api/TopicApi.kt`, `T/data/mapper/TopicCalendarMapper.kt`, `T/data/repository/`, `T/domain/usecase/GetTopicCalendarUseCase.kt`, `T/FeatureTopicsModule.kt`

- [ ] 10.1 Add the domain, the mapper, the API call, the repository call and the use case; verify with the mapper tests and the mock engine tests for 200, a failed call and a call without saved settings

## 11. [11/16] The Topics view model on the calendar · ~340 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `T/presentation/` (`TopicsViewModel`, `TopicsUiStateMapper`, `TopicFieldText`), `A/core/presentation/Status.kt` (`WARNING`), `T/presentation/ui/TopicsScreen.kt` (compile only: Date and Topic), removals: `T/domain/usecase/GetTopicsUseCase.kt`, `T/domain/LoadTopicsResult.kt`, the admin `list`, `GET /admin/topics` and `S/usecase/GetTopicsUseCase.kt`

- [ ] 11.1 Load the calendar in `TopicsViewModel` and map the rows and To do with their copy and tones; verify with the mapper tests for each status, Led by, 2 of 5, an empty day, a ready draft, a draft with gaps and nothing to do, and the view model tests for the load, a return, a failed reload and Retry
- [ ] 11.2 Remove the list route, its use cases and the admin `list`; verify with `./gradlew build`

## 12. [12/16] The Topics screen · ~330 · `A/core/design/` (`AdminTheme` `warn`, `StatusText` `WARNING`, the column widths), `T/presentation/ui/` (`TopicsScreen`, `TodoSection`), `A/ScreenCaptureTest.kt`

Slice 1 (~250): the tokens, To do, the header All topics, the five columns, the empty day row and
the note. Slice 2 (~80): the captures. Create opens a new topic without a date until [13/16].

- [ ] 12.1 Build To do and All topics from the UI state; verify with `./gradlew build` and the UI state tests of [11/16]
- [ ] 12.2 Capture Topics with every status, an empty day, 2 of 5, nothing to do, loading and the error; verify with the images

## 13. [13/16] Create on an empty day · ~150 · `A/Main.kt`, `A/navigation/` (`Screen.Topic` with the date), `T/FeatureTopicsModule.kt`, `T/presentation/TopicViewModel.kt`, `T/presentation/ui/TopicsScreen.kt`

- [ ] 13.1 Pass the date of Create to a new topic; verify with the navigation test for Create, and the view model test for a new topic with a date (Release date shows the long date)

## 14. [14/16] The admin publish data · ~330 · `T/domain/` (`Topic` with the release state and the gaps, `SaveTopicResult` with the gaps and `DateTaken`, `PublishTopicResult`), `T/data/` (`TopicApi.publish`, the mapper, the repository), `T/domain/usecase/PublishTopicUseCase.kt`, `T/FeatureTopicsModule.kt`, `T/presentation/TopicViewModel.kt` (compile only)

- [ ] 14.1 Add the domain, the mapper and the publish call; verify with the mapper tests for each status and field, and the mock engine tests for publish 200, 400 with the gaps, 404, 409 and a failed call, and PUT 400 and 409

## 15. [15/16] The Topic view model publish · ~380, at risk · `T/presentation/TopicViewModel.kt` and its test, `T/presentation/TopicUiStateMapper.kt` and its test

Slice 1 (~200): the publish event, the busy flag and the outcomes in the view model. Slice 2 (~180):
the status slot, the labels, the subtitle and the enabled flags in the mapper.

- [ ] 15.1 Add `PublishClicked` and its outcomes; verify with the view model tests for a publish, the gaps, a taken date on Publish and on Save, a failed publish (one snackbar), an edit after a rejected publish, and a second click while busy
- [ ] 15.2 Map the status slot in its order, Publish, Published, Save, and the subtitle; verify with the mapper tests for a new topic, a ready draft, a draft with gaps, Publishing…, each release state, and a rejected save of a published topic

## 16. [16/16] The Topic toolbar and the archive · ~220 · `T/presentation/ui/TopicScreen.kt`, `A/ScreenCaptureTest.kt`, `openspec/`

- [ ] 16.1 Add Publish beside Save draft, with the label of the state; verify with `./gradlew build` and the UI state tests of [15/16]
- [ ] 16.2 Capture Topic as a ready draft, a draft with gaps, Publishing…, a rejected publish, a taken date and published; verify with the images
- [ ] 16.3 Owner check: run the admin app against a server with a database; publish a topic, see Published in the calendar and after a restart, see a 409 for a second publish on its date, and see the counts in Characters
- [ ] 16.4 Run `/opsx:archive` as the last commit; verify that `openspec/specs/topics/spec.md` and `openspec/specs/characters/spec.md` update
