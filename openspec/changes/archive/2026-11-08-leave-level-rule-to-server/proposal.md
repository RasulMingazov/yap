# Proposal

## Why

The admin app is a thin client. It checked the level rule itself: every level filled, and a rating
that grows from A1 to C2. The server already checks this rule and answers 400.

## What Changes

- The admin app reads only the numbers and the idioms choice of the form. It sends nothing when a
  number is not a number or a level has no idioms choice.
- The admin app sends an empty rating from, vocabulary or block to the server. The server rejects
  the save, and the admin app shows the error of the server.

Out of scope:

- The Model column of Levels: #11.

## Capabilities

### Modified Capabilities

- `rating-levels`: the admin app no longer checks the level rule before a save.
