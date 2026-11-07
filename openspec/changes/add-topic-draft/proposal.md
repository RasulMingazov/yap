# Proposal

## Why

The learner gets one topic a day. The owner must write each topic first: the scene, the
interlocutor and the steps that the learner passes (#7).

## What Changes

- The server stores topics. A topic has:
  - a title, a release date, a duration in minutes and a one-line description for the day card;
  - the setting: where and when the scene happens. The learner does not see it;
  - the interlocutor: a character from the cast, or a guest with its own description;
  - the role in the scene, shown above the replies of the interlocutor;
  - the opening line in English and its Russian translation;
  - ordered steps: the task on the screen, Pass when, and the outcome of the step.
- Every topic is a draft. A draft needs only a title and a release date.
- The admin API lists, reads, creates and updates topics behind the admin key.
- The admin app gets the Topics list and the Topic screen with the sections Scene, Interlocutor,
  Opening line and Steps, plus Add step and Save draft.
- The sidebar gets Topics as the first item of Content. The admin app opens on Topics.

Out of scope:

- The calendar, empty days, To do, the status and Publish: #8. The Topics list of this change is
  a plain list, and #8 turns it into the calendar table.
- The number of topics in each row of Characters and in the interlocutor choice: #8, together
  with the Led by column.
- One topic per date: #8.
- Try on the Topic screen: #15.
- Delete of a topic or of a step: the issue leaves it out. The server drops an empty step.

## Capabilities

### New Capabilities

- `topics`: the daily topics, the admin API that serves them, and the admin screens where the
  owner writes them.

### Modified Capabilities

- `characters`: the sidebar shows Topics before Characters, and the admin app opens on Topics.

## Impact

- A new Gradle module `server/feature-topics`. It uses `GetCharacterUseCase` of
  `server/feature-characters`.
- The Flyway migration `V2__topics.sql`.
- `contract-admin` gets the topic DTOs.
- `admin` gets the topics screens and their data layer, and an actions slot in the toolbar.
