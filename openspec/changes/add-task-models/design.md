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
ModelsOverview(tasks: List<TaskModel>, providers: List<ProviderState(id, name, models, connected)>)
SaveTaskModelsOutcome = Invalid | Saved(ModelsOverview)
```

- One rule in `model/`, `TaskModelTableValidity`, checks a full save: each task once, and each
  choice in the catalog.
- `GetModelsUseCase` reads the rows, fills a missing or unknown row with `ModelCatalog.DEFAULT`, and
  adds the providers with `ProviderConnections`.
- `SaveModelsUseCase` checks the rule, saves, and returns the overview.
- `GetTaskModelUseCase(task, level: LevelCode): ModelChoice` is the rule of a call. For a task that
  depends on the level, it reads the level through `GetLevelsUseCase` of `feature-levels` and uses
  its model when it has one. Else it uses the task row, or the default. It reads at each call and
  caches nothing, so the next call sees a save.
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
- Each feature owns its table, so the change has two migrations. The orchestrator reserved `V6`;
  `V7` needs its reservation (see Risks).

### The wire contract

- `GET /admin/models` returns `ModelsDto(tasks: List<TaskModelDto>, providers: List<ProviderDto>)`.
- `TaskModelDto(task, provider, model)`: strings; the codes are the enum names.
- `ProviderDto(code, name, models: List<String>, connected: Boolean)`. No key field.
- `PUT /admin/models` takes a JSON array of `TaskModelDto` and returns `ModelsDto`. 400 for an
  unknown task or provider code (`entries.find`, as in levels), and for `Invalid`.
- `LevelSettingsDto` gets `model: String? = null`. The default keeps old clients valid.

### The admin app

- `feature/models` has the Ktorfit API, the remote repository, `GetModelsUseCase`,
  `SaveModelsUseCase` and `ModelsViewModel`. The titles and the when-lines of the tasks are Russian
  copy in `ModelTaskTitles.kt`, as `PromptTitles.kt` does for the prompts.
- The form keeps the choice of each task. A change of provider sets the first model of that
  provider from the server's list. Save sends the five tasks. A 400 gives the error beside Save.
- `LevelsViewModel` also calls `GetModelsUseCase` of `feature/models` for the Model choices. The two
  loads run together; either failure is the load error. The level form gets `model: String?`.
- `Screen.Models` goes into the group Settings after Server. #10 adds Prompts to the group Rules at
  the same time in `Sidebar.kt` and `NavigationViewModel.kt`.
- The toolbar uses `ToolbarSaveAction`. The selects use `AdminSelect`.

### Deviations from the canvas

- `deepseek-flash` instead of `deepseek-chat` (see above).
- No price line under Model, no Connect a provider button, and no comparison sentence in the note.
  The issue puts them out of scope.
- The note under the tasks table names Character reply and Hints. The canvas says the level model
  works wherever it is set.
- The Levels Model choices show the model name, as the canvas does; Default for no model.

## Risks / Trade-offs

- [`V7` is not reserved] → The plan asks the orchestrator. Fallback: the part with the level column
  waits until `V7` is free, or both statements go into `V6` of `feature-models` with a note.
- [`GetTaskModelUseCase` has no caller until #12] → Same as `GetLevelRulesUseCase` of #9. Its tests
  cover the issue's «a changed model is used by the next call»; #12 owns the end-to-end check.
- [A task can point to a provider without a key] → Save accepts it so a local run without a key
  works. Only DeepSeek exists. #12 decides what a call does without a key.
- [A model leaves the catalog] → A stored task choice reads as the default, and the call rule skips
  a level model that is not in the catalog, so a call never gets an unknown name.
- [Cost] → Pro costs about 3 to 4 times more. The owner chooses it per task or per level.
