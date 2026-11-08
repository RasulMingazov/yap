# Spec Delta

## MODIFIED Requirements

### Requirement: A level has its speech settings

Each level SHALL have a rating from, a vocabulary, the maximum words in a sentence, an idioms
setting, a prompt block and an optional model. No rating SHALL have no rating from. The idioms
setting SHALL be one of Never, Rarely, Sometimes and Freely. A level without a model SHALL use the
models of the tasks.

#### Scenario: A saved level keeps its values

- **WHEN** the owner saves B1 with the rating from 1050, the vocabulary B1, 16 words, Rarely and a
  block
- **THEN** the server returns B1 with these values

#### Scenario: A saved level keeps its model

- **WHEN** the owner saves B1 with the model `deepseek-v4-pro`
- **THEN** the server returns B1 with the model `deepseek-v4-pro`

#### Scenario: A level saved without a model

- **WHEN** the owner saves B1 with a model and then saves it without one
- **THEN** the server returns B1 with no model

#### Scenario: A stored model that left the catalog

- **WHEN** B1 has a stored model that the provider catalog no longer has
- **THEN** the server returns B1 with no model, and calls for B1 use the models of the tasks

### Requirement: The owner saves every level at once

Save SHALL replace the values of all seven levels. The server SHALL remove the spaces at the start
and at the end of the vocabulary and the block. It SHALL reject the save when a level is missing or
comes twice, when a value is empty, when the maximum words is not greater than zero, when No rating
has a rating from, when the rating from does not grow from A1 to C2, or when a model is not in the
provider catalog.

#### Scenario: A save without C2

- **WHEN** the owner saves the levels without C2
- **THEN** the server rejects the save and keeps the levels as they were

#### Scenario: A block of only spaces

- **WHEN** the owner saves the levels with a block of only spaces for B1
- **THEN** the server rejects the save and keeps the levels as they were

#### Scenario: No rating with a rating from

- **WHEN** the owner saves No rating with a rating from
- **THEN** the server rejects the save and keeps the levels as they were

#### Scenario: An empty block

- **WHEN** the owner saves the levels with an empty block for C1
- **THEN** the server rejects the save and keeps the levels as they were

#### Scenario: The rating from does not grow

- **WHEN** the owner saves B2 with a rating from below the rating from of B1
- **THEN** the server rejects the save and keeps the levels as they were

#### Scenario: A model that is not in the catalog

- **WHEN** the owner saves B1 with a model that the provider catalog does not have
- **THEN** the server rejects the save and keeps the levels as they were

### Requirement: The Levels screen

Levels SHALL show the subtitle that the levels set how the interlocutor speaks to each rating
level. The table SHALL have a row for each level with Level, Rating, Vocabulary, Words in a
sentence, max, Idioms and Model. Rating SHALL show the range from the server and a field for the rating
from, except for No rating. Below the table SHALL be the prompt block of the chosen level, and a note that the server puts
the block into the reply and hints prompts as `{level.rules}`.

#### Scenario: The first open

- **WHEN** the owner opens Levels
- **THEN** B1 is the chosen level and its prompt block shows below the table

#### Scenario: Levels opens again

- **WHEN** the owner opens Levels again
- **THEN** the admin app loads the levels from the server again

#### Scenario: A click on a level

- **WHEN** the owner clicks B2 in the table
- **THEN** B2 becomes the chosen level and its prompt block shows below the table

#### Scenario: The levels do not load

- **WHEN** the admin app cannot load the levels
- **THEN** Levels shows an error that the levels did not load, and Retry

#### Scenario: A level is missing in the answer

- **WHEN** the server returns the levels without one of the seven
- **THEN** Levels shows an error that the levels did not load, and Retry

### Requirement: The admin API serves the levels

Behind the admin key, the server SHALL return the seven levels with their ranges at
`GET /admin/levels` and replace them at `PUT /admin/levels`. A save with a model that is not in the
provider catalog SHALL get 409, so the admin app can tell it from a form error.

#### Scenario: No admin key

- **WHEN** a call to the levels API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: A rejected save

- **WHEN** the admin app saves the levels with an empty value
- **THEN** the server answers 400

#### Scenario: A model that is not in the catalog

- **WHEN** the admin app saves B1 with a model that the provider catalog does not have
- **THEN** the server answers 409 and changes nothing

## ADDED Requirements

### Requirement: The Model column of a level

Model SHALL offer Default and every model of the provider catalog, in the order of the server.
Default SHALL mean no model for the level, and SHALL have the same name on Levels and Models. A
level without a model, or without saved values, SHALL show Default. Save SHALL send the chosen
model of each level with its other values.

#### Scenario: A level before a save

- **WHEN** the owner opens Levels before the levels are saved
- **THEN** the Model of each level shows Default

#### Scenario: The choices

- **WHEN** the owner opens Model of B1
- **THEN** it offers Default, `deepseek-flash` and `deepseek-v4-pro`

#### Scenario: A model for a level

- **WHEN** the owner chooses `deepseek-v4-pro` for B1 and clicks Save
- **THEN** the admin app sends B1 with the model `deepseek-v4-pro`, and after the save B1 shows it

#### Scenario: Back to Default

- **WHEN** the owner chooses Default for B1 and clicks Save
- **THEN** the admin app sends B1 with no model

### Requirement: The notes on the level model

The note below the table SHALL say that the model of a level works for Character reply and Hints,
and that Default is the model of the task on Models. The Rating of No rating SHALL also say that
the calibration model is set on Models: the level model does not apply to the calibration.

#### Scenario: The table note

- **WHEN** the owner opens Levels
- **THEN** the note below the table names Character reply, Hints and Default as the model of the
  task on Models

#### Scenario: No rating and the calibration

- **WHEN** the owner opens Levels
- **THEN** the row of No rating says that the calibration model is set on Models

### Requirement: Levels without the models

When the admin app loads the levels but not the models, Levels SHALL show the table. The Model of
each level SHALL show Default with the choice disabled, and an error that the models did not load
SHALL show with Retry. Save SHALL send the stored model of each level as it was.

#### Scenario: The models do not load

- **WHEN** the admin app loads the levels but cannot load the models
- **THEN** Levels shows the table, every Model shows Default and is disabled, and an error that
  the models did not load shows with Retry

#### Scenario: Retry the models

- **WHEN** the owner clicks Retry after the models did not load, and the models load
- **THEN** the Model choices become enabled and show the model of each level

#### Scenario: A save without the models

- **WHEN** the models did not load, B1 has the stored model `deepseek-v4-pro`, and the owner saves
- **THEN** the admin app sends B1 with the model `deepseek-v4-pro`

### Requirement: A level model that is no longer available

When the server answers 409 on Save, the admin app SHALL show a status beside Save that a level
model is no longer available, with Refresh. Refresh SHALL load the levels and the models again.

#### Scenario: The catalog changed before the save

- **WHEN** the owner saves a level model that the server no longer has
- **THEN** the admin app shows the status that the level model is no longer available, with
  Refresh, and not the error that asks to fill every level

#### Scenario: Refresh

- **WHEN** the owner clicks Refresh beside Save
- **THEN** the admin app loads the levels and the models again
