# Spec Delta

## Purpose

Each rating level has its own settings for the speech of the interlocutor. The owner tunes them,
and the content of a topic stays the same for every level.

## ADDED Requirements

### Requirement: Seven levels

The server SHALL have the levels A1, A2, B1, B2, C1, C2 and No rating, in this order. No rating
SHALL be for a learner without a rating yet and for the calibration. The owner SHALL NOT add or
remove a level.

#### Scenario: The levels before a save

- **WHEN** the admin app reads the levels before the owner saved them
- **THEN** the server returns the seven levels in order, with no values

### Requirement: A level has its speech settings

Each level SHALL have a rating from, a vocabulary, the maximum words in a sentence, an idioms
setting and a prompt block. No rating SHALL have no rating from. The idioms setting SHALL be one of
Never, Rarely, Sometimes and Freely.

#### Scenario: A saved level keeps its values

- **WHEN** the owner saves B1 with the rating from 1050, the vocabulary B1, 16 words, Rarely and a
  block
- **THEN** the server returns B1 with these values

### Requirement: The server derives the rating range

The range of a level SHALL start at its rating from and end one point below the rating from of the
next level. The range of C2 SHALL have no end. No rating SHALL have no range.

#### Scenario: The range of A2

- **WHEN** A2 has the rating from 900 and B1 has the rating from 1050
- **THEN** the range of A2 is from 900 to 1049

### Requirement: The owner saves every level at once

Save SHALL replace the values of all seven levels. The server SHALL remove the spaces at the start
and at the end of the vocabulary and the block. It SHALL reject the save when a level is missing or
comes twice, when a value is empty, when the maximum words is not greater than zero, when No rating
has a rating from, or when the rating from does not grow from A1 to C2.

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

### Requirement: The level block is the value of `{level.rules}`

The server SHALL give the saved block of a level as the value of `{level.rules}` for that level. A
level without saved values SHALL have no value.

#### Scenario: A changed block

- **WHEN** the owner saves a new block for B1
- **THEN** the value of `{level.rules}` for B1 is the new block

#### Scenario: A level before a save

- **WHEN** the owner did not save the levels yet
- **THEN** `{level.rules}` has no value for any level

### Requirement: Levels stay after a restart

The server SHALL keep the levels in its database.

#### Scenario: A restart

- **WHEN** the server restarts after the owner saved the levels
- **THEN** the admin API returns the same values

### Requirement: The admin API serves the levels

Behind the admin key, the server SHALL return the seven levels with their ranges at
`GET /admin/levels` and replace them at `PUT /admin/levels`.

#### Scenario: No admin key

- **WHEN** a call to the levels API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: A rejected save

- **WHEN** the admin app saves the levels with an empty value
- **THEN** the server answers 400

### Requirement: The sidebar opens the levels

The sidebar SHALL show the group Rules between Content and Settings, with Levels in it.

#### Scenario: Levels in the sidebar

- **WHEN** the owner clicks Levels in the sidebar
- **THEN** the admin app opens the Levels screen

### Requirement: The Levels screen

Levels SHALL show the subtitle that the levels set how the interlocutor speaks to each rating
level. The table SHALL have a row for each level with Level, Rating, Vocabulary, Words in a
sentence, max, and Idioms. Rating SHALL show the range from the server and a field for the rating
from, except for No rating. Below the table SHALL be the prompt block of the chosen level, and a note that the server puts
the block into the reply and hints prompts as `{level.rules}`.

#### Scenario: The first open

- **WHEN** the owner opens Levels
- **THEN** B1 is the chosen level and its prompt block shows below the table

#### Scenario: A click on a level

- **WHEN** the owner clicks B2 in the table
- **THEN** B2 becomes the chosen level and its prompt block shows below the table

#### Scenario: The levels do not load

- **WHEN** the admin app cannot load the levels
- **THEN** Levels shows an error that the levels did not load, and Retry

#### Scenario: A level is missing in the answer

- **WHEN** the server returns the levels without one of the seven
- **THEN** Levels shows an error that the levels did not load, and Retry

### Requirement: The rows without a rating from or without values

The Rating of No rating SHALL show that the level is also for the calibration, with no field for the
rating from. A level without saved values SHALL show no idioms choice.

#### Scenario: No rating

- **WHEN** the owner opens Levels
- **THEN** the row of No rating shows the calibration in Rating and has no field for the rating from

#### Scenario: A level before a save

- **WHEN** the owner opens Levels before the levels are saved
- **THEN** the Idioms of each level show no choice

### Requirement: Saving the levels

The toolbar of Levels SHALL have Save with the save status. Save SHALL send every level to the
server. When the server accepts them, the admin app SHALL show the levels that the server returns
and a Saved status.

#### Scenario: The save succeeds

- **WHEN** the owner fills every level and clicks Save
- **THEN** the table shows the ranges from the server and the Saved status shows beside Save

#### Scenario: The save is in progress

- **WHEN** the owner clicks Save and the server has not answered yet
- **THEN** a saving status shows beside Save, and Save and the fields of the levels are disabled

#### Scenario: The server rejects the save

- **WHEN** the server rejects the levels
- **THEN** the admin app shows an error beside Save that asks to fill every level with growing
  ratings, and keeps the form as it is

#### Scenario: A number that is not a number

- **WHEN** the owner types a rating from or a word count that the admin app cannot read
- **THEN** the admin app shows an error beside Save that names the field and the level, and sends
  nothing

#### Scenario: An empty value

- **WHEN** the owner leaves a value of a level empty and clicks Save
- **THEN** the admin app shows the error beside Save that asks to fill every level with growing
  ratings, and sends nothing

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save
- **THEN** it keeps the form as it is and shows a snackbar that the save failed
