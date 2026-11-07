# Tasks

## 1. Server persistence

- [ ] 1.1 Add `server/feature-levels` with the level model, the table rule and the ranges; verify with the model tests for each rejection and for the ranges
- [ ] 1.2 Add `V3__levels.sql` and the Exposed repository; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 2. Server admin API

- [ ] 2.1 Add the read and the save use cases, the level DTOs in `contract-admin`, the routes, and mount them in `app` behind the admin key; verify with the use-case tests for a rejected table and with the route tests for 200, 400 and 401

## 3. Admin data layer

- [ ] 3.1 Add the admin levels repository; verify with mock engine tests for 200, 400, a call without saved settings and a failed call

## 4. Admin Levels

- [ ] 4.1 Add `GetLevelsUseCase`, `SaveLevelsUseCase` and `LevelsViewModel`; verify with the mapper and view-model tests for empty and saved levels, the chosen level, the Saved status, the rejected save, the unread number, the save failure and the load error
- [ ] 4.2 Add `AdminSelect`, the mono style, the Levels screen and the sidebar group Rules; verify with `./gradlew build` and the images of the screen capture
- [ ] 4.3 Run the admin app against a server with a database; verify a first save of seven levels, a rejected save and the levels after a server restart

## Workflow follow-up

- Run `/opsx:archive` as the last commit of the last PR for #9, so `openspec/specs/rating-levels/spec.md` updates at the merge.
