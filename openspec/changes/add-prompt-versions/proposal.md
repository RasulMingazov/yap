# Proposal

## Why

The owner must change every model prompt without a release, and go back to an earlier text when a
change makes the replies worse (#10).

## What Changes

- The server knows six prompts. Topic: Character reply, Step check, Hints, Review. Calibration:
  Answer check, Sam's reply.
- Each prompt lists the variables that the server substitutes, such as `{character.name}`.
- The server does not seed the texts. A prompt has no text until the owner saves its first version.
- Save makes a new version of a prompt, and the new version is current at once.
- Restore makes an older version current again.
- The admin API lists the prompts, reads one prompt with its versions, saves a version and restores
  a version, behind the admin key.
- The admin app gets the Prompts screen: the list grouped as Topic and Calibration, the editor, the
  variables and Versions. The sidebar shows Prompts in the group Rules.

Out of scope:

- The use of the prompts and the substitution of the variables: #12, #13 and #14.
- The calibration engine.
- Drafts and saved test conversations. Try at a topic is #15.
- A note for each version. The canvas shows one, but the issue does not ask for it.
- A check of the variables in the text.

## Capabilities

### New Capabilities

- `prompts`: the model prompts, their versions, the admin API that serves them, and the admin
  screen where the owner edits them.

### Modified Capabilities

None.

## Impact

- A new Gradle module `server/feature-prompts`.
- The Flyway migration `V4__prompts.sql`. #7 takes `V2`, #9 takes `V3`.
- `exposed-java-time` for the time of a version.
- `contract-admin` gets the prompt DTOs.
- `admin` gets the Prompts screen and its data layer.
