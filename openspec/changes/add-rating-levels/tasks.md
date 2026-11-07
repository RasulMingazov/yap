# Tasks

One group is one PR, in merge order. (hot) marks a hot file of `docs/process/README.md`. Paths
under `server/feature-levels/` start at `src/main/kotlin/app/yap/server/feature/levels/` or the
matching test folder. Paths under `admin/` start at `src/main/kotlin/app/yap/admin/` or the matching
test folder.

## 1. [1/7] The plan

Files: `openspec/changes/add-rating-levels/`.

- [ ] 1.1 Propose the change; verify with `openspec validate add-rating-levels --strict`

## 2. [2/7] Server model and persistence

Needs: the part of #7 with `V2__topics.sql` is merged. The migrations deploy as `V2` (#7), `V3`
(#9), `V4` (#10).

Files: `settings.gradle.kts` (hot), `server/feature-levels/build.gradle.kts`,
`server/feature-levels/` `model/` and `persistence/`,
`server/feature-levels/src/main/resources/db/migration/V3__levels.sql` (hot),
`server/feature-levels/src/testFixtures/` (stub repository), and their tests.

- [ ] 2.1 Add `server/feature-levels` with the level model, the table rule and the ranges; verify with the model tests for a missing level, a level twice, an empty or blank text, zero words, a rating from on No rating, a missing rating from, a rating from that does not grow, and the ranges
- [ ] 2.2 Add `V3__levels.sql` and the Exposed repository that saves with an upsert by `code`; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 3. [3/7] Server admin API

Files: `server/feature-levels/` `usecase/` and `api/`, `contract-admin/` level DTOs (hot),
`server/app/build.gradle.kts`, `server/app/.../Application.kt` (hot),
`server/app/src/test/.../ApplicationTest.kt` and `server/app/src/test/.../admin/ServerRoutesTest.kt`
(the new `serverModule` parameter), and their tests.

- [ ] 3.1 Add the read and the save use cases and `GetLevelRulesUseCase`; verify with the use-case tests for a rejected table, and for the value of `{level.rules}` after a save and before a save
- [ ] 3.2 Add the level DTOs in `contract-admin`, the mapper that reads names with `entries.find`, the routes, and mount them in `app` behind the admin key; verify with the route tests for 200, 400 for an unknown code and for a rejected table, 401, and the JSON

## 4. [4/7] Admin data layer

Files: `admin/` `feature/levels/domain/` (models and repository interface) and
`feature/levels/data/` (mapper and remote repository), and their tests.

- [ ] 4.1 Add the admin levels repository; verify with mock engine tests for 200, 400, a call without saved settings and a failed call

## 5. [5/7] Admin view model

Files: `admin/` `feature/levels/domain/usecase/`, `feature/levels/presentation/`
(`LevelsViewModel`, `LevelsUiStateMapper`), `feature/levels/di/`, and their tests.

- [ ] 5.1 Add `GetLevelsUseCase`, `SaveLevelsUseCase` and `LevelsViewModel`; verify with the mapper and view-model tests for empty and saved levels, B1 on the first open, a click on a level, the Saved status, the rejected save, the unread number, the save failure and the load error

## 6. [6/7] Admin Levels screen and the group Rules

Needs: the part of #7 that adds the actions slot of `ScreenToolbar` is merged. Save uses that slot.
This part adds no second slot.

Files: `admin/` `core/design/AdminSelect.kt` (hot), `core/design/AdminTheme.kt` (hot, the mono
style, unless #10 added it first), `feature/levels/presentation/ui/LevelsScreen.kt`, `Main.kt`
(hot), `navigation/NavigationViewModel.kt` (hot, `Screen.Levels`),
`navigation/NavigationUiStateMapper.kt` (hot), `navigation/ui/Sidebar.kt` (hot, the group Rules),
`navigation/ui/AdminShell.kt` (hot), `admin/src/test/.../ScreenCaptureTest.kt`, and the navigation
tests. #7 and #10 change the same navigation and design files.

- [ ] 6.1 Add `AdminSelect`, the mono style and the Levels screen with Save in the toolbar; verify with `./gradlew build` and the images of the screen capture for each state
- [ ] 6.2 Add the sidebar group Rules with Levels between Content and Settings; verify with the navigation view-model test for Levels
- [ ] 6.3 Run the admin app against a server with a database; verify a first save of seven levels, a rejected save and the levels after a server restart

## 7. [7/7] Archive

Files: `openspec/changes/add-rating-levels/`, `openspec/specs/rating-levels/spec.md`.

- [ ] 7.1 Run `/opsx:archive` as the last commit of the last PR for #9, so `openspec/specs/rating-levels/spec.md` updates at the merge; verify with `openspec validate --strict`
