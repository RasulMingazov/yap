# Proposal

## Why

The owner must choose the provider and the model for each task of the conversation without a
release, so that other providers can be tried later (#11). A rating level must be able to use its
own model for the replies and the hints.

## What Changes

- The server knows five tasks: Character reply, Step check, Hints, Review and Calibration.
- The server knows a catalog of providers and their models. Now it is DeepSeek with
  `deepseek-flash` and `deepseek-v4-pro`. The default of each task is DeepSeek `deepseek-flash`
  (see `design.md`: the issue names `deepseek-chat`, which DeepSeek discontinued).
- The owner saves a provider and a model for every task at once. The server accepts only a model of
  the catalog for its provider.
- A level of #9 can override the model. The override applies to Character reply and Hints, the
  tasks that use `{level.rules}`. Without an override, the model of the task applies.
- The server resolves the model of a task for a level at each call, so a saved change applies to
  the next call.
- A provider is Connected when the server has its key. The server reads `DEEPSEEK_API_KEY` as an
  optional setting. The admin API never returns a key.
- The admin API reads the tasks with their models and the providers, and saves the tasks, behind
  the admin key.
- The admin app gets the Models screen in the sidebar group Settings: the section Who does what
  with Task, Provider and Model, and the section Providers with the Connected state.
- The Levels screen gets the Model column: Default or a model of the catalog.
- The states of the UX review of the design: Not connected with the next step and a mark on its
  task rows, a status with Refresh when the catalog changed before a save (409), the loader, task
  lines shared with Prompts, and Levels that still shows its table when the models do not load.

Out of scope:

- The calls to the model, and the end-to-end check that a changed model is used by the next call:
  #12 (reply and step check), #13 (hints), #14 (review). They call the resolve use case of this
  change.
- Adding a provider from the admin app. Keys stay in the Timeweb panel. The canvas button Connect a
  provider is not built.
- Prices per million tokens and model comparison. The canvas price line and the comparison note are
  not built.
- Calibration engine: no issue yet. It uses the Calibration task model when it lands.

## Capabilities

### New Capabilities

- `task-models`: the tasks, the provider catalog, the model of each task, the level override, the
  admin API that serves them, and the Models screen.

### Modified Capabilities

- `rating-levels`: a level has an optional model, the save checks it, and the Levels table has the
  Model column.

## Impact

- A new module `server/feature-models` with `V6__task_models.sql`. `server/feature-levels` gets
  `V7__level_model.sql`. Both versions are reserved for #11, and they merge after the `V5` of #8.
- `server/core-llm` gets the provider catalog and the optional DeepSeek key setting. `server/app`
  reads `DEEPSEEK_API_KEY`.
- `contract-admin` gets the model DTOs, and `LevelSettingsDto` gets an optional `model`.
- `admin` gets the Models screen and its data layer, and the Model column in Levels.
