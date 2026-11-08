# Tasks

One group is one PR, in merge order. (hot) marks a hot file. `fm` is
`server/feature-models/src/main/kotlin/app/yap/server/feature/models`, `fl` is
`server/feature-levels/src/main/kotlin/app/yap/server/feature/levels`, `llm` is
`server/core-llm/src/main/kotlin/app/yap/server/core/llm`, `am` is
`admin/src/main/kotlin/app/yap/admin/feature/models`, `al` is
`admin/src/main/kotlin/app/yap/admin/feature/levels`. Tests mirror them. An owner check needs a
server with a database: it cannot run on the agent machine.

## 1. [1/10] The plan

- [x] 1.1 Propose `add-task-models` in `openspec/changes/add-task-models/`; verify with `openspec validate add-task-models --strict`

## 2. [2/10] Server catalog and task model persistence (about 300 lines)

Needs: the `V5` part of #8 is merged into `develop`. Migrations merge in version order: with the
reset flag, a `V6` below an applied `V5` would fail the validation and clear the database.

Files: `llm/ModelProviderId.kt`, `llm/ModelChoice.kt`, `llm/ModelCatalog.kt`, `settings.gradle.kts`
(hot), `server/feature-models/build.gradle.kts`, `fm/model/` (`ModelTask`, `TaskModel`,
`TaskModelTableValidity`), `fm/persistence/` (`TaskModelRepository`, `TaskModelsTable`,
`DatabaseTaskModelRepository`), `server/feature-models/src/main/resources/db/migration/V6__task_models.sql`
(hot, Flyway), `server/feature-models/src/testFixtures/` (`StubTaskModel`), and their tests.

- [ ] 2.1 Add the catalog in `core-llm`: DeepSeek with the exact API ids `deepseek-flash` and `deepseek-v4-pro`, as the `model` row of the DeepSeek pricing page lists them (see `design.md`), and the default; verify with `ModelCatalogTest`: the order, the default, and each model name in one provider only
- [ ] 2.2 Add `server/feature-models` with `ModelTask` and the save rule; verify with the model tests for the five tasks in order, a missing task, a task twice, an unknown model and a model of another provider
- [ ] 2.3 Add `V6__task_models.sql` and the Exposed repository that saves with an upsert by `task`; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 3. [3/10] Server Connected and the model use cases (about 310 lines)

Files: `llm/LlmConfig.kt`, `llm/ProviderConnections.kt`, `llm/CoreLlmModule.kt`,
`server/core-llm/build.gradle.kts` (Koin), `server/app/.../config/AppConfig.kt`,
`server/app/.../config/AppConfigLoader.kt`, `server/app/.../AppModules.kt`,
`server/app/build.gradle.kts`, `.env.example`, `server/app/src/test/.../StubAppConfig.kt`,
`server/app/src/test/.../config/AppConfigLoaderTest.kt`, `fm/model/` (`TaskModelChoice`,
`ModelsOverview`, `ProviderState`, `SaveTaskModelsOutcome`), `fm/usecase/` (`GetModelsUseCase`,
`SaveModelsUseCase`), `fm/FeatureModelsModule.kt`, `server/feature-models/src/testFixtures/`
(`StubTaskModelRepository`), and their tests.

- [ ] 3.1 Read `DEEPSEEK_API_KEY` as an optional setting into `LlmConfig`, and bind `ProviderConnections` in `coreLlmModule(config)`; add an empty `DEEPSEEK_API_KEY=` line to `.env.example`; verify with `AppConfigLoaderTest` for a key, no key and a blank key, the `ProviderConnections` test, and `AppModulesTest`
- [ ] 3.2 Add the shared rule `TaskModelChoice` in `fm/model/`: the stored row when it is in the catalog, else the default; verify with `TaskModelChoiceTest` for no row, a row in the catalog and a row outside it
- [ ] 3.3 Add `GetModelsUseCase` and `SaveModelsUseCase`, with `TaskModelChoice` in the read; verify with the use-case tests for the defaults before a save, Connected with and without a key, a rejected save that saves nothing, and a save with a provider without a key

## 4. [4/10] Server admin API (about 330 lines)

Files: `contract-admin/src/main/kotlin/app/yap/contract/admin/` `ModelsDto.kt`, `TaskModelDto.kt`,
`ProviderDto.kt` (hot), `fm/api/TaskModelDtoMapper.kt`, `fm/api/ModelRoutes.kt`,
`server/feature-models/build.gradle.kts`, `server/app/src/main/kotlin/app/yap/server/Application.kt`
(hot), and their tests.

- [ ] 4.1 Add the model DTOs, the mapper that reads codes with `entries.find`, the routes, and mount them behind the admin key; verify with the mapper test and the route tests for `GET` 200 with the five tasks and the providers, a JSON with no key field, `PUT` 200, 400 for an unknown task, an unknown provider and a rejected save, and 401

## 5. [5/10] Server level model and the model of a call (about 310 lines)

Needs: part 2 with `V6` is merged. `V7` is reserved for this part.

Files: `server/feature-levels/src/main/resources/db/migration/V7__level_model.sql` (hot, Flyway),
`fl/persistence/LevelsTable.kt`, `fl/persistence/DatabaseLevelRepository.kt`,
`fl/model/LevelSettings.kt`, `fl/model/LevelTableValidity.kt`, `fl/api/LevelDtoMapper.kt`,
`server/feature-levels/build.gradle.kts` (`core-llm`),
`contract-admin/src/main/kotlin/app/yap/contract/admin/LevelSettingsDto.kt` (hot),
`server/feature-levels/src/testFixtures/` (`StubLevel`), `fm/usecase/GetTaskModelUseCase.kt`,
`fm/FeatureModelsModule.kt`, `server/feature-models/build.gradle.kts` (`feature-levels`), and their
tests.

- [ ] 5.1 Add the optional level model: the column, the upsert that also sets it, the read that gives no model for a stored model outside the catalog, the save rule for a model of the catalog, and `LevelSettingsDto.model` with the default `null`; verify with the model test for an unknown model, the mapper test, the route test for a level model, and the PostgreSQL tests for a save with a model, a second save without one, and a stored model outside the catalog that reads as no model (CI runs them)
- [ ] 5.2 Add `GetTaskModelUseCase(task, level)` with `TaskModelChoice` for the task row; verify with the use-case tests for a level without a model, a level model for Character reply and for Hints, a level model that Step check, Review and Calibration ignore, a stored task row outside the catalog that gives the default, and a save between two calls that changes the model of the second call

## 6. [6/10] Admin data layer (about 350 lines)

Files: `am/domain/` (`ModelTask`, `ModelChoice`, `TaskModel`, `Provider`, `Models`,
`SaveModelsResult`), `am/domain/repository/ModelRepository.kt`, `am/data/api/ModelApi.kt`,
`am/data/mapper/ModelMapper.kt`, `am/data/repository/RemoteModelRepository.kt`,
`am/domain/usecase/` (`GetModelsUseCase`, `SaveModelsUseCase`), `al/domain/LevelSettings.kt`,
`al/data/mapper/LevelMapper.kt`, `admin/src/test/.../feature/models/domain/StubModels.kt`,
`admin/src/test/.../feature/levels/domain/StubLevel.kt`, and their tests.

- [ ] 6.1 Add the admin models repository and its mapper; verify with mock engine tests for `GET` 200, `PUT` 200, 400, an unknown task code in the answer, a call without saved settings and a failed call
- [ ] 6.2 Add `GetModelsUseCase` and `SaveModelsUseCase`, and the level model in the levels mapper; verify with `RemoteLevelRepositoryTest` for a level with a model and without one. No use-case test: the use cases only delegate

## 7. [7/10] Admin Models view model (about 380 lines)

Before this part: run `ux-review` on the canvas Models screen. This part holds the decisions of
that review that change the state, and their scenarios in `specs/task-models/spec.md`.

Files: `am/presentation/ModelsViewModel.kt`, `am/presentation/ModelsUiStateMapper.kt`,
`am/presentation/ModelTaskTitles.kt`, `am/FeatureModelsModule.kt`,
`admin/src/main/kotlin/app/yap/admin/AppModules.kt`, `specs/task-models/spec.md` (the scenarios of
the review), stub use cases in the tests, and their tests.

- [ ] 7.1 Add `ModelsViewModel`, its UI state, its mapper and the Russian titles and when-lines of the tasks; verify with the mapper and view-model tests for the first open with the defaults, Opened again, a provider change that sets its first model, the Saved status and its reset on a change, the saving state, the rejected save, the save failure, the load error, a task missing in the answer, and each state decision of the `ux-review`

## 8. [8/10] Admin Models screen and the sidebar (about 330 lines)

This part reuses the state of part 7 and does not change it. Slice 1 holds 8.1 without the
captures. Slice 2 holds `ScreenCaptureTest.kt` and the images. Then run `ui-review`.

Files: `am/presentation/ui/ModelsScreen.kt`, `admin/src/main/kotlin/app/yap/admin/Main.kt` (hot),
`admin/src/main/kotlin/app/yap/admin/navigation/NavigationViewModel.kt` (hot, `Screen.Models`),
`navigation/NavigationUiStateMapper.kt` (hot), `navigation/ui/Sidebar.kt` (hot, Models in Settings),
`navigation/ui/AdminShell.kt` (hot), `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`, and
the navigation tests. #10 changes the same navigation files.

- [ ] 8.1 Add the Models screen with the sections Who does what and Providers, Save in the toolbar, and Models in the sidebar group Settings after Server; verify with the navigation view-model test for Models, `./gradlew build`, and the images of the screen capture for the loaded, saving, saved, rejected, Not connected and load error states
- [ ] 8.2 Owner check: run the admin app against a server with a database and a DeepSeek key; verify a first save, Connected, and the choices after a server restart

## 9. [9/10] Admin Levels Model column (about 300 lines)

Before this part: run `ux-review` on the canvas Levels Model column. After 9.1: run `ui-review`.

Files: `al/presentation/LevelsViewModel.kt`, `al/presentation/LevelsUiStateMapper.kt`,
`al/presentation/ui/LevelsScreen.kt`, `admin/src/test/.../feature/models/domain/usecase/StubGetModelsUseCase.kt`,
`admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`, and their tests.

- [ ] 9.1 Add the Model column to Levels: `LevelsViewModel` loads the levels and the models together, the form keeps the model of each level, and Save sends it; verify with the mapper and view-model tests for Default before a save, the choices in the server's order, a chosen model in the save, Default in the save, and the load error when the models do not load, and the images of the screen capture
- [ ] 9.2 Owner check: run the admin app against a server with a database; verify a level model after a save and after a server restart

## 10. [10/10] Archive

Files: `openspec/changes/add-task-models/`, `openspec/specs/task-models/spec.md`,
`openspec/specs/rating-levels/spec.md`, `openspec/changes/archive/`.

- [ ] 10.1 Run `/opsx:archive` as the last commit of the last PR for #11, so `openspec/specs/` updates at the merge; verify that `openspec/specs/task-models/spec.md` and `openspec/specs/rating-levels/spec.md` hold the requirements of this change, with `openspec validate --strict`
