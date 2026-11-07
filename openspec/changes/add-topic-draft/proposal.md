# Proposal

## Why

The learner gets one topic a day. The owner must write each topic first: the scene, the
interlocutor and the steps that the learner passes (#7).

## What Changes

- The server stores topics: the title, the release date, the duration, the day card description,
  the setting, the interlocutor (a character or a guest), the role, the opening line with its
  translation, and ordered steps (task, Pass when, outcome).
- Every topic is a draft. A draft needs only a title and a release date.
- The admin API lists, reads, creates and updates topics behind the admin key.
- The admin app gets Topics with New topic, and the Topic screen with Scene, Interlocutor, Opening
  line, Steps, Add step and Save draft. Topics comes first in Content, and the app opens on it.

Out of scope:

- The calendar, To do, empty days, the status, Publish and one topic per date: #8. New topic comes
  with this change, so #8 adds only Publish to the Topic screen.
- The topic counts in Characters and in the interlocutor choice, and Led by: #8.
- Try on the Topic screen: #15.
- Delete of a topic or of a step: out of scope.

## Capabilities

### New Capabilities

- `topics`: the daily topics, their admin API and the admin screens where the owner writes them.

### Modified Capabilities

- `characters`: the sidebar shows Topics before Characters, and the admin app opens on Topics.

## Impact

- The module `server/feature-topics` with `V2__topics.sql` and `exposed-java-time`. It uses
  `GetCharacterUseCase`. `contract-admin` gets the topic DTOs.
- `admin` gets the topics screens, their data layer, and an actions slot in `ScreenToolbar`.
