## ADDED Requirements

### Requirement: The topic count of a character

The topic count of a character SHALL be the number of topics, drafts and published, that have the
character as the interlocutor. The server SHALL derive it.

#### Scenario: Drafts and published topics

- **WHEN** Sam leads one draft and two published topics, and Mia leads no topic
- **THEN** the topic count of Sam is 3 and the topic count of Mia is 0

## MODIFIED Requirements

### Requirement: The admin API serves characters

Behind the admin key, the server SHALL:

- list the characters, ordered by name, with the topic count of each, at `GET /admin/characters`;
- return one character at `GET /admin/characters/{characterId}`;
- create a character and return it with its identifier at `POST /admin/characters`;
- replace the profile of a character and return it at `PUT /admin/characters/{characterId}`.

#### Scenario: No admin key

- **WHEN** a call to the characters API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: An unknown character

- **WHEN** the admin app reads or updates a character that does not exist
- **THEN** the server answers 404

#### Scenario: A rejected profile

- **WHEN** the admin app creates or updates a character with an empty field
- **THEN** the server answers 400

#### Scenario: The list with counts

- **WHEN** Sam leads two topics, and the admin app lists the characters
- **THEN** the row of Sam has the topic count 2

### Requirement: The Characters list

Characters SHALL show a subtitle that calls the characters the recurring cast, and one row for each
character, in the order of the server. A row SHALL show the name and, below it, the personality in
one line, and the topic count on the right, in the Russian form for its number. A click on a row
SHALL open the Character form of the character. New character SHALL be below the list.

#### Scenario: The list

- **WHEN** the owner opens Characters
- **THEN** the admin app shows a row for each character from the server

#### Scenario: The topic count

- **WHEN** Sam leads 12 topics, Mia leads 4 topics and Dan leads no topic
- **THEN** the rows show 12 topics, 4 topics and 0 topics, each in the Russian form for its number

#### Scenario: No characters yet

- **WHEN** the server has no characters
- **THEN** Characters shows a message that there are no characters yet, and New character

#### Scenario: A return to Characters

- **WHEN** the owner returns to Characters from another screen
- **THEN** the admin app shows the list that it has at once, and replaces it with the list from the
  server when the server answers

#### Scenario: The list does not update

- **WHEN** the admin app shows a list and cannot load the characters again
- **THEN** it keeps the list

#### Scenario: The list does not load

- **WHEN** the admin app has no list yet and cannot load the characters
- **THEN** Characters shows an error that the characters did not load, and Retry
