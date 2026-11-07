# Tasks

One group is one PR, in merge order: its size, its files (hot first). `S` is
`server/feature-topics/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/topics`, `A` is
`admin/src/{main,test}/kotlin/app/yap/admin`, `T` is `A/feature/topics`. One executor runs at a
time from `develop`, so the files that #9 and #10 also change need no lock.

## 1. [1/16] The plan · this PR

- [x] 1.1 Write the change; verify with `openspec validate add-topic-draft --strict`

## 2. [2/16] The toolbar actions slot · ~30 · `A/core/design/ScreenToolbar.kt` · before the screens of #9 and #10

- [ ] 2.1 Add `actions: @Composable RowScope.() -> Unit = {}` after the title; verify with `./gradlew build` and unchanged capture images

## 3. [3/16] The topic model · ~190 · `settings.gradle.kts`, `server/feature-topics/build.gradle.kts`, `S/model/`

- [x] 3.1 Add the module, the model and the member `TopicContent.isDraftReady`; verify with the model tests for no title, no date, a zero duration, no duration and an empty guest

## 4. [4/16] The persistence · ~400 · `gradle/libs.versions.toml`, `server/feature-topics/src/main/resources/db/migration/V2__topics.sql`, `server/feature-topics/build.gradle.kts`, `S/persistence/` · `V2` merges before `V3` of #9; #10 reuses `exposed-java-time`

- [x] 4.1 Add `exposed-java-time`, `V2__topics.sql`, the tables and the Exposed repository; verify with the PostgreSQL tests for create, read, update with fewer steps, the step order, the list order, the guest and the clean bootstrap (CI runs them)

## 5. [5/16] The use cases · ~240 · `server/feature-topics/build.gradle.kts` (`testFixturesApi(libs.stubcall)` and `testImplementation(testFixtures(...))` of `:server:feature-characters`), `S/usecase/`, `S/persistence/StubTopicRepository.kt` (moved from [4/16], so its first consumer adds it)

- [ ] 5.1 Add the list, read, create and update use cases; create and update check `isDraftReady` and the character, and return a typed `Invalid` outcome; verify with the stub tests for the content that is ready, the content that is not ready, the unknown character and the unknown topic

## 6. [6/16] The DTOs · ~170 · `contract-admin/src/main/kotlin/app/yap/contract/admin/` (`TopicDto`, `TopicContentDto`, `InterlocutorDto`), `S/api/TopicDtoMapper.kt`

- [ ] 6.1 Add the DTOs and the server mapper, which trims every text and drops the empty steps; verify with the mapper tests for the trim, the empty step, both interlocutors, no interlocutor and a date that does not parse

## 7. [7/16] The routes · ~330 · `server/app/src/main/kotlin/app/yap/server/Application.kt`, `server/app/build.gradle.kts`, `S/api/TopicRoutes.kt`

- [ ] 7.1 Add the routes behind the admin key; verify with the route tests for 200, 201, 400 (no title, no date, zero duration, unknown character, bad date), 401 and 404

## 8. [8/16] The admin domain · ~170 · `T/domain/`, `T/data/mapper/`

- [ ] 8.1 Add the domain model and the DTO mapper; verify with the mapper tests for both interlocutors, no date and no duration

## 9. [9/16] The admin repository · ~270 · `T/domain/repository/`, `T/data/repository/`

- [ ] 9.1 Add `RemoteTopicRepository`; verify with mock engine tests for 200, 201, 400, a call without saved settings and a failed call

## 10. [10/16] The Topics view model · ~280 · `T/domain/usecase/GetTopicsUseCase.kt`, `T/presentation/Topics*.kt`, `T/presentation/ReleaseDateFormat.kt`, `T/di/`

- [ ] 10.1 Add the use case, the view model, the mapper and the Russian date format; verify with the tests for the rows, the short date, the empty list, the load error, Retry and a return

## 11. [11/16] Topics and the sidebar · ~280 · `A/Main.kt`, `A/navigation/`, `A/ScreenCaptureTest.kt`, `T/presentation/ui/TopicsScreen.kt`

- [ ] 11.1 Add the Topics screen without New topic and row clicks ([14/16] adds them), Topics before Characters and the start on Topics; verify with the navigation tests for both start screens
- [ ] 11.2 Capture Topics with rows, empty, loading and the error; verify with the images

## 12. [12/16] The Topic use cases and state · ~260 · `T/domain/usecase/GetTopicUseCase.kt`, `SaveTopicUseCase.kt`, `T/presentation/TopicUiState*.kt`

- [ ] 12.1 Add the use cases, the UI state and its mapper; verify with the tests for create, update, a new and a saved topic, the long date, Guest and the 400 error

## 13. [13/16] The Topic view model · ~370, at risk · `T/presentation/TopicViewModel.kt` and its test

- [ ] 13.1 Add the view model; verify with the tests for the load, the field edits, a picked date, Add step, Guest, Saved, the 400 error, the save failure and the load error. Over 400 lines: the step events and their tests go to [15/16]

## 14. [14/16] Topic: Scene and Opening line · ~300 · `A/Main.kt`, `A/navigation/`, `A/ScreenCaptureTest.kt`, `T/presentation/ui/` (`TopicScreen`, `ReleaseDateField`, `TopicsScreen`)

- [ ] 14.1 Add the screen, the two sections and the date popup, and open it from a title and from New topic; verify with `./gradlew build`
- [ ] 14.2 Capture Topic new, saved, Saved, the 400 error and the load error; verify with the images

## 15. [15/16] Topic: Interlocutor and Steps · ~260 · `A/ScreenCaptureTest.kt`, `T/presentation/ui/` (`InterlocutorSection`, `CastRadioRow`, `StepsSection`)

- [ ] 15.1 Add the two sections; verify with `./gradlew build` and the capture of Guest and five steps
- [ ] 15.2 Run the admin app against a server with a database; verify a create, an edit with Add step and Guest, and the topic after a restart

## 16. [16/16] The archive · `openspec/`

- [ ] 16.1 Run `/opsx:archive` as the last commit; verify that `openspec/specs/topics/spec.md` and `openspec/specs/characters/spec.md` update
