# Tasks

Paths: `fp` is `server/feature-prompts/src/main/kotlin/app/yap/server/feature/prompts`, and `ap` is
`admin/src/main/kotlin/app/yap/admin/feature/prompts`. Tests mirror them under `src/test`. (hot)
marks a hot file of `docs/process/README.md`.

## 1. The plan

- [x] 1.1 Propose `add-prompt-versions` in `openspec/changes/add-prompt-versions/`; verify with `openspec validate add-prompt-versions --strict`

## 2. Server model and persistence

Starts after part 2/7 of #9 (`V3__levels.sql`) merges. That part starts after part 2/9 of #7 (`V2__topics.sql`, `exposed-java-time`).

- [ ] 2.1 Add `server/feature-prompts` with `PromptKey`, `PromptGroup`, `PromptVersion` and `Prompt`. Files: `settings.gradle.kts` (hot), `server/feature-prompts/build.gradle.kts`, `fp/model/`. Verify with `PromptKeyTest`: the six keys in order, their groups and the variables of each one, as the spec lists them
- [ ] 2.2 Add `V4__prompts.sql`, the tables and the Exposed repository. Files: `server/feature-prompts/src/main/resources/db/migration/V4__prompts.sql` (hot, Flyway), `fp/persistence/PromptRepository.kt`, `fp/persistence/PromptTables.kt`, `fp/persistence/DatabasePromptRepository.kt`, `exposed-java-time` in `server/feature-prompts/build.gradle.kts`, and in `gradle/libs.versions.toml` (hot) only if #7 did not add it. Verify with the PostgreSQL tests for a read before a save, two saves, a restore, a save after a restore, an unknown version and the clean bootstrap (CI runs them)

## 3. Server use cases

- [ ] 3.1 Add the list, read, save and restore use cases and their outcomes. Files: `fp/usecase/`, `fp/model/` (the outcomes), `server/feature-prompts/src/testFixtures/` (`StubPromptRepository`). Verify with the use-case tests for the trimmed text, the empty text and the unknown version

## 4. Server admin API

- [ ] 4.1 Add the prompt DTOs, the routes and their mount behind the admin key. Files: `contract-admin/src/main/kotlin/app/yap/contract/admin/Prompt*Dto.kt` (hot), `fp/api/PromptRoutes.kt`, `fp/api/PromptDtoMapper.kt`, `server/app/build.gradle.kts`, `server/app/src/main/kotlin/app/yap/server/Application.kt` (hot). Verify with the route tests for the six prompts of the list, 200, 201, 400, 401 and 404 for a key and for a version

## 5. Admin data layer and use cases

- [ ] 5.1 Add the admin prompts repository and its mapper. Files: `ap/domain/`, `ap/domain/repository/PromptRepository.kt`, `ap/data/repository/RemotePromptRepository.kt`, `ap/data/mapper/PromptMapper.kt`. Verify with mock engine tests for 200, 201, 400, 404, a call without saved settings and a failed call
- [ ] 5.2 Add `GetPromptsUseCase`, `GetPromptUseCase`, `SavePromptUseCase` and `RestorePromptUseCase`. Files: `ap/domain/usecase/`. Verify with a use-case test that a 400 on save gives the result for an empty text

## 6. Admin view model

- [ ] 6.1 Add `PromptsViewModel`, its UI state and its mapper, with the Russian titles of the keys. Files: `ap/presentation/PromptsViewModel.kt`, `ap/presentation/PromptsUiStateMapper.kt`, `ap/presentation/PromptTitles.kt`, stub use cases in the tests. Verify with the mapper and view-model tests for the first open, a prompt without a version, the typed text on a return, the Saved status, the 400 that asks for the text, Restore, the save failure and the load error

## 7. Admin Prompts screen

Starts after part 6/9 of #7 (the actions slot of `ScreenToolbar`) and part 6/7 of #9 (the mono style and the sidebar group Rules) merge. This part reuses them and does not change them.

- [ ] 7.1 Add `Chip`, the Prompts screen, its graph, and Prompts in the sidebar group Rules after Levels. Files: `admin/src/main/kotlin/app/yap/admin/core/design/Chip.kt` (hot), `ap/presentation/ui/PromptsScreen.kt`, `ap/di/PromptsGraph.kt`, `admin/src/main/kotlin/app/yap/admin/Main.kt` (hot), `admin/src/main/kotlin/app/yap/admin/navigation/` (hot), `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`. Verify with the navigation view-model test for Prompts, `./gradlew build` and the images of the screen capture
- [ ] 7.2 Run the admin app against a server with a database; verify a first save, a second save, a restore and the versions after a server restart

## 8. Archive

- [ ] 8.1 Run `/opsx:archive` as the last commit of the last PR for #10. Files: `openspec/specs/prompts/spec.md`, `openspec/changes/archive/`. Verify that `openspec/specs/prompts/spec.md` holds the requirements of this change
