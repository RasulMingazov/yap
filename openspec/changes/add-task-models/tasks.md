# Tasks

One group is one PR, in merge order. (hot) marks a hot file. `fm` is
`server/feature-models/src/main/kotlin/app/yap/server/feature/models`, `fl` is
`server/feature-levels/src/main/kotlin/app/yap/server/feature/levels`, `llm` is
`server/core-llm/src/main/kotlin/app/yap/server/core/llm`, `am` is
`admin/src/main/kotlin/app/yap/admin/feature/models`, `al` is
`admin/src/main/kotlin/app/yap/admin/feature/levels`. Tests mirror them. An owner check needs a
server with a database: it cannot run on the agent machine.

## 1. [1/9] The plan

- [x] 1.1 Propose `add-task-models` in `openspec/changes/add-task-models/`; verify with `openspec validate add-task-models --strict`

## 2. [2/9] Server catalog and task model persistence (about 300 lines)

Files: `llm/ModelProviderId.kt`, `llm/ModelChoice.kt`, `llm/ModelCatalog.kt`, `settings.gradle.kts`
(hot), `server/feature-models/build.gradle.kts`, `fm/model/` (`ModelTask`, `TaskModel`,
`TaskModelTableValidity`), `fm/persistence/` (`TaskModelRepository`, `TaskModelsTable`,
`DatabaseTaskModelRepository`), `server/feature-models/src/main/resources/db/migration/V6__task_models.sql`
(hot, Flyway), `server/feature-models/src/testFixtures/` (`StubTaskModel`), and their tests.

- [ ] 2.1 Add the catalog in `core-llm`: DeepSeek with `deepseek-flash` and `deepseek-v4-pro`, and the default; verify with `ModelCatalogTest`: the order, the default, and each model name in one provider only
- [ ] 2.2 Add `server/feature-models` with `ModelTask` and the save rule; verify with the model tests for the five tasks in order, a missing task, a task twice, an unknown model and a model of another provider
- [ ] 2.3 Add `V6__task_models.sql` and the Exposed repository that saves with an upsert by `task`; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 3. [3/9] Server Connected and the model use cases (about 290 lines)

Files: `llm/LlmConfig.kt`, `llm/ProviderConnections.kt`, `llm/CoreLlmModule.kt`,
`server/core-llm/build.gradle.kts` (Koin), `server/app/.../config/AppConfig.kt`,
`server/app/.../config/AppConfigLoader.kt`, `server/app/.../AppModules.kt`,
`server/app/build.gradle.kts`, `.env.example`, `server/app/src/test/.../StubAppConfig.kt`,
`server/app/src/test/.../config/AppConfigLoaderTest.kt`, `fm/model/` (`ModelsOverview`,
`ProviderState`, `SaveTaskModelsOutcome`), `fm/usecase/` (`GetModelsUseCase`, `SaveModelsUseCase`),
`fm/FeatureModelsModule.kt`, `server/feature-models/src/testFixtures/` (`StubTaskModelRepository`),
and their tests.

- [ ] 3.1 Read `DEEPSEEK_API_KEY` as an optional setting into `LlmConfig`, and bind `ProviderConnections` in `coreLlmModule(config)`; add an empty `DEEPSEEK_API_KEY=` line to `.env.example`; verify with `AppConfigLoaderTest` for a key, no key and a blank key, the `ProviderConnections` test, and `AppModulesTest`
- [ ] 3.2 Add `GetModelsUseCase` and `SaveModelsUseCase`; verify with the use-case tests for the defaults before a save, a row that is not in the catalog, Connected with and without a key, a rejected save that saves nothing, and a save with a provider without a key

## 4. [4/9] Server admin API (about 330 lines)

Files: `contract-admin/src/main/kotlin/app/yap/contract/admin/` `ModelsDto.kt`, `TaskModelDto.kt`,
`ProviderDto.kt` (hot), `fm/api/TaskModelDtoMapper.kt`, `fm/api/ModelRoutes.kt`,
`server/feature-models/build.gradle.kts`, `server/app/src/main/kotlin/app/yap/server/Application.kt`
(hot), and their tests.

- [ ] 4.1 Add the model DTOs, the mapper that reads codes with `entries.find`, the routes, and mount them behind the admin key; verify with the mapper test and the route tests for `GET` 200 with the five tasks and the providers, a JSON with no key field, `PUT` 200, 400 for an unknown task, an unknown provider and a rejected save, and 401

## 5. [5/9] Server level model and the model of a call (about 300 lines)

Files: `server/feature-levels/src/main/resources/db/migration/V7__level_model.sql` (hot, Flyway;
needs the orchestrator's `V7`), `fl/persistence/LevelsTable.kt`,
`fl/persistence/DatabaseLevelRepository.kt`, `fl/model/LevelSettings.kt`,
`fl/model/LevelTableValidity.kt`, `fl/api/LevelDtoMapper.kt`, `server/feature-levels/build.gradle.kts`
(`core-llm`), `contract-admin/src/main/kotlin/app/yap/contract/admin/LevelSettingsDto.kt` (hot),
`server/feature-levels/src/testFixtures/` (`StubLevel`), `fm/usecase/GetTaskModelUseCase.kt`,
`fm/FeatureModelsModule.kt`, `server/feature-models/build.gradle.kts` (`feature-levels`), and their
tests.

- [ ] 5.1 Add the optional level model: the column, the upsert that also sets it, the save rule for a model of the catalog, and `LevelSettingsDto.model` with the default `null`; verify with the model test for an unknown model, the mapper test, the route test for a level model, and the PostgreSQL tests for a save with a model and a second save without one (CI runs them)
- [ ] 5.2 Add `GetTaskModelUseCase(task, level)`; verify with the use-case tests for a level without a model, a level model for Character reply and for Hints, a level model that Step check, Review and Calibration ignore, a level model that is not in the catalog, and a save between two calls that changes the model of the second call

## 6. [6/9] Admin data layer (about 350 lines)

Files: `am/domain/` (`ModelTask`, `ModelChoice`, `TaskModel`, `Provider`, `Models`,
`SaveModelsResult`), `am/domain/repository/ModelRepository.kt`, `am/data/api/ModelApi.kt`,
`am/data/mapper/ModelMapper.kt`, `am/data/repository/RemoteModelRepository.kt`,
`am/domain/usecase/` (`GetModelsUseCase`, `SaveModelsUseCase`), `al/domain/LevelSettings.kt`,
`al/data/mapper/LevelMapper.kt`, `admin/src/test/.../feature/models/domain/StubModels.kt`,
`admin/src/test/.../feature/levels/domain/StubLevel.kt`, and their tests.

- [ ] 6.1 Add the admin models repository and its mapper; verify with mock engine tests for `GET` 200, `PUT` 200, 400, an unknown task code in the answer, a call without saved settings and a failed call
- [ ] 6.2 Add `GetModelsUseCase` and `SaveModelsUseCase`, and the level model in the levels mapper; verify with `RemoteLevelRepositoryTest` for a level with a model and without one. No use-case test: the use cases only delegate

## 7. [7/9] Admin Models view model (about 380 lines)

Files: `am/presentation/ModelsViewModel.kt`, `am/presentation/ModelsUiStateMapper.kt`,
`am/presentation/ModelTaskTitles.kt`, `am/FeatureModelsModule.kt`,
`admin/src/main/kotlin/app/yap/admin/AppModules.kt`, stub use cases in the tests, and their tests.

- [ ] 7.1 Add `ModelsViewModel`, its UI state, its mapper and the Russian titles and when-lines of the tasks; verify with the mapper and view-model tests for the first open with the defaults, Opened again, a provider change that sets its first model, the Saved status and its reset on a change, the saving state, the rejected save, the save failure, the load error, and a task missing in the answer

## 8. [8/9] Admin Models screen and the sidebar (about 350 lines)

Files: `am/presentation/ui/ModelsScreen.kt`, `admin/src/main/kotlin/app/yap/admin/Main.kt` (hot),
`admin/src/main/kotlin/app/yap/admin/navigation/NavigationViewModel.kt` (hot, `Screen.Models`),
`navigation/NavigationUiStateMapper.kt` (hot), `navigation/ui/Sidebar.kt` (hot, Models in Settings),
`navigation/ui/AdminShell.kt` (hot), `admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`, and
the navigation tests. #10 changes the same navigation files.

Run `ux-review` on the canvas Models screen first. Slice 1 holds the decisions of that review that
change the state: `ModelsViewModel`, `ModelsUiStateMapper`, their tests and the scenarios in
`specs/task-models/spec.md`. Slice 2 holds 8.1 without the captures. Slice 3 holds
`ScreenCaptureTest.kt` and the images. Then run `ui-review`.

- [ ] 8.1 Add the Models screen with Save in the toolbar, and Models in the sidebar group Settings after Server; verify with the navigation view-model test for Models, `./gradlew build`, and the images of the screen capture for the loaded, saving, saved, rejected, Not connected and load error states
- [ ] 8.2 Owner check: run the admin app against a server with a database and a DeepSeek key; verify a first save, Connected, and the choices after a server restart

## 9. [9/9] Admin Levels Model column and the archive (about 300 lines)

Files: `al/presentation/LevelsViewModel.kt`, `al/presentation/LevelsUiStateMapper.kt`,
`al/presentation/ui/LevelsScreen.kt`, `admin/src/test/.../feature/models/domain/usecase/StubGetModelsUseCase.kt`,
`admin/src/test/kotlin/app/yap/admin/ScreenCaptureTest.kt`, their tests, then
`openspec/specs/task-models/spec.md`, `openspec/specs/rating-levels/spec.md` and
`openspec/changes/archive/`.

Run `ux-review` on the canvas Levels Model column first, and `ui-review` after 9.1.

- [ ] 9.1 Add the Model column to Levels: `LevelsViewModel` loads the levels and the models together, the form keeps the model of each level, and Save sends it; verify with the mapper and view-model tests for Default before a save, the choices in the server's order, a chosen model in the save, Default in the save, and the load error when the models do not load, and the images of the screen capture
- [ ] 9.2 Owner check: run the admin app against a server with a database; verify a level model after a save and after a server restart
- [ ] 9.3 Run `/opsx:archive` as the last commit of this PR; verify that `openspec/specs/task-models/spec.md` and `openspec/specs/rating-levels/spec.md` hold the requirements of this change, with `openspec validate --strict`
