# Tasks

## 1. Server persistence

- [x] 1.1 Connect Exposed 1.6.0 in `core-database` and give features a suspending transaction; verify with `./gradlew :server:core-database:build`
- [x] 1.2 Add `server/feature-characters` with the character model and the profile rule; verify with the model tests for trimmed and empty fields
- [x] 1.3 Add the `characters` table, `V1__characters.sql` and the Exposed repository; verify with the PostgreSQL tests for create, read, update, the name order and the clean bootstrap (CI runs them)

## 2. Server admin API

- [x] 2.1 Add `CharacterDto` and `CharacterProfileDto` to `contract-admin`; verify with `./gradlew :contract-admin:build`
- [x] 2.2 Add the list, read, create and update use cases; verify with the use-case tests for the trimmed profile, the rejected profile and the unknown character
- [x] 2.3 Add the routes and mount them in `app` behind the admin key; verify with the route tests for 200, 201, 400, 401 and 404

## 3. Admin data layer

- [x] 3.1 Add the admin characters repository that reads the saved server settings for each call; verify with mock engine tests for 200, 201, 400, a call without saved settings and a failed call

## 4. Admin sidebar and Characters

- [x] 4.1 Add the shell with the sidebar groups Content and Settings and the start screen rule; verify with `./gradlew :admin:run`: the app opens Server without saved settings and Characters with them
- [x] 4.2 Add `GetCharactersUseCase`, `CharactersViewModel` and the Characters screen; verify with the mapper and view-model tests for the rows, the empty list, the load error and Retry

## 5. Admin Character form

- [x] 5.1 Add `GetCharacterUseCase`, `SaveCharacterUseCase` and `CharacterViewModel`; verify with the use-case test for create and update, and with the mapper and view-model tests for a new and a saved character, the Saved status, the error for an empty field, the save failure and the load error
- [ ] 5.2 Add the Character form screen; verify with `./gradlew :admin:run`: the form opens from a row and from New character
- [ ] 5.3 Create and edit a character against a server with a database, then restart the server and open the character again; verify that the profile stays

## Workflow follow-up

- Run `/opsx:archive` as the last commit of the last PR for #6, so `openspec/specs/characters/spec.md` updates at the merge.
