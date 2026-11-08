## MODIFIED Requirements

### Requirement: Saving the levels

The toolbar of Levels SHALL have Save with the save status. Save SHALL send every level to the
server. The admin app SHALL read only the numbers and the idioms choice of the form, and SHALL leave
every other rule to the server. When the server accepts the levels, the admin app SHALL show the
levels that the server returns and a Saved status.

#### Scenario: The save succeeds

- **WHEN** the owner fills every level and clicks Save
- **THEN** the table shows the ranges from the server and the Saved status shows beside Save

#### Scenario: The save answer misses a level

- **WHEN** the server accepts the levels and returns them without one of the seven
- **THEN** the Saved status shows beside Save, and the admin app loads the levels again

#### Scenario: The save is in progress

- **WHEN** the owner clicks Save and the server has not answered yet
- **THEN** a saving status shows beside Save, and Save and the fields of the levels are disabled

#### Scenario: The server rejects the save

- **WHEN** the server rejects the levels
- **THEN** the admin app shows an error beside Save that asks to fill every level with growing
  ratings, and keeps the form as it is

#### Scenario: A number that is not a number

- **WHEN** the owner types a rating from or a word count that the admin app cannot read, or leaves
  the word count empty
- **THEN** the admin app shows an error beside Save that names the field and the level, and sends
  nothing

#### Scenario: A level without idioms

- **WHEN** a level has no idioms choice and the owner clicks Save
- **THEN** the admin app shows an error beside Save that names the level, and sends nothing

#### Scenario: An empty value

- **WHEN** the owner leaves a rating from, a vocabulary or a block of a level empty and clicks Save
- **THEN** the admin app sends the levels, and shows the error of the server beside Save

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save
- **THEN** it keeps the form as it is and shows a snackbar that the save failed
