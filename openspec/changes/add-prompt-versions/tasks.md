# Tasks

`fp` is `server/feature-prompts/src/main/kotlin/app/yap/server/feature/prompts`, `ap` is
`admin/src/main/kotlin/app/yap/admin/feature/prompts`; tests mirror them. (hot): a hot file.

## 1. The plan

- [x] 1.1 Propose `add-prompt-versions` in `openspec/changes/add-prompt-versions/`; verify with `openspec validate add-prompt-versions --strict`

## 2. Server model and persistence (about 330 lines)

Starts after the #9 part with `V3__levels.sql` merges. That part starts after the #7 part with `V2__topics.sql` and `exposed-java-time`.

- [x] 2.1 Add `server/feature-prompts` with `PromptKey`, `PromptGroup`, `PromptVersion` and `Prompt`. Files: `settings.gradle.kts` (hot), `server/feature-prompts/build.gradle.kts`, `fp/model/`. Verify with `PromptKeyTest`: the six keys in order, their groups and the variables of each one, as the spec lists them
- [x] 2.2 Add `V4__prompts.sql`, the tables and the Exposed repository. Files: `server/feature-prompts/src/main/resources/db/migration/V4__prompts.sql` (hot, Flyway), `fp/persistence/PromptRepository.kt`, `fp/persistence/PromptTables.kt`, `fp/persistence/DatabasePromptRepository.kt`, `exposed-java-time` in `server/feature-prompts/build.gradle.kts`, and in `gradle/libs.versions.toml` (hot) only if #7 did not add it. Verify with the PostgreSQL tests for a read before a save, two saves, a restore, a save after a restore, an unknown version and the clean bootstrap (CI runs them)

## 3. Server use cases (about 200 lines)

- [x] 3.1 Add the list, read, save and restore use cases and their outcomes. The save use case does not trim: the DTO mapper of 4.1 does, and the use case rejects an empty text. Files: `fp/usecase/`, `fp/model/` (the outcomes), `server/feature-prompts/build.gradle.kts`, `server/feature-prompts/src/testFixtures/` (`StubPromptRepository`, `StubPrompt`). Verify with the use-case tests for the empty text and the unknown version

## 4. Server admin API (about 330 lines)

- [x] 4.1 Add the prompt DTOs, the routes and their mount behind the admin key. Files: `contract-admin/src/main/kotlin/app/yap/contract/admin/Prompt*Dto.kt` (hot), `fp/api/PromptRoutes.kt`, `fp/api/PromptDtoMapper.kt`, `server/app/build.gradle.kts`, `server/app/src/main/kotlin/app/yap/server/Application.kt` (hot). The mapper trims the text of a save. Verify with the mapper test for the trimmed text and the route tests for the six prompts of the list, 200, 201, 400, 401 and 404 for a key and for a version

## 5. Admin data layer and use cases (about 380 lines)

- [x] 5.1 Add the admin prompts repository and its mapper. Files: `ap/domain/`, `ap/domain/repository/PromptRepository.kt`, `ap/data/repository/RemotePromptRepository.kt`, `ap/data/mapper/PromptMapper.kt`. Verify with mock engine tests for 200, 201, 400, 404, a call without saved settings and a failed call
- [ ] 5.2 Add `GetPromptsUseCase`, `GetPromptUseCase`, `SavePromptUseCase` and `RestorePromptUseCase`. Files: `ap/domain/usecase/`. Verify with a use-case test that a 400 on save gives the result for an empty text

## 6. Admin view model (about 380 lines)

- [ ] 6.1 Add `PromptsViewModel`, its UI state and its mapper, with the Russian titles of the keys. Files: `ap/presentation/PromptsViewModel.kt`, `ap/presentation/PromptsUiStateMapper.kt`, `ap/presentation/PromptTitles.kt`, stub use cases in the tests. Verify with the mapper and view-model tests for the first open, a prompt without a version, the typed text on a return, the Saved status, the 400 that asks for the text, Restore, the save failure and the load error

## 7. Admin Prompts screen (about 350 lines)

Starts after the #7 part with the actions slot of `ScreenToolbar` and the #9 part with the mono style and the sidebar group Rules merge. This part reuses them and does not change them.

- [ ] 7.1 Add `Chip`, the Prompts screen, its graph, and Prompts in the sidebar group Rules after Levels. Files: `admin/src/main/kotlin/app/yap/admin/core/design/Chip.kt` (hot), `ap/presentation/ui/PromptsScreen.kt`, `ap/di/PromptsGraph.kt`, `admin/src/main/kotlin/app/yap/admin/Main.kt` (hot), `admin/src/main/kotlin/app/yap/admin/navigation/` (hot), `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`. Verify with the navigation view-model test for Prompts, `./gradlew build` and the images of the screen capture
- [ ] 7.2 Run the admin app against a server with a database; verify a first save, a second save, a restore and the versions after a server restart

## 8. Archive

- [ ] 8.1 Run `/opsx:archive` as the last commit of the last PR for #10. Files: `openspec/specs/prompts/spec.md`, `openspec/changes/archive/`. Verify that `openspec/specs/prompts/spec.md` holds the requirements of this change
