# Spec Delta

## Purpose

The owner keeps a cast of recurring characters. Topics use them as interlocutors, so the same
character sounds the same in every topic.

## ADDED Requirements

### Requirement: A character has a profile

A character SHALL have a profile of four fields: the name, the history, the personality and the
manner of speech. The server SHALL give each character a stable identifier. The role in a scene,
such as a taxi driver or a doctor, SHALL NOT be part of the character: the topic sets it.

#### Scenario: A saved character keeps its profile

- **WHEN** the owner saves a character with a name, a history, a personality and a manner of speech
- **THEN** the server stores the four fields under the identifier of the character

### Requirement: Every field of the profile is filled

The server SHALL remove the spaces at the start and at the end of each field. Then it SHALL reject
the profile when a field is empty.

#### Scenario: An empty field

- **WHEN** the owner saves a character with an empty manner of speech
- **THEN** the server rejects the profile and stores nothing

#### Scenario: Spaces around a field

- **WHEN** the owner saves a character with the name " Sam "
- **THEN** the server stores the name "Sam"

### Requirement: Characters stay after a restart

The server SHALL keep the characters in its database.

#### Scenario: A restart

- **WHEN** the server restarts after the owner saved a character
- **THEN** the admin API returns the character with the same profile

### Requirement: The admin API serves characters

Behind the admin key, the server SHALL:

- list the characters, ordered by name, at `GET /admin/characters`;
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

### Requirement: The sidebar opens the characters

The sidebar of the admin app SHALL show «Персонажи» in the group «Контент» and «Сервер» in the
group «Настройки». The admin app SHALL open on «Персонажи» when it has saved server settings, and on
«Сервер» when it has none.

#### Scenario: The first start

- **WHEN** the owner starts the admin app for the first time
- **THEN** the admin app opens «Сервер»

#### Scenario: A later start

- **WHEN** the owner starts the admin app after a connection to a server
- **THEN** the admin app opens «Персонажи»

### Requirement: The «Персонажи» list

«Персонажи» SHALL show the subtitle «Постоянные герои, как в ситкоме» and one row for each
character, in the order of the server. A row SHALL show the name and, below it, the personality in
one line. A click on a row SHALL open the «Персонаж» form of the character. «Новый персонаж» SHALL
be below the list.

#### Scenario: The list

- **WHEN** the owner opens «Персонажи»
- **THEN** the admin app shows a row for each character from the server

#### Scenario: No characters yet

- **WHEN** the server has no characters
- **THEN** «Персонажи» shows «Персонажей пока нет» and «Новый персонаж»

#### Scenario: The list does not load

- **WHEN** the admin app cannot load the characters
- **THEN** «Персонажи» shows «Не удалось загрузить персонажей» and «Повторить»

### Requirement: The «Персонаж» form

The «Персонаж» form SHALL have the section «Кто это» with the fields «Имя — как его называют
пользователи», «История», «Характер» and «Манера речи». Below the fields SHALL be the note «Роль в
сцене — таксист, врач, сосед — задаётся в теме. Сложность лексики задаётся в «Уровнях» и одна для
всех персонажей.» The toolbar SHALL have «Персонажи» to go back, and «Сохранить».

#### Scenario: A saved character

- **WHEN** the owner opens a saved character
- **THEN** the title is its name, the subtitle is «Постоянный герой» and the fields hold its profile

#### Scenario: A new character

- **WHEN** the owner clicks «Новый персонаж»
- **THEN** the title is «Новый персонаж», the subtitle is «Ещё не сохранён», the fields are empty,
  and the form shows «Заполните поля и нажмите «Сохранить».»

#### Scenario: The character does not load

- **WHEN** the admin app cannot load the character
- **THEN** the form shows «Не удалось загрузить персонажа» and «Повторить»

### Requirement: Saving a character

«Сохранить» SHALL send the profile to the server. When the server accepts it, the admin app SHALL
show the profile that the server returns and «Сохранено» beside «Сохранить». A new character SHALL
then become a saved character.

#### Scenario: The save succeeds

- **WHEN** the owner fills every field of a new character and clicks «Сохранить»
- **THEN** the title becomes the name, the subtitle becomes «Постоянный герой», and «Сохранено» shows
  beside «Сохранить»

#### Scenario: An empty field

- **WHEN** the owner clicks «Сохранить» with an empty field
- **THEN** the admin app shows «Заполните все поля» beside «Сохранить» and keeps the form as it is

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on «Сохранить»
- **THEN** it keeps the form as it is and shows «Не удалось сохранить. Попробуйте ещё раз.»
