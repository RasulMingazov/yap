# Tasks

One group is one PR, in merge order. (hot) marks a hot file. `fm` is
`server/feature-models/src/main/kotlin/app/yap/server/feature/models`, `fl` is
`server/feature-levels/src/main/kotlin/app/yap/server/feature/levels`, `llm` is
`server/core-llm/src/main/kotlin/app/yap/server/core/llm`, `am` is
`admin/src/main/kotlin/app/yap/admin/feature/models`, `al` is
`admin/src/main/kotlin/app/yap/admin/feature/levels`. Tests mirror them. An owner check needs a
server with a database: it cannot run on the agent machine. Each admin part is planned at 300 lines
or less; the captures and the archive have their own parts. `design-models-ux.md` is the UX review
of the design in the run folder: the decisions M1 to M6, m7 and m8.

## 1. [1/15] The plan

- [x] 1.1 Propose `add-task-models` in `openspec/changes/add-task-models/`; verify with `openspec validate add-task-models --strict`

## 2. [2/15] Server catalog and task model persistence (about 300 lines)

Needs: the `V5` part of #8 is merged into `develop`. Migrations merge in version order: with the
reset flag, a `V6` below an applied `V5` would fail the validation and clear the database.

Files: `llm/ModelProviderId.kt`, `llm/ModelChoice.kt`, `llm/ModelCatalog.kt`, `settings.gradle.kts`
(hot), `server/feature-models/build.gradle.kts`, `fm/model/` (`ModelTask`, `TaskModel`,
`TaskModelTableValidity`), `fm/persistence/` (`TaskModelRepository`, `TaskModelsTable`,
`DatabaseTaskModelRepository`), `server/feature-models/src/main/resources/db/migration/V6__task_models.sql`
(hot, Flyway), `server/feature-models/src/testFixtures/` (`StubTaskModel`), and their tests.

- [x] 2.1 Add the catalog in `core-llm`: DeepSeek with the exact API ids `deepseek-flash` and `deepseek-v4-pro`, as the `model` row of the DeepSeek pricing page lists them (see `design.md`), the key setting name `DEEPSEEK_API_KEY`, and the default; verify with `ModelCatalogTest`: the order, the default, the key setting name, and each model name in one provider only
- [x] 2.2 Add `server/feature-models` with `ModelTask` and the save rule; verify with the model tests for the five tasks in order, a missing task, a task twice and an unknown model; one test covers an unknown model and a model of another provider, because the catalog has one provider
- [x] 2.3 Add `V6__task_models.sql` and the Exposed repository that saves with an upsert by `task`; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 3. [3/15] Server Connected and the model use cases (about 310 lines)

Files: `llm/LlmConfig.kt`, `llm/ProviderConnections.kt`, `llm/CoreLlmModule.kt`,
`server/core-llm/build.gradle.kts` (Koin), `server/app/.../config/AppConfig.kt`,
`server/app/.../config/AppConfigLoader.kt`, `server/app/.../AppModules.kt`,
`server/app/build.gradle.kts`, `.env.example`, `server/app/src/test/.../StubAppConfig.kt`,
`server/app/src/test/.../config/AppConfigLoaderTest.kt`, `fm/model/` (`TaskModelChoice`,
`ModelsOverview`, `ProviderState`, `SaveTaskModelsOutcome`), `fm/usecase/` (`GetModelsUseCase`,
`SaveModelsUseCase`), `fm/FeatureModelsModule.kt`, `server/feature-models/build.gradle.kts`
(Koin, stubcall), `server/feature-models/src/testFixtures/` (`StubTaskModelRepository`), and their
tests.

- [x] 3.1 Read `DEEPSEEK_API_KEY` as an optional setting into `LlmConfig`, and bind `ProviderConnections` in `coreLlmModule(config)`; add an empty `DEEPSEEK_API_KEY=` line to `.env.example`; verify with `AppConfigLoaderTest` for a key, no key and a blank key, the `ProviderConnections` test, and `AppModulesTest`
- [x] 3.2 Add the shared rule `TaskModelChoice` in `fm/model/`: the stored row when it is in the catalog, else the default; verify with `TaskModelChoiceTest` for no row, a row in the catalog and a row outside it
- [x] 3.3 Add `GetModelsUseCase` and `SaveModelsUseCase`, with `TaskModelChoice` in the read and the outcomes `Invalid` and `CatalogChanged`; verify with the use-case tests for the defaults before a save, Connected with and without a key, `Invalid` for a task missing or twice, `CatalogChanged` for a model outside the catalog, a rejected save that saves nothing, and a save with a provider without a key

## 4. [4/15] Server admin API (about 350 lines)

Files: `contract-admin/src/main/kotlin/app/yap/contract/admin/` `ModelsDto.kt`, `TaskModelDto.kt`,
`ProviderDto.kt` (hot), `fm/api/TaskModelDtoMapper.kt`, `fm/api/ModelRoutes.kt`,
`server/feature-models/build.gradle.kts` (`yap.ktor.server`, `contract-admin`), `server/app/src/main/kotlin/app/yap/server/Application.kt`
(hot), and their tests.

- [x] 4.1 Add the model DTOs with `ProviderDto.keySetting`, the mapper that reads task codes with `entries.find` and keeps an unknown provider code as no provider, the routes, and mount them behind the admin key; verify with the mapper test and the route tests for `GET` 200 with the five tasks and the providers, `keySetting` and no key field in the JSON, `PUT` 200, 400 for an unknown task and for a task missing, 409 for an unknown provider and for a model outside the catalog, and 401

## 5. [5/15] Server level model and the model of a call (about 330 lines)

Needs: part 2 with `V6` is merged. `V7` is reserved for this part.

Files: `server/feature-levels/src/main/resources/db/migration/V7__level_model.sql` (hot, Flyway),
`fl/persistence/LevelsTable.kt`, `fl/persistence/DatabaseLevelRepository.kt`,
`fl/model/LevelSettings.kt`, `fl/model/LevelTableValidity.kt`, `fl/model/SaveLevelsOutcome.kt`,
`fl/usecase/SaveLevelsUseCase.kt`, `fl/api/LevelDtoMapper.kt`, `fl/api/LevelRoutes.kt`,
`server/feature-levels/build.gradle.kts` (`core-llm`),
`contract-admin/src/main/kotlin/app/yap/contract/admin/LevelSettingsDto.kt` (hot),
`server/feature-levels/src/testFixtures/` (`StubLevel`), `fm/usecase/GetTaskModelUseCase.kt`,
`fm/FeatureModelsModule.kt`, `server/feature-models/build.gradle.kts` (`feature-levels`), `fm/model/ModelTask.kt` (the
`internal val dependsOnLevel`: Character reply and Hints), and their tests.

- [ ] 5.1 Add the optional level model: the column, the upsert that also sets it, the read that gives no model for a stored model outside the catalog, the outcome `ModelUnavailable` with 409 for a saved model outside the catalog, and `LevelSettingsDto.model` with the default `null`; verify with the use-case test for an unknown model, the mapper test, the route tests for a level model and for 409, and the PostgreSQL tests for a save with a model, a second save without one, and a stored model outside the catalog that reads as no model (CI runs them)
- [ ] 5.2 Add `GetTaskModelUseCase(task, level)` with `TaskModelChoice` for the task row; verify with the use-case tests for a level without a model, a level model for Character reply and for Hints, a level model that Step check, Review and Calibration ignore, `dependsOnLevel` for the five tasks, a stored task row outside the catalog that gives the default, and a save between two calls that changes the model of the second call

## 6. [6/15] Admin models data layer (about 300 lines)

Files: `am/domain/` (`ModelTask`, `ModelChoice`, `TaskModel`, `Provider`, `Models`,
`SaveModelsResult` with `CatalogChanged`), `am/domain/repository/ModelRepository.kt`,
`am/data/api/ModelApi.kt`, `am/data/mapper/ModelMapper.kt`,
`am/data/repository/RemoteModelRepository.kt`, `am/domain/usecase/` (`GetModelsUseCase`,
`SaveModelsUseCase`), `admin/src/test/.../feature/models/domain/StubModels.kt`, and their tests.

- [ ] 6.1 Add the admin models repository, its mapper and the two use cases; verify with mock engine tests for `GET` 200 with `keySetting`, `PUT` 200, 400, 409 that gives `CatalogChanged`, an unknown task code in the answer, a call without saved settings and a failed call. No use-case test: the use cases only delegate

## 7. [7/15] Admin Models UI state and its mapper (about 300 lines)

The `ux-review` of the canvas Models screen is done. This part holds the state of M1, M5, m7 and
m8, before the screen. Split of the view model: this part holds the state types nested in
`ModelsViewModel` and the mapper with its tests; part 8 holds the logic.

Files: `am/presentation/ModelsViewModel.kt` (the nested `DataState`, `UiState`, `Event` and `News`
only), `am/presentation/ModelsUiStateMapper.kt`, `am/presentation/ModelTaskTitles.kt` (it calls
`PromptKey.title()` and `PromptKey.usage()` of `admin/.../feature/prompts/presentation/PromptTitles.kt`),
and `ModelsUiStateMapperTest`.

- [ ] 7.1 Add the Models UI state and `ModelsUiStateMapper`; verify with the mapper tests for the loader while loading (m7), the defaults on the first open, the task lines from the prompts and the Calibration line (m8), Not connected with its tone and the step that names `keySetting`, the Not connected mark on a task row (M1), the model choices of the chosen provider, the saving, Saved, rejected and catalog-changed statuses with Refresh (M5), the load error, and a task missing in the answer

## 8. [8/15] Admin Models view model (about 300 lines)

Files: `am/presentation/ModelsViewModel.kt` (events and logic), `am/FeatureModelsModule.kt`,
`admin/src/main/kotlin/app/yap/admin/AppModules.kt`, `admin/src/test/.../feature/models/domain/usecase/`
(`StubGetModelsUseCase`, `StubSaveModelsUseCase`), and `ModelsViewModelTest`.

- [ ] 8.1 Add the events and the logic of `ModelsViewModel`, and `featureModelsModule()` in `appModules()`; verify with the view-model tests for the load on `Opened` and Opened again, a provider change that sets its first model, the Saved status and its reset on a change, the guard while saving, 400, 409 and Refresh that loads again (M5), the save failure snackbar, Retry after a load error, and `AppModulesTest`

## 9. [9/15] Admin Models screen and the sidebar (about 300 lines)

This part uses the state of parts 7 and 8 and does not change it. Part 13 holds the captures.

Files: `am/presentation/ui/ModelsScreen.kt`, `admin/src/main/kotlin/app/yap/admin/Main.kt` (hot),
`admin/src/main/kotlin/app/yap/admin/navigation/NavigationViewModel.kt` (hot, `Screen.Models`),
`navigation/NavigationUiStateMapper.kt` (hot), `navigation/ui/Sidebar.kt` (hot, Models in Settings),
`navigation/ui/AdminShell.kt` (hot), and the navigation tests. #10 changed the same navigation files.

- [ ] 9.1 Add the Models screen with the sections Who does what and Providers, the loader, the statuses beside Save with Refresh, and Models in the sidebar group Settings after Server; verify with the navigation view-model test for Models and `./gradlew build`
- [ ] 9.2 Owner check: run the admin app against a server with a database, with a DeepSeek key and without one; verify a first save, Connected and Not connected, and the choices after a server restart

## 10. [10/15] Admin Levels UI state and its mapper (about 280 lines)

This part holds the state of M2, M3, M4 and M6, before the screen. Split of the view model: this
part holds the state types nested in `LevelsViewModel` and the mapper with its tests; part 11 holds
the logic.

Files: `al/domain/LevelSettings.kt` (`model`), `al/domain/SaveLevelsResult.kt` (`ModelUnavailable`),
`al/presentation/LevelsViewModel.kt` (the nested state types only: the model of the form, the model
choices, the state of the models load, the status `ModelUnavailable`),
`al/presentation/LevelsUiStateMapper.kt`, `admin/src/test/.../feature/levels/domain/StubLevel.kt`,
and `LevelsUiStateMapperTest`.

- [ ] 10.1 Add the level model to the Levels UI state; verify with the mapper tests for Default before a save, the choices in the server's order with the one name Default (M2), the No rating line on the calibration model (M3), Default disabled and the models error with Retry while the table shows (M4), and the status that a level model is no longer available with Refresh (M6)

## 11. [11/15] Admin Levels data and view model (about 300 lines)

Files: `al/data/mapper/LevelMapper.kt`, `al/data/repository/RemoteLevelRepository.kt` (409),
`al/presentation/LevelsViewModel.kt` (logic), `StubGetModelsUseCase` (from part 8),
`RemoteLevelRepositoryTest`, and `LevelsViewModelTest`.

- [ ] 11.1 Map the level model and 409 in the levels repository; verify with `RemoteLevelRepositoryTest` for a level with a model and without one, and 409 that gives `ModelUnavailable`
- [ ] 11.2 Load the levels and the models together in `LevelsViewModel`, with the models Retry, the model change of a level, the stored model in a save without the models, and Refresh after `ModelUnavailable`; verify with the view-model tests for each of these, a chosen model and Default in the save, and the levels load error

## 12. [12/15] Admin Levels Model column (about 200 lines)

This part uses the state of parts 10 and 11 and does not change it. Part 14 holds the captures.

Files: `al/presentation/ui/LevelsScreen.kt`.

- [ ] 12.1 Add the Model column, the table note on Character reply, Hints and Default (M2), the No rating line (M3), the models error with Retry (M4), and the status with Refresh beside Save (M6); verify with `./gradlew build`
- [ ] 12.2 Owner check: run the admin app against a server with a database; verify a level model after a save and after a server restart

## 13. [13/15] Models screen captures

Files: `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt` (the Models states) and the
images, as `docs/testing/004-screen-capture.md` says.

- [ ] 13.1 Capture Models in the loading, loaded, Not connected, saving, Saved, rejected, catalog-changed and load error states; verify with the images, then run `ui-review` against the canvas Models screen

## 14. [14/15] Levels Model column captures

Files: `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt` (the Levels states) and the
images.

- [ ] 14.1 Capture Levels with the Model column, the models error and the level-model-unavailable status; verify with the images, then run `ui-review` against the canvas Levels screen

## 15. [15/15] Archive

Files: `openspec/changes/add-task-models/`, `openspec/specs/task-models/spec.md`,
`openspec/specs/rating-levels/spec.md`, `openspec/changes/archive/`.

- [ ] 15.1 Run `/opsx:archive` as the last commit of the last PR for #11, so `openspec/specs/` updates at the merge; verify that `openspec/specs/task-models/spec.md` and `openspec/specs/rating-levels/spec.md` hold the requirements of this change, with `openspec validate --strict`
