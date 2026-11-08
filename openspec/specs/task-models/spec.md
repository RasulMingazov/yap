# task-models Specification

## Purpose
The owner chooses the provider and the model of each model task on the server, without a release,
so that other providers can be tried later.

## Requirements

### Requirement: Five tasks

The server SHALL know five tasks, in this order: Character reply, Step check, Hints, Review and
Calibration. Calibration SHALL cover the answer check and Sam's reply of the calibration. The owner
SHALL NOT add or remove a task.

#### Scenario: The tasks

- **WHEN** the admin app reads the models
- **THEN** the server returns the five tasks in this order

### Requirement: The provider catalog

The server SHALL know a catalog of providers, each with a name and its models in a fixed order. The
catalog SHALL have DeepSeek with `deepseek-flash` and `deepseek-v4-pro`. A model name SHALL belong
to one provider only.

#### Scenario: The providers

- **WHEN** the admin app reads the models
- **THEN** the server returns DeepSeek with `deepseek-flash` and `deepseek-v4-pro`

### Requirement: A provider is connected when the server has its key

A provider SHALL be Connected when the server has a key for it that is not blank. The admin API
SHALL return the name of the setting that holds the key of each provider, such as
`DEEPSEEK_API_KEY`, and SHALL NOT return a key.

#### Scenario: The server has the key

- **WHEN** the server runs with a DeepSeek key and the admin app reads the models
- **THEN** DeepSeek is Connected, the answer names the setting `DEEPSEEK_API_KEY`, and it holds no
  key

#### Scenario: The server has no key

- **WHEN** the server runs without a DeepSeek key and the admin app reads the models
- **THEN** DeepSeek is not Connected

### Requirement: Each task has a provider and a model

Each task SHALL have a provider and a model of that provider. A task without a saved choice SHALL
have DeepSeek `deepseek-flash`. A saved choice that is no longer in the catalog SHALL read as no
choice.

#### Scenario: Before a save

- **WHEN** the admin app reads the models before the owner saved them
- **THEN** every task has DeepSeek `deepseek-flash`

#### Scenario: A saved choice

- **WHEN** the owner saves Review with DeepSeek `deepseek-v4-pro`
- **THEN** the server returns Review with DeepSeek `deepseek-v4-pro` and the other tasks as saved

#### Scenario: A stored choice that left the catalog

- **WHEN** Review has a stored model that the provider catalog no longer has
- **THEN** the server returns Review with DeepSeek `deepseek-flash`, and Review calls use it

### Requirement: The owner saves every task at once

Save SHALL replace the choices of all five tasks. The server SHALL reject the save when a task is
missing or comes twice, when a provider is not in the catalog, or when a model is not a model of its
provider. A rejected save SHALL change nothing. Save SHALL accept a provider that is not Connected.

#### Scenario: A save without Hints

- **WHEN** the owner saves the tasks without Hints
- **THEN** the server rejects the save and keeps the choices as they were

#### Scenario: A model of another provider

- **WHEN** the owner saves a task with a model that is not a model of its provider
- **THEN** the server rejects the save and keeps the choices as they were

#### Scenario: A provider without a key

- **WHEN** the server has no DeepSeek key and the owner saves every task with DeepSeek
- **THEN** the server saves the choices

### Requirement: The model of a call

For a call of a task for a level, the server SHALL use the model of that level when the level has
one and the task is Character reply or Hints. In every other case it SHALL use the model of the
task. The server SHALL read the choices at each call, so a saved change applies to the next call.

#### Scenario: A level without a model

- **WHEN** B1 has no model and Character reply has DeepSeek `deepseek-flash`
- **THEN** a Character reply call for B1 uses DeepSeek `deepseek-flash`

#### Scenario: A level with a model

- **WHEN** B1 has the model `deepseek-v4-pro` and Hints has DeepSeek `deepseek-flash`
- **THEN** a Hints call for B1 uses DeepSeek `deepseek-v4-pro`

#### Scenario: A task that does not depend on the level

- **WHEN** B1 has the model `deepseek-v4-pro` and Step check has DeepSeek `deepseek-flash`
- **THEN** a Step check call for B1 uses DeepSeek `deepseek-flash`

#### Scenario: A changed model

- **WHEN** the owner saves Review with `deepseek-v4-pro` after a Review call with `deepseek-flash`
- **THEN** the next Review call uses `deepseek-v4-pro`

### Requirement: Task models stay after a restart

The server SHALL keep the choices of the tasks in its database.

#### Scenario: A restart

- **WHEN** the server restarts after the owner saved the tasks
- **THEN** the admin API returns the same choices

### Requirement: The admin API serves the models

Behind the admin key, the server SHALL return the five tasks with their choices and the providers
with their models and their Connected state at `GET /admin/models`. It SHALL replace the choices at
`PUT /admin/models` and answer with the same content as `GET`. A save with a provider or a model
that is not in the catalog SHALL get 409. A save with a task missing, twice or unknown SHALL get
400.

#### Scenario: No admin key

- **WHEN** a call to the models API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: A model that is not in the catalog

- **WHEN** the admin app saves the tasks with a model that is not in the catalog
- **THEN** the server answers 409 and changes nothing

#### Scenario: A save without a task

- **WHEN** the admin app saves the tasks without Hints
- **THEN** the server answers 400 and changes nothing

#### Scenario: An unknown task

- **WHEN** the admin app saves a task code that the server does not know
- **THEN** the server answers 400

### Requirement: The sidebar opens the models

The sidebar SHALL show Models in the group Settings.

#### Scenario: Models in the sidebar

- **WHEN** the owner clicks Models in the sidebar
- **THEN** the admin app opens the Models screen

### Requirement: The Models screen

Models SHALL show the subtitle that it sets which model does which task. The section Who does what
SHALL have a table with a row for each task: Task with a line on when the task runs, Provider and
Model.
A note below the table SHALL say that a level can set its own model for Character reply and Hints
in Levels.

#### Scenario: The first open

- **WHEN** the owner opens Models before any save
- **THEN** the table shows the five tasks with DeepSeek and `deepseek-flash`

#### Scenario: Models loads

- **WHEN** the owner opens Models and the server has not answered yet
- **THEN** Models shows the loader and no table

#### Scenario: The task lines

- **WHEN** the owner opens Models
- **THEN** Character reply, Step check, Hints and Review show the same title and usage line as
  their prompts on Prompts, and Calibration shows its own line on the check and Sam's replies

#### Scenario: Models opens again

- **WHEN** the owner opens Models again
- **THEN** the admin app loads the models from the server again

#### Scenario: The models do not load

- **WHEN** the admin app cannot load the models
- **THEN** Models shows an error that the models did not load, and Retry

#### Scenario: A task is missing in the answer

- **WHEN** the server returns the models without one of the five tasks
- **THEN** Models shows an error that the models did not load, and Retry

### Requirement: The choices of a task row

Provider SHALL offer the providers of the catalog. Model SHALL offer the models of the chosen
provider. When the owner chooses another provider, Model SHALL take the first model of that
provider.

#### Scenario: The models of the provider

- **WHEN** the owner opens Model of a row with DeepSeek
- **THEN** it offers `deepseek-flash` and `deepseek-v4-pro`

### Requirement: The Providers section

The section Providers SHALL list each provider with its name, its models and Connected or Not
connected. Not connected SHALL show in the warning tone with the next step: add the key setting
that the server names to the server settings. A note SHALL say that the keys are on the server and
the admin app does not show them.

#### Scenario: A connected provider

- **WHEN** the server has the DeepSeek key and the owner opens Models
- **THEN** Providers shows DeepSeek, its two models and Connected

#### Scenario: A provider without a key

- **WHEN** the server has no DeepSeek key and the owner opens Models
- **THEN** Providers shows DeepSeek and Not connected in the warning tone, with the step to add
  `DEEPSEEK_API_KEY` to the server settings

#### Scenario: A task with a provider without a key

- **WHEN** the server has no DeepSeek key and a task has DeepSeek
- **THEN** the row of that task shows a short Not connected mark

### Requirement: Saving the models

The toolbar of Models SHALL have Save with the save status. Save SHALL send every task to the
server. When the server accepts them, the admin app SHALL show the models that the server returns
and a Saved status.

#### Scenario: The save succeeds

- **WHEN** the owner chooses `deepseek-v4-pro` for Review and clicks Save
- **THEN** the table shows the choices from the server and the Saved status shows beside Save

#### Scenario: A change after a save

- **WHEN** the owner changes a choice after the Saved status shows
- **THEN** the Saved status goes away

#### Scenario: The save is in progress

- **WHEN** the owner clicks Save and the server has not answered yet
- **THEN** a saving status shows beside Save, and Save and the choices are disabled

#### Scenario: The server rejects the save

- **WHEN** the server rejects the tasks with 400
- **THEN** the admin app shows an error beside Save that asks to choose a model of the provider for
  every task, and keeps the form as it is

#### Scenario: The catalog changed before the save

- **WHEN** the server rejects the tasks with 409
- **THEN** the admin app shows a status beside Save that the model list on the server changed,
  with Refresh

#### Scenario: Refresh after a changed catalog

- **WHEN** the owner clicks Refresh beside Save
- **THEN** the admin app loads the models again and shows the choices from the server

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save
- **THEN** it keeps the form as it is and shows a snackbar that the save failed
