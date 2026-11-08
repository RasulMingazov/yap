# Spec Delta

## Purpose

The owner writes the topic of each day: the scene, the interlocutor and the steps.

## ADDED Requirements

### Requirement: A topic has a scene, an interlocutor and steps

A topic SHALL have a title, a release date, a duration in minutes, a day card description, a
setting, an interlocutor, a role, an opening line with its Russian translation, and ordered steps.
A draft MAY leave all but the title and the date empty. The server SHALL give each topic a stable
identifier and keep it in its database. A step SHALL have a task, Pass when and an outcome. The
steps SHALL keep their order and SHALL be the same for every rating level.

#### Scenario: A saved topic keeps its fields

- **WHEN** the owner saves a topic with every field filled, and the server restarts
- **THEN** the admin API returns every field under the identifier of the topic

#### Scenario: The order of the steps

- **WHEN** the owner saves a topic with three steps
- **THEN** the server returns the three steps in the same order

### Requirement: The interlocutor is a character or a guest

The interlocutor SHALL be a character of the cast or a guest. A guest SHALL have its own
description, which only this topic uses. A guest with an empty description SHALL stay a guest.

#### Scenario: A character

- **WHEN** the owner saves a topic with a character of the cast as the interlocutor
- **THEN** the server stores the identifier of the character

#### Scenario: A guest

- **WHEN** the owner saves a topic with a guest and its description, or with an empty description
- **THEN** the server stores a guest with that description

### Requirement: A draft needs a title and a release date

Every topic SHALL be a draft. The server SHALL remove the spaces at the start and at the end of
each text field, and SHALL drop a step whose task, Pass when and outcome are all empty. Then it
SHALL reject a topic without a title, without a release date, with a duration that is not a whole
number of minutes greater than zero, or with a character that does not exist. An empty duration
SHALL be valid.

#### Scenario: A draft with a title and a date

- **WHEN** the owner saves a topic with only a release date and a title with spaces around it
- **THEN** the server stores the draft with the title without the spaces

#### Scenario: A rejected draft

- **WHEN** the owner saves a topic with a title of spaces only, without a release date, with a
  duration of 0 minutes, or with a character that does not exist
- **THEN** the server rejects the topic and stores nothing

#### Scenario: An empty step

- **WHEN** the owner saves a topic with a filled step and an empty step after it
- **THEN** the server stores only the filled step

### Requirement: The admin API serves topics

Behind the admin key, the server SHALL list the topics ordered by release date at
`GET /admin/topics`, return one topic at `GET /admin/topics/{topicId}`, create a topic and return
it with its identifier at `POST /admin/topics`, and replace the fields of a topic and return it at
`PUT /admin/topics/{topicId}`.

#### Scenario: No admin key

- **WHEN** a call to the topics API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: An unknown topic

- **WHEN** the admin app reads or updates a topic that does not exist
- **THEN** the server answers 404

#### Scenario: A rejected topic

- **WHEN** the admin app sends a topic that the draft rule rejects, or a release date that is not a
  date
- **THEN** the server answers 400 and stores nothing

### Requirement: The Topics list

Topics SHALL show the subtitle One topic a day and a table with the columns Date and Topic: one
row for each topic, in the order of the server. Date SHALL show the short day of the week, the day
and the month, such as Fri, 24 Oct. A click on a title SHALL open the Topic screen of the topic.
The toolbar SHALL have New topic.

#### Scenario: Topics with rows

- **WHEN** the server has topics for 24 and 25 October
- **THEN** Topics shows them in the order of the server with the dates Fri, 24 Oct and Sat, 25 Oct,
  and a click on a title opens the Topic screen of that topic

#### Scenario: No topics yet

- **WHEN** the server has no topics
- **THEN** Topics shows a message that there are no topics yet, and New topic

#### Scenario: A return to Topics

- **WHEN** the owner returns to Topics from another screen
- **THEN** the admin app shows the list that it has at once, and replaces it with the list from the
  server when the server answers

#### Scenario: The list does not load

- **WHEN** the admin app has no list yet and cannot load the topics
- **THEN** Topics shows an error that the topics did not load, and Retry

### Requirement: The Topic screen

The Topic screen SHALL have the numbered sections Scene, Interlocutor, Opening line and Steps.
Scene SHALL have Title, Release date, Duration in minutes, Description and Setting. Release date
SHALL be a popup with the day of the week, the day and the month, which opens a date picker.
Duration SHALL take at most three digits. Opening line SHALL have English and Translation. The
toolbar SHALL have a back arrow to Topics, and Save draft with its status.

#### Scenario: A saved topic

- **WHEN** the owner opens a saved topic
- **THEN** the title is the title of the topic, the subtitle shows its release date and that it is
  a draft, and the fields hold the topic

#### Scenario: A new topic

- **WHEN** the owner clicks New topic
- **THEN** the title is New topic, the subtitle says that it is not saved yet, the fields are empty,
  Release date shows Choose a date, the Steps section has one empty step, and a hint says that a
  draft needs a title and a release date and that the rest can wait

#### Scenario: Pick a date

- **WHEN** the owner picks a day in the date picker of Release date
- **THEN** the popup shows that day with its day of the week, such as Friday, 24 October

#### Scenario: Duration

- **WHEN** the owner types a fourth digit or a letter into Duration
- **THEN** the field does not take it

#### Scenario: The topic does not load

- **WHEN** the admin app cannot load the topic or the cast
- **THEN** the screen shows an error that the topic did not load, and Retry

### Requirement: The Topic screen guides the owner

While the topic loads, and when it does not load, the toolbar SHALL show the title Topic and the
back arrow to Topics. A new topic SHALL show a hint that a draft needs a title and a release date.
Release date without a date SHALL show Choose a date. A pick of the day that Release date already
shows SHALL change nothing.

#### Scenario: The topic loads

- **WHEN** the admin app loads the topic and the cast
- **THEN** the screen shows a loader below a toolbar with the title Topic and the back arrow to
  Topics

#### Scenario: The error keeps the way back

- **WHEN** the topic does not load
- **THEN** the error and Retry show below a toolbar with the title Topic and the back arrow to
  Topics

#### Scenario: Pick the same day

- **WHEN** the Saved status shows, and the owner picks the day that Release date already shows
- **THEN** the form does not change and the Saved status stays

### Requirement: The Interlocutor and Steps sections

Interlocutor SHALL offer one radio row for each character of the cast, in the order of the server,
and then Guest with the note Only for this topic. The guest description field SHALL show only when
Guest is chosen. Role in the scene SHALL be below the choice. Steps SHALL show one row for each
step with its number, Task on the screen, Pass when and Step outcome, then Add step, and a note
that the steps and the check are the same for every rating level.

#### Scenario: Guest

- **WHEN** the owner chooses Guest
- **THEN** the guest description field shows below the choice

#### Scenario: Add step

- **WHEN** the owner clicks Add step
- **THEN** the Steps section gets an empty step below the last one

#### Scenario: A step edit

- **WHEN** the owner edits a field of the second of two steps
- **THEN** only the second step changes

### Requirement: The Interlocutor section guides the owner

Without characters, a note above Guest SHALL say that there are no characters yet and that the
owner adds them in Characters. A second choice of Guest SHALL bring back the last guest description. A choice of the interlocutor
that is already chosen SHALL change nothing.

#### Scenario: Guest again

- **WHEN** the owner types a guest description, chooses a character, and then chooses Guest again
- **THEN** the guest description field shows the description that the owner typed

#### Scenario: The same interlocutor

- **WHEN** the Saved status shows, and the owner chooses the interlocutor that is already chosen
- **THEN** the form does not change and the Saved status stays

#### Scenario: No characters yet

- **WHEN** the cast has no characters
- **THEN** Interlocutor shows a note above Guest that there are no characters yet and that the
  owner adds them in Characters

### Requirement: Saving a draft

Save draft SHALL send the topic to the server, with an empty release date and an empty duration as
no value. When the server accepts it, the admin app SHALL show the topic that the server returns,
and a Saved status beside Save draft. A new topic SHALL then become a saved topic.

#### Scenario: The save succeeds

- **WHEN** the owner fills the title and the release date of a new topic and clicks Save draft
- **THEN** the title becomes the title of the topic, the subtitle shows the release date, and the
  Saved status shows beside Save draft

#### Scenario: The server rejects the draft

- **WHEN** the server answers 400 on Save draft
- **THEN** the admin app keeps the form and shows one error beside Save draft: the server did not
  accept the draft, check the title, the date, the duration and the interlocutor

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save draft
- **THEN** it keeps the form as it is and shows a snackbar that the save failed

### Requirement: The status of a save

While the save runs, the admin app SHALL show Saving… beside Save draft, and SHALL disable Save
draft, the fields, the date popup, Add step and the back arrow. A field edit SHALL remove the Saved status.
The error of a rejected draft SHALL stay until the next click on Save draft.

#### Scenario: The save runs

- **WHEN** the owner clicks Save draft and the server has not answered yet
- **THEN** Saving… shows beside Save draft, and Save draft, the fields, the date popup, Add step
  and the back arrow are disabled

#### Scenario: An edit after a rejected draft

- **WHEN** the server rejected the draft, and the owner edits a field
- **THEN** the error stays beside Save draft until the next click on Save draft

#### Scenario: An edit after a save

- **WHEN** the Saved status shows, and the owner edits a field
- **THEN** the Saved status goes away
