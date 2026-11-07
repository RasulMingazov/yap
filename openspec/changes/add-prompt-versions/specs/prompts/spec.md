# Spec Delta

## Purpose

The owner keeps the text of every model prompt on the server, changes it without a release, and
returns to an earlier version when needed.

## ADDED Requirements

### Requirement: Six prompts

The server SHALL know six prompts in two groups. Topic: Character reply, Step check, Hints and
Review. Calibration: Answer check and Sam's reply. The owner SHALL NOT add or remove a prompt.

#### Scenario: The list

- **WHEN** the admin app lists the prompts
- **THEN** the server returns the six prompts with their groups, in this order

### Requirement: A topic prompt lists its variables

Each prompt SHALL list the variables that the server substitutes in its text, in a fixed order. The
topic prompts SHALL list:

- Character reply: `{interlocutor}`, `{topic.setting}`, `{topic.role}`, `{level.rules}`,
  `{step.n}`, `{step.task}` and `{check.verdict}`.
- Step check: `{step.task}`, `{step.passWhen}`, `{last.line}` and `{answers}`.
- Hints: `{step.task}`, `{topic.setting}`, `{level.rules}` and `{hint.level}`.
- Review: `{topic.title}`, `{steps}` and `{answers}`.

#### Scenario: The variables of a topic prompt

- **WHEN** the admin app reads a topic prompt
- **THEN** the server returns the variables of that prompt from this list, in this order

### Requirement: A calibration prompt lists its variables

The calibration prompts SHALL list:

- Answer check: `{step.task}` and `{answer}`.
- Sam's reply: `{step.n}`, `{level.rules}` and `{check.verdict}`. Here `{level.rules}` is the block
  of No rating.

#### Scenario: The variables of a calibration prompt

- **WHEN** the admin app reads a calibration prompt
- **THEN** the server returns the variables of that prompt from this list, in this order

### Requirement: No text until the first save

The server SHALL NOT seed the texts. A prompt SHALL have no text and no versions until the owner
saves its first version.

#### Scenario: A prompt before a save

- **WHEN** the admin app reads a prompt that the owner never saved
- **THEN** the server returns the prompt with no text and no versions

### Requirement: Save makes a new current version

Save SHALL store the text as a new version with the next number and the time of the save. The new
version SHALL be current at once. The server SHALL reject a text that is empty after the spaces at
its start and its end are removed.

#### Scenario: A second save

- **WHEN** the owner saves a prompt that has the versions 1 and 2
- **THEN** the server stores version 3, and version 3 is current

#### Scenario: An empty text

- **WHEN** the owner saves a text of spaces only
- **THEN** the server answers 400 and stores nothing

### Requirement: Restore makes an older version current

Restore SHALL make an existing version current again. It SHALL NOT make a new version. The next
Save SHALL take the number after the highest one.

#### Scenario: Restore

- **WHEN** the owner restores version 1 of a prompt with the versions 1 and 2
- **THEN** version 1 is current, and the next save stores version 3

### Requirement: Prompts stay after a restart

The server SHALL keep the versions in its database.

#### Scenario: A restart

- **WHEN** the server restarts after the owner saved a version
- **THEN** the admin API returns the same versions and the same current version

### Requirement: The admin API serves the prompts

Behind the admin key, the server SHALL:

- list the prompts with their groups, in order, at `GET /admin/prompts`;
- return a prompt with its variables, its current text and its versions at
  `GET /admin/prompts/{promptKey}`;
- save a version and return the prompt at `POST /admin/prompts/{promptKey}/versions`;
- restore a version and return the prompt at
  `POST /admin/prompts/{promptKey}/versions/{number}/restore`.

#### Scenario: No admin key

- **WHEN** a call to the prompts API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: An unknown prompt or version

- **WHEN** the admin app reads an unknown prompt or restores an unknown version
- **THEN** the server answers 404

### Requirement: The sidebar opens the prompts

The sidebar SHALL show Prompts in the group Rules, after Levels.

#### Scenario: Prompts in the sidebar

- **WHEN** the owner clicks Prompts in the sidebar
- **THEN** the admin app opens the Prompts screen

### Requirement: The Prompts screen

Prompts SHALL show the subtitle that a prompt affects all topics. The list on the left SHALL show the
groups Topic and Calibration with the title of each prompt and when it is used. The editor SHALL show
the title of the chosen prompt, its current version, its text, the variables and Versions. Each
version SHALL show its number, its time and whether it is current. Each older version SHALL have
Restore.

#### Scenario: The first open

- **WHEN** the owner opens Prompts
- **THEN** Character reply is the chosen prompt

#### Scenario: A prompt without a version

- **WHEN** the owner chooses a prompt that has no version yet
- **THEN** the editor is empty, the state says that it is not saved yet, and Versions is empty

#### Scenario: Typed text stays

- **WHEN** the owner types in one prompt, chooses another, and returns
- **THEN** the editor shows the typed text

#### Scenario: The prompts do not load

- **WHEN** the admin app cannot load the list or the chosen prompt
- **THEN** Prompts shows an error that the prompts did not load, and Retry

### Requirement: Saving and restoring a prompt

The toolbar SHALL have Save with a note that a new version goes to the users at once. Save SHALL send
the text of the chosen prompt. Restore SHALL send the number of the version. After either, the admin
app SHALL show the prompt that the server returns, and a Saved status for Save.

#### Scenario: The save succeeds

- **WHEN** the owner changes the text and clicks Save
- **THEN** Versions shows the new version as current, and the Saved status shows beside Save

#### Scenario: An empty text

- **WHEN** the owner clicks Save with an empty editor
- **THEN** the admin app sends the text, the server answers 400, and the admin app shows an error
  beside Save that asks for the text

#### Scenario: Restore

- **WHEN** the owner clicks Restore at version 2
- **THEN** the editor shows the text of version 2, and Versions shows version 2 as current

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save or Restore
- **THEN** it keeps the editor as it is and shows a snackbar that the action failed
