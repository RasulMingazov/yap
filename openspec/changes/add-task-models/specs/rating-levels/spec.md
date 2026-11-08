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

#### Scenario: The models do not load

- **WHEN** the admin app loads the levels but cannot load the models
- **THEN** Levels shows an error that the levels did not load, and Retry

## ADDED Requirements

### Requirement: The Model column of a level

Model SHALL offer Default and every model of the provider catalog, in the order of the server.
Default SHALL mean no model for the level. A level without a model, or without saved values, SHALL
show Default. Save SHALL send the chosen model of each level with its other values.

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
