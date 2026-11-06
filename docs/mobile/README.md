# Mobile App

These documents define the implementation rules under `mobile/`. The app is iOS first: logic and
view models are Kotlin Multiplatform in `mobile/shared`, screens are SwiftUI in `mobile/ios`.
Android may come later on the same `shared`. All Kotlin in this area also follows
[Code conventions](../001-code-conventions.md).

| Document | Read when changing |
| --- | --- |
| [Structure](001-structure.md) | packages, `api`/`impl`, layers, the Swift surface, or implementation order |
| [Domain](002-domain.md) | entities, repository ports, typed results, or use cases |
| [Dependency injection](003-dependency-injection.md) | Koin modules, bindings, the composition root, or scopes |
| [Repositories](data/001-repositories.md) | repository shape, naming, cache, observation, concurrency, or mutations |
| [Data sources](data/002-data-sources.md) | DTOs, mapping, protocol errors, or networking |
| [View models](presentation/001-view-models.md) | screen contracts, `UiState`, `News`, or events |
| [SwiftUI](presentation/002-swiftui.md) | what a SwiftUI screen does and what stays in Kotlin |

The repository has no `mobile/` code yet. The guides name the helpers the first feature adds:
`ApiClient`, `coreNetworkModule`, `runSuspendCatching`, `mapState`, `UserDataCleaner`, Room, and the
ast-grep boundary rule. See the note in [Documentation](../README.md).

Test rules live in [Testing](../testing/README.md).
