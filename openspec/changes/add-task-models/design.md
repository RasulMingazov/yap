# Design

## Context

- `server/core-llm` has `ModelProvider`, `ModelRequest(messages, model)` and the DeepSeek adapter
  with `DeepSeekConfig(apiKey)`. No module calls a model yet, and `server/app` does not read a
  DeepSeek key.
- `server/feature-levels` (#9) saves the seven levels with an upsert by `code`. Its design reserves
  the nullable `levels.model` for this change.
- `server/feature-prompts` (#10) names the prompts `TOPIC_REPLY`, `TOPIC_CHECK`, `TOPIC_HINTS`,
  `TOPIC_REVIEW`, `CALIBRATION_CHECK` and `CALIBRATION_REPLY`.
- The admin app has `AdminSelect`, `CellTextField`, `ToolbarSaveAction`, `Note`, `StatusText` and
  the table cells of Levels. The sidebar group Settings holds Server.

## Goals / Non-Goals

**Goals:**

- A choice of provider and model per task that the owner changes without a release.
- One rule that gives the model of a call: #12, #13 and #14 call it.
- The Connected state of each provider without a key on the wire.

**Non-Goals:**

- A call to a model, a router of providers, or a second adapter. #12 builds the first call.
- Prices, model comparison, adding a provider from the admin app.

## Decisions

### The default model is `deepseek-flash`, not `deepseek-chat`

The issue and the canvas name `deepseek-chat`. DeepSeek scheduled `deepseek-chat` and
`deepseek-reasoner` for discontinuation on 2026-07-24 (change log, 2026-04-24 entry). Its pricing
page now lists only `deepseek-flash` (DeepSeek-V4.1-Flash) and `deepseek-v4-pro`. The core-llm
tests already use `deepseek-flash`.

The exact API ids, checked on 2026-11-08: the `model` row of the PARAM/VALUE table on the pricing
page lists `deepseek-flash` and `deepseek-v4-pro`. The two names do not share one pattern; the
catalog copies them as DeepSeek writes them. The footnote of that row says that the legacy names
`deepseek-v4-flash` and `deepseek-v4-flash-vision-exp` are still accepted but their models are
retired. The catalog does not list legacy names.

| Option | Price per 1M tokens, cache miss in / out (peak) | Choice |
| --- | --- | --- |
| `deepseek-chat` | discontinued | no |
| `deepseek-flash` | $0.30 / $1.20 | default of every task |
| `deepseek-v4-pro` | $1.32 / $3.96 | in the catalog, the owner opts in |

The default keeps the cost low: Pro costs about 3 to 4 times more per token. Sources:
<https://api-docs.deepseek.com/quick_start/pricing>, <https://api-docs.deepseek.com/updates>.

### The catalog lives in `core-llm`

`core-llm` owns the providers, so it owns the list of what each adapter serves.

```text
ModelProviderId = DEEPSEEK                       (enum; the wire code is the enum name)
ModelCatalog: DEEPSEEK -> name "DeepSeek", models ["deepseek-flash", "deepseek-v4-pro"]
ModelChoice(provider: ModelProviderId, model: String)
ModelCatalog.DEFAULT = ModelChoice(DEEPSEEK, "deepseek-flash")
```

- A model name belongs to one provider. A catalog test checks it. So a level stores only the
  model, and the server finds its provider in the catalog. This keeps the one column `levels.model`
  that #9 reserved.
- `feature-levels` and `feature-models` depend on `core-llm` for the catalog. Core modules never
  depend on features, so there is no cycle.
- A new model or provider is a code change with its adapter. The key goes to the Timeweb panel.

### Connected

- `core-llm` gets `LlmConfig(deepSeek: DeepSeekConfig?)` and `coreLlmModule(config)`, which binds
  `ProviderConnections`: `isConnected(provider)`.
- `app` reads `DEEPSEEK_API_KEY`. A missing or blank value gives `deepSeek = null`. It stays
  optional in every environment: the server starts without it and shows Not connected.
- `.env.example` gets an empty `DEEPSEEK_API_KEY=` line with a comment. No value goes into the
  repository.
- #12 builds `DeepSeekModelProvider` from the same `LlmConfig`.

### The module `server/feature-models`

```text
ModelTask = TOPIC_REPLY | TOPIC_CHECK | TOPIC_HINTS | TOPIC_REVIEW | CALIBRATION
            (enum, the order of the table; dependsOnLevel = TOPIC_REPLY, TOPIC_HINTS)
TaskModel(task, choice: ModelChoice)
ModelsOverview(tasks: List<TaskModel>, providers: List<ProviderState(id, connected)>)
            (the name, the models and keySetting come from ModelProviderId)
SaveTaskModelsOutcome = Invalid | CatalogChanged | Saved(ModelsOverview)
```

- One rule in `model/`, `TaskModelTableValidity`, checks a full save: each task once (`Invalid`),
  and each choice in the catalog (`CatalogChanged`).
- A second rule in `model/`, `TaskModelChoice`, gives the choice of a task from its stored row: the
  row when it is in the catalog, else `ModelCatalog.DEFAULT`. Two use cases need it, so it lives in
  `model/`, as `docs/server/001-structure.md` says. `TaskModelChoiceTest` covers no row, a row in
  the catalog and a row outside it.
- `GetModelsUseCase` reads the rows, gives each task its choice with `TaskModelChoice`, and adds the
  providers with `ProviderConnections`.
- `SaveModelsUseCase` checks the rule, saves, and returns the overview.
- `GetTaskModelUseCase(task, level: LevelCode): ModelChoice` is the rule of a call. For a task that
  depends on the level, it reads the level through `GetLevelsUseCase` of `feature-levels` and uses
  its model when it has one. Else it uses `TaskModelChoice` on the task row. It reads at each call
  and caches nothing, so the next call sees a save.
- `feature-models` depends on `feature-levels` through its use case. `feature-levels` does not
  depend on `feature-models`.

### The level override applies to Character reply and Hints

| Option | For | Against | Choice |
| --- | --- | --- | --- |
| Every task of a learner on the level | one rule | Step check and Review feed the rating; a different judge per level makes the scores unfair | no |
| The tasks that use `{level.rules}`: Character reply, Hints | follows the Levels note: step check and review do not depend on the level | Calibration uses `{level.rules}` too, but has its own row | yes |

### The tables

```sql
-- V6__task_models.sql, server/feature-models
create table task_models (
    task text primary key,
    provider text not null,
    model text not null
);

-- V7__level_model.sql, server/feature-levels
alter table levels add column model text;
```

- No seed rows: a missing row is the default.
- A save upserts the five rows by `task` in one transaction. It never deletes a row.
- The level upsert now also sets `model`, so Default clears it.
- The level read gives no model when the stored model is not in the catalog. So Levels shows
  Default, and the next save sends no model and passes the save rule. `GetTaskModelUseCase` then
  needs no own check of a level model.
- Each feature owns its table, so the change has two migrations: `V6` and `V7`, both reserved for
  #11.
- `V7` is a new version, not a column added to `V3__levels.sql`. An edit of the applied `V3`
  changes its checksum, and with the reset flag the deploy clears the database and the levels that
  the owner saved. A new version keeps them. The run also forbids edits of `V2` to `V5`.
- Migration order: the part with `V6` merges only after the `V5` part of #8 is in `develop`, and
  the part with `V7` after the part with `V6`. With the reset flag, a version that lands below an
  applied one fails the validation and clears the database.

### The wire contract

- `GET /admin/models` returns `ModelsDto(tasks: List<TaskModelDto>, providers: List<ProviderDto>)`.
- `TaskModelDto(task, provider, model)`: strings; the codes are the enum names.
- `ProviderDto(code, name, models: List<String>, connected: Boolean, keySetting: String)`.
  `keySetting` is the name of the setting, `DEEPSEEK_API_KEY`, for the next step of Not connected.
  No key field. The name comes from `core-llm`, so the admin app holds no provider facts.
- `PUT /admin/models` takes a JSON array of `TaskModelDto` and returns `ModelsDto`. 400 for an
  unknown task code (`entries.find`, as in levels) and for a task missing or twice. 409 for a
  provider or a model outside the catalog: an unknown provider code gives no table in the mapper,
  and the route answers 409 before the save rule, also when a task is missing. So the admin app can
  tell a changed catalog from a broken form.
- `LevelSettingsDto` gets `model: String? = null`. The default keeps old clients valid.
- `PUT /admin/levels` answers 409 for a level model outside the catalog
  (`SaveLevelsOutcome.ModelUnavailable`), and 400 as before for the other rules.

### The admin app

- `feature/models` has the Ktorfit API, the remote repository, `GetModelsUseCase`,
  `SaveModelsUseCase` and `ModelsViewModel`. `ModelTaskTitles.kt` takes the title and the usage
  line of the four topic tasks from `PromptKey.title()` and `PromptKey.usage()` of
  `feature/prompts`, so the two screens have one source. Calibration takes
  `PromptGroup.CALIBRATION.title()` and its own line.
- The form keeps the choice of each task. A change of provider sets the first model of that
  provider from the server's list. Save sends the five tasks. A 400 gives the error beside Save.
- `LevelsViewModel` also calls `GetModelsUseCase` of `feature/models` for the Model choices. The two
  loads run together and have their own states. A failed levels load is the screen error. A failed
  models load keeps the table, shows Default disabled in Model, and shows its own error with Retry,
  which reloads only the models. The level form gets `model: String?`; without the models, Save
  sends the stored model, so a failed load never clears a level model.
- The UX review of the design (`ux-review`, decisions M1 to M6, m7 and m8 of the orchestrator)
  sets these states:
  - Models: Not connected in the warning tone with the step to add the key setting, and a mark on
    each task row with a Not connected provider (M1). A 409 on Save gives the status that the
    model list on the server changed, with Refresh, which reloads (M5). The loader while loading
    (m7). The task lines from the prompts (m8).
  - Levels: one name Default on both screens, and the table note on Character reply, Hints and
    Default (M2). The No rating row names the calibration model on Models (M3). The models error
    without a blocked screen (M4). A 409 on Save gives the status that a level model is no longer
    available, with Refresh (M6).
- `Screen.Models` goes into the group Settings after Server. #10 adds Prompts to the group Rules at
  the same time in `Sidebar.kt` and `NavigationViewModel.kt`.
- The toolbar uses `ToolbarSaveAction`. The selects use `AdminSelect`.

### Deviations from the canvas

- `deepseek-flash` instead of `deepseek-chat` (see above).
- No price line under Model, no Connect a provider button, and no comparison sentence in the note.
  The issue puts them out of scope.
- The note under the Who does what table names Character reply and Hints. The canvas says the level model
  works wherever it is set.
- The Levels Model choices show the model name, as the canvas does; Default for no model.
- The UX states M1 to M6 and m7 are not in the canvas. The canvas update is for the owner (m9).

## Risks / Trade-offs

- [`GetTaskModelUseCase` has no caller until #12] → Same as `GetLevelRulesUseCase` of #9. Its tests
  cover the issue's «a changed model is used by the next call»; #12 owns the end-to-end check.
- [A task can point to a provider without a key] → Save accepts it so a local run without a key
  works. Only DeepSeek exists. #12 decides what a call does without a key.
- [A model leaves the catalog] → A stored task choice reads as the default, and a stored level
  model reads as no model, so a call never gets an unknown name and a Levels save never fails on
  it.
- [Cost] → Pro costs about 3 to 4 times more. The owner chooses it per task or per level.
