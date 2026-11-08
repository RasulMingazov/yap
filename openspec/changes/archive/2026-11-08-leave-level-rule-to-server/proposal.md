# Proposal

## Why

The admin app is a thin client, but it checked the level rule itself. The server already checks it.

## What Changes

- The admin app reads only the numbers and the idioms choice. It sends the rest, and shows the 400
  error of the server. Out of scope: the Model column of Levels (#11).

## Capabilities

### Modified Capabilities

- `rating-levels`: the admin app no longer checks the level rule before a save.
