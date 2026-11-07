# Tasks

## 1. Server persistence

- [ ] 1.1 Add `server/feature-prompts` with the prompt keys, their groups and variables, and the version model; verify with `./gradlew :server:feature-prompts:build`
- [ ] 1.2 Add `exposed-java-time`, `V4__prompts.sql` and the Exposed repository; verify with the PostgreSQL tests for a read before a save, two saves, a restore, a save after a restore, an unknown version and the clean bootstrap (CI runs them)

## 2. Server admin API

- [ ] 2.1 Add the list, read, save and restore use cases, the prompt DTOs in `contract-admin`, the routes, and mount them in `app` behind the admin key; verify with the use-case tests for the trimmed and the empty text and with the route tests for 200, 201, 400, 401 and 404

## 3. Admin data layer

- [ ] 3.1 Add the admin prompts repository; verify with mock engine tests for 200, 201, 400, 404, a call without saved settings and a failed call

## 4. Admin Prompts

- [ ] 4.1 Add the use cases and `PromptsViewModel`; verify with the mapper and view-model tests for the first open, a prompt without a version, the typed text on a return, the Saved status, the empty text, Restore, the save failure and the load error
- [ ] 4.2 Add `Chip`, the Prompts screen and Prompts in the sidebar group Rules; verify with `./gradlew build` and the images of the screen capture
- [ ] 4.3 Run the admin app against a server with a database; verify a first save, a second save, a restore and the versions after a server restart

## Workflow follow-up

- Run `/opsx:archive` as the last commit of the last PR for #10, so `openspec/specs/prompts/spec.md` updates at the merge.
