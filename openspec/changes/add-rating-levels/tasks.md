# Tasks

One group is one PR, in merge order. (hot) marks a hot file. Paths start at the package root of
the module, main or test.

## 1. [1/7] The plan

- [ ] 1.1 Propose the change; verify with `openspec validate add-rating-levels --strict`

## 2. [2/7] Server model and persistence

Needs: the `V2` part of #7 is merged.

Files: `settings.gradle.kts` (hot), `server/feature-levels/build.gradle.kts`,
`server/feature-levels/` `model/` and `persistence/`,
`server/feature-levels/src/main/resources/db/migration/V3__levels.sql` (hot),
`server/feature-levels/src/testFixtures/` (`StubLevel`), and their tests.

- [x] 2.1 Add `server/feature-levels` with the level model, the table rule and the ranges; verify with the model tests for a missing level, a level twice, an empty or blank text, zero words, a rating from on No rating, a missing rating from, a rating from that does not grow, and the ranges
- [x] 2.2 Add `V3__levels.sql` and the Exposed repository that saves with an upsert by `code`; verify with the PostgreSQL tests for an empty read, a save, a second save and the clean bootstrap (CI runs them)

## 3. [3/7] Server admin API

Files: `server/feature-levels/` `usecase/` and `api/`, `contract-admin/` level DTOs (hot),
`server/feature-levels/src/testFixtures/` `StubLevelRepository` (moved from [2/7], its first consumer),
`server/app/build.gradle.kts`, `server/app/.../Application.kt` (hot),
`server/app/src/test/.../ApplicationTest.kt` and `server/app/src/test/.../admin/ServerRoutesTest.kt`
(the new `serverModule` parameter), and their tests.

- [x] 3.1 Add the read and the save use cases and `GetLevelRulesUseCase`; verify with the use-case tests for a rejected table, and for the value of `{level.rules}` after a save and before a save
- [x] 3.2 Add the level DTOs in `contract-admin`, the mapper that reads names with `entries.find`, the routes, and mount them in `app` behind the admin key; verify with the route tests for 200, 400 for an unknown code and for a rejected table, 401, and the JSON

## 4. [4/7] Admin data layer

Files: `admin/` `feature/levels/domain/` (models and repository interface) and
`feature/levels/data/` (mapper and remote repository), and their tests.

Slice 1 of this part holds the shared helpers: `admin/` `core/common/LoadResult.kt` (moved from
`feature/characters/domain/`), the new `core/network/ServerCall.kt`, the switch of
`feature/characters/` to them, and the import in `ScreenCaptureTest.kt`. Slice 2 holds the levels
files above.

- [x] 4.1 Add the admin levels repository; verify with mock engine tests for 200, 400, a call without saved settings and a failed call

## 5. [5/7] Admin view model

Slice 1 of this part holds the use cases, the state types nested in `LevelsViewModel`, and
`LevelsUiStateMapper` with its tests. Slice 2 holds the events and the logic of `LevelsViewModel`,
the stub use cases, the view-model tests and `featureLevelsModule()` in `AppModules.kt`.

Files: `admin/` `feature/levels/domain/usecase/`, `feature/levels/presentation/`
(`LevelsViewModel`, `LevelsUiStateMapper`), `feature/levels/FeatureLevelsModule.kt`, `AppModules.kt`, and their tests.

- [ ] 5.1 Add `GetLevelsUseCase`, `SaveLevelsUseCase` and `LevelsViewModel`; verify with the mapper and view-model tests for empty and saved levels, B1 on the first open, a click on a level, the Saved status, the rejected save, the unread number, the save failure and the load error

## 6. [6/7] Admin Levels screen and the group Rules

Needs: the part of #7 with the actions slot of `ScreenToolbar` is merged.

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
