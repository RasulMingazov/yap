# Proposal

## Why

Topics need recurring interlocutors. The owner keeps a cast of characters, so the same character
appears in many topics and sounds the same in each of them (#6).

## What Changes

- The server stores characters: the name, the history, the personality and the manner of speech.
  A character stays after a server restart.
- The admin API lists, reads, creates and updates characters behind the admin key.
- The admin app gets the «Персонажи» list and the «Персонаж» form with «Новый персонаж» and
  «Сохранить».
- The admin app gets a sidebar. It moves between «Персонажи» and the server settings.

Out of scope:

- The number of topics in each row of «Персонажи». Topics do not exist yet: the change for #7 adds
  the number together with the topics.
- «Поговорить» on the «Персонаж» form: #16.
- Delete: the issue leaves it out.
- The role in a scene, such as a taxi driver: the topic owns it, #7.

## Capabilities

### New Capabilities

- `characters`: the cast of recurring characters, the admin API that serves it, and the admin
  screens where the owner keeps it.

### Modified Capabilities

None.

## Impact

- A new Gradle module `server/feature-characters`.
- `server/core-database` gets Exposed 1.6.0 and a suspending transaction for feature repositories.
- The first Flyway migration of the server, `V1__characters.sql`.
- `contract-admin` gets `CharacterDto` and `CharacterProfileDto`.
- `admin` gets the sidebar, the characters screens and their data layer.
