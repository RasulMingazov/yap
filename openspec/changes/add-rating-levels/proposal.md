# Proposal

## Why

The interlocutor must speak to each rating level in its own way. The owner tunes each level in the
admin app, without a release (#9).

## What Changes

- The server keeps seven levels: A1, A2, B1, B2, C1, C2 and No rating. No rating is for a learner
  without a rating yet, and for the calibration.
- Each level has a rating from, a vocabulary, the maximum words in a sentence, an idioms setting and
  a prompt block. No rating has no rating from.
- The server derives the rating range of each level from the rating from of the next level.
- The server does not seed the values. The levels are empty until the owner saves them.
- The admin API reads all levels and replaces all levels in one call, behind the admin key.
- The server gives the saved block of a level as the value of `{level.rules}`.
- The admin app gets the Levels screen: the table of levels, the prompt block of the chosen level,
  and Save. The sidebar gets the group Rules with Levels.

Out of scope:

- The substitution of `{level.rules}` into the reply and hints prompts: #12 and #13. Nothing builds
  a prompt before #12. #12 owns the end-to-end check of the issue.
- The calculation of the rating.
- The Model column of the table: #11. It adds an optional model to the level values.
- Prompts in the group Rules: #10 adds it to the group that this change adds.

## Capabilities

### New Capabilities

- `rating-levels`: the levels of the rating, the admin API that serves them, and the admin screen
  where the owner tunes them.

### Modified Capabilities

None.

## Impact

- A new Gradle module `server/feature-levels`.
- The Flyway migration `V3__levels.sql`, deployed after `V2` (#7) and before `V4` (#10).
- `contract-admin` gets the level DTOs.
- `admin` gets the Levels screen and its data layer, and the sidebar group Rules.
