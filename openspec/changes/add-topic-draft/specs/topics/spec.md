# Spec Delta

## Purpose

The owner writes the topic of each day: the scene, the interlocutor and the steps. The learner
later passes the topic as a dialog with the interlocutor.

## ADDED Requirements

### Requirement: A topic has a scene, an interlocutor and steps

A topic SHALL have a title, a release date, a duration in minutes, a description for the day card,
a setting, an interlocutor, a role in the scene, an opening line in English with its Russian
translation, and ordered steps. The server SHALL give each topic a stable identifier.

#### Scenario: A saved topic keeps its fields

- **WHEN** the owner saves a topic with every field filled
- **THEN** the server stores every field under the identifier of the topic

### Requirement: A step has a task, a pass rule and an outcome

A step SHALL have the task on the screen, Pass when, and the outcome shown when the step is done.
The steps SHALL keep the order in which the owner saves them. The steps SHALL be the same for
every rating level.

#### Scenario: The order of the steps

- **WHEN** the owner saves a topic with three steps
- **THEN** the server returns the three steps in the same order

### Requirement: The interlocutor is a character or a guest

The interlocutor SHALL be a character of the cast or a guest. A guest SHALL have its own
description, which only this topic uses. A draft MAY have no interlocutor yet.

#### Scenario: A character

- **WHEN** the owner saves a topic with a character of the cast as the interlocutor
- **THEN** the server stores the identifier of the character

#### Scenario: An unknown character

- **WHEN** the owner saves a topic with a character that does not exist
- **THEN** the server rejects the topic and stores nothing

#### Scenario: A guest

- **WHEN** the owner saves a topic with a guest and its description
- **THEN** the server stores the guest with its description

### Requirement: A draft needs a title and a release date

Every topic SHALL be a draft. The server SHALL remove the spaces at the start and at the end of
each text field. Then it SHALL reject a topic without a title or without a release date. Every
other field MAY be empty.

#### Scenario: A draft with a title and a date

- **WHEN** the owner saves a topic with only a title and a release date
- **THEN** the server stores the draft

#### Scenario: No title

- **WHEN** the owner saves a topic with a title of spaces only
- **THEN** the server rejects the topic and stores nothing

### Requirement: The duration is a positive number of minutes

The duration SHALL be a whole number of minutes greater than zero, or empty.

#### Scenario: A zero duration

- **WHEN** the owner saves a topic with a duration of 0 minutes
- **THEN** the server rejects the topic and stores nothing

### Requirement: The server drops an empty step

The server SHALL drop a step whose task, Pass when and outcome are all empty. It SHALL keep a step
with at least one filled field.

#### Scenario: An empty step

- **WHEN** the owner saves a topic with a filled step and an empty step after it
- **THEN** the server stores only the filled step

### Requirement: Topics stay after a restart

The server SHALL keep the topics in its database.

#### Scenario: A restart

- **WHEN** the server restarts after the owner saved a topic
- **THEN** the admin API returns the topic with the same fields

### Requirement: The admin API serves topics

Behind the admin key, the server SHALL:

- list the topics, ordered by release date, at `GET /admin/topics`;
- return one topic at `GET /admin/topics/{topicId}`;
- create a topic and return it with its identifier at `POST /admin/topics`;
- replace the fields of a topic and return it at `PUT /admin/topics/{topicId}`.

#### Scenario: No admin key

- **WHEN** a call to the topics API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: An unknown topic

- **WHEN** the admin app reads or updates a topic that does not exist
- **THEN** the server answers 404

#### Scenario: A rejected topic

- **WHEN** the admin app creates or updates a topic without a title, or with an unknown character
- **THEN** the server answers 400

#### Scenario: A date that does not parse

- **WHEN** the admin app sends a release date that is not a date
- **THEN** the server answers 400

### Requirement: The Topics list

Topics SHALL show the subtitle One topic a day and one row for each topic, in the order of the
server. A row SHALL show the title and, below it, the release date with the day of the week. A
click on a row SHALL open the Topic screen of the topic. The toolbar SHALL have New topic.

#### Scenario: The list

- **WHEN** the owner opens Topics
- **THEN** the admin app shows a row for each topic from the server

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

The Topic screen SHALL have four numbered sections: Scene, Interlocutor, Opening line and Steps.
Scene SHALL have Title, Release date, Duration in minutes, Description and Setting. Opening line
SHALL have English and Translation. The toolbar SHALL have a back arrow that opens Topics, and Save
draft with the save status.

#### Scenario: A saved topic

- **WHEN** the owner opens a saved topic
- **THEN** the title is the title of the topic, the subtitle shows its release date and that it is
  a draft, and the fields hold the topic

#### Scenario: A new topic

- **WHEN** the owner clicks New topic
- **THEN** the title is New topic, the subtitle says that it is not saved yet, the fields are
  empty, and the Steps section has one empty step

#### Scenario: The topic does not load

- **WHEN** the admin app cannot load the topic or the cast
- **THEN** the screen shows an error that the topic did not load, and Retry

### Requirement: The Interlocutor section

The Interlocutor section SHALL offer each character of the cast, in the order of the server, and
Guest. The guest description field SHALL show only when Guest is chosen. Role in the scene SHALL be
below the choice.

#### Scenario: Guest

- **WHEN** the owner chooses Guest
- **THEN** the guest description field shows below the choice

### Requirement: The Steps section

The Steps section SHALL show one row for each step with its number, Task on the screen, Pass when
and Step outcome. Add step SHALL be below the rows. A note SHALL say that the steps and the check
are the same for every rating level.

#### Scenario: Add step

- **WHEN** the owner clicks Add step
- **THEN** the Steps section gets an empty step below the last one

### Requirement: Saving a draft

Save draft SHALL send the topic to the server. When the server accepts it, the admin app SHALL show
the topic that the server returns, and a Saved status beside Save draft. A new topic SHALL then
become a saved topic.

#### Scenario: The save succeeds

- **WHEN** the owner fills the title and the release date of a new topic and clicks Save draft
- **THEN** the title becomes the title of the topic, the subtitle shows the release date, and the
  Saved status shows beside Save draft

#### Scenario: No title or date

- **WHEN** the owner clicks Save draft without a title or without a release date
- **THEN** the admin app shows an error beside Save draft that asks for the title and the date, and
  keeps the form as it is

#### Scenario: A date that is not a date

- **WHEN** the owner types a release date or a duration that the admin app cannot read
- **THEN** the admin app shows an error beside Save draft that names the field, sends nothing, and
  keeps the form as it is

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save draft
- **THEN** it keeps the form as it is and shows a snackbar that the save failed
