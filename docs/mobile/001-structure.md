# Structure

`mobile/shared` is one Kotlin Multiplatform module. It keeps the module layout of yap-app as
packages: one Gradle module builds faster and exports one iOS framework.

```text
mobile/shared/src/commonMain/kotlin/app/yap/mobile/
├── app/                 Koin composition root
├── core/
│   ├── common/
│   └── network/
└── feature/
    └── topic/
        ├── api/         use-case contracts and entities other features call
        └── impl/
            ├── data/
            ├── di/
            ├── domain/
            └── presentation/
mobile/ios/              Xcode project, SwiftUI screens
```

## Feature boundaries

- A feature imports another feature's `api` only, never its `impl`. `internal` cannot enforce this
  inside one module, so an ast-grep rule fails CI on such an import.
- `api` holds only use-case contracts that another feature calls, and the entities in their
  signatures. A use case that only its own feature calls stays in `impl`. A feature that nobody
  calls has no `api` package.
- `api` holds no navigation: SwiftUI owns it.
- Two features that need each other's `impl` are one feature.

## Layers inside `impl`

```text
                  di
             /    |    \
            v     v     v
presentation --> domain <-- data
```

- `presentation` depends on `domain`, never on `data`.
- `data` implements ports owned by `domain`.
- `domain` depends on neither.
- Common code stays platform-neutral; platform source sets provide narrow adapters.
- Do not create a layer or abstraction before it owns behavior or a required boundary.

## Swift surface

Kotlin/Native exports every `public` declaration of `shared` into the iOS framework.

- Default declarations to `internal`.
- Make public only what Swift uses: view models with their `UiState`, `News`, and `Event`, and the
  `app` entry points that start Koin.
- SKIE exposes `Flow` as an `AsyncSequence` and sealed types as Swift enums. Do not write manual
  callback wrappers for Swift.

## Implementation order

1. Define behavior, state, outcomes, and ownership.
2. Add `api` only for contracts another feature calls.
3. Define domain ports, then implement data, mapping, and repositories.
4. Add view models and presentation mapping.
5. Declare the feature's Koin module.
6. Build the SwiftUI screen.
7. Add focused tests and verify the iOS build.
