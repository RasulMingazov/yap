# Proposal

## Why

The owner must change every model prompt without a release, and go back to an earlier text when a
change makes the replies worse (#10).

## What Changes

- The server knows six prompts. Topic: Character reply, Step check, Hints, Review. Calibration:
  Answer check, Sam's reply.
- Each prompt lists the variables that the server substitutes, such as `{interlocutor}`. The design
  gives the value of each variable for #12, #13 and #14.
- The server does not seed the texts. A prompt has no text until the owner saves its first version.
- Save makes a new version of a prompt, and the new version is current at once.
- Restore makes an older version current again.
- The admin API lists the prompts, reads one prompt with its versions, saves a version and restores
  a version, behind the admin key.
- The admin app gets the Prompts screen: the list grouped as Topic and Calibration, the editor, the
  variables and Versions. The sidebar shows Prompts in the group Rules that #9 adds. Save uses the
  toolbar actions slot that #7 adds.

Out of scope:

- The use of the prompts and the substitution of the variables: #12, #13 and #14.
- Drafts and saved test conversations. Try at a topic is #15.
- No issue, not planned, for the owner to confirm: the calibration engine, a note for each version
  (the canvas shows one, the issue does not ask for it), and a check of the variables in the text.

## Capabilities

### New Capabilities

- `prompts`: the model prompts, their versions, the admin API that serves them, and the admin
  screen where the owner edits them.

### Modified Capabilities

None.

## Impact

- A new module `server/feature-prompts` with `V4__prompts.sql`, after `V2` of #7 and `V3` of #9.
- `contract-admin` gets the prompt DTOs. `admin` gets the Prompts screen and its data layer.
- The parts wait for parts of #7 and #9, as `tasks.md` says.
