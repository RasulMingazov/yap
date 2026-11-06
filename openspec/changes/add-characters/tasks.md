# Tasks

## 1. Server persistence

- [ ] 1.1 Connect Exposed 1.6.0 in `core-database` and give features a suspending transaction; verify with `./gradlew :server:core-database:build`
- [ ] 1.2 Add `server/feature-characters` with the character model and the profile rule; verify with the model tests for trimmed and empty fields
- [ ] 1.3 Add the `characters` table, `V1__characters.sql` and the Exposed repository; verify with the PostgreSQL tests for create, read, update, the name order and the clean bootstrap (CI runs them)

## 2. Server admin API

- [ ] 2.1 Add `CharacterDto` and `CharacterProfileDto` to `contract-admin`; verify with `./gradlew :contract-admin:build`
- [ ] 2.2 Add the list, read, create and update use cases; verify with the use-case tests for the trimmed profile, the rejected profile and the unknown character
- [ ] 2.3 Add the routes and mount them in `app` behind the admin key; verify with the route tests for 200, 201, 400, 401 and 404

## 3. Admin data layer

- [ ] 3.1 Add the admin characters repository that reads the saved server settings for each call; verify with mock engine tests for 200, 400, 404 and a failed call
- [ ] 3.2 Add the admin use cases for the list, one character and the save; verify with `./gradlew :admin:build`

## 4. Admin sidebar and «Персонажи»

- [ ] 4.1 Add the shell with the sidebar groups «Контент» and «Настройки» and the start screen rule; verify with `./gradlew :admin:run`: the app opens «Сервер» without saved settings and «Персонажи» with them
- [ ] 4.2 Add `CharactersViewModel` and the «Персонажи» screen; verify with the mapper and view-model tests for the rows, the empty list, the load error and «Повторить»

## 5. Admin «Персонаж»

- [ ] 5.1 Add `CharacterViewModel` and the «Персонаж» form; verify with the mapper and view-model tests for a new and a saved character, «Сохранено», «Заполните все поля», the save failure and the load error
- [ ] 5.2 Create and edit a character against a local server, then restart the server and open the character again; verify that the profile stays

## Workflow follow-up

- Run `/opsx:archive` as the last commit of the last PR for #6, so `openspec/specs/characters/spec.md` updates at the merge.
