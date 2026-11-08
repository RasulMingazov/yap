# Proposal

## Why

Every day must have one published topic. The owner must see the empty days and the unfinished
drafts early, before a learner gets an empty day (#8).

## What Changes

- A topic is a draft or published. Publish needs every field of the topic and a date of today or
  later. The server checks it and names what is missing: the fields, the numbers of the incomplete
  steps, a past date, and a date that another published topic holds.
- A date has at most one published topic. Drafts can share a date.
- On its release date, a published topic is the topic of the day. The server derives the status:
  Draft, Published, Today or Out. The day starts at midnight Moscow time.
- A published topic stays published. Its save needs every field.
- The admin API gets the calendar: the topics and the empty days of the next 14 days, and the To
  do list. It gets Publish. A rejected write names the missing fields. A date that has a published
  topic answers 409.
- The characters list of the admin API gets the topic count of each character.
- Topics gets To do, the section All topics, the columns Led by, Steps and Status, the empty days
  with Create, and the note about publishing.
- The Topic screen gets Publish with a question first, the publish note, errors that say the
  changes are not saved and what to do next, Save for a published topic, and a question before a
  save moves the date of a published topic.
- Characters and the interlocutor choice show the topic count of each character.
- **BREAKING** (admin API only): `GET /admin/topics` goes away. The calendar replaces it.

Out of scope:

- Try on the Topic screen: #15. The To do note of a ready draft does not mention Try until #15.
- The items under Proposals: no issue owns them yet.

## Proposals

Out of scope, with no issue yet. The owner decides whether to open one.

- What the mobile app shows for the day.
- Unpublish.
- The To do count in the sidebar, as the canvas shows it.
- Delete of a topic.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `topics`: the status, Publish, one published topic per date, the release day, the calendar, To
  do, and the topic counts in the interlocutor choice.
- `characters`: the topic count of each character in the admin API and in Characters.

## Impact

- `server/feature-topics`: `V5__topic_status.sql`, the status, the publish rule, the calendar, the
  publish use case and routes. It implements the topic count port of `feature-characters`.
- `server/feature-characters`: the port for the topic counts, the summaries use case for the list,
  and the port rule in `docs/server/001-structure.md`.
- `docs/server/001-structure.md`: the DTO mapper maps 1:1; a rule in `model/` normalizes the text
  input (the readability audit of 2026-11-08).
- `contract-admin`: the calendar, the status, the field and the rejection DTOs, the character
  summary DTO, and two new fields in `TopicDto`.
- `admin`: the calendar screen, Publish on the Topic screen, the counts, a `warn` color token, and
  Create with a date in the navigation.
