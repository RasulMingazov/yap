# Structure

The mobile Kotlin code uses feature-first Gradle modules, like the server. A feature keeps its
product scenarios together, and shared infrastructure stays feature-independent. One umbrella
module, `shared`, exports all of them to iOS as one framework.

## Modules

```text
mobile/
├── shared/              umbrella: the iOS framework and the Koin composition root (`app`)
├── core-common/
├── core-network/
├── feature-topic/
│   └── src/commonMain/kotlin/app/yap/mobile/feature/topic/
│       ├── api/         use-case contracts and entities other features call
│       └── impl/
│           ├── data/
│           ├── di/
│           ├── domain/
│           └── presentation/
└── ios/                 Xcode project, SwiftUI screens
```

- `shared` owns the `app` package: `appModules(...)`, `initKoin(...)` and the view model factories
  for Swift. It holds no feature code.
- `core-*` owns infrastructure, such as the Ktor client in `core-network`.
- `feature-*` owns one product slice, such as `feature-topic`, and may depend on `core-*`.

Core modules never depend on features. Features never depend on `shared`. A feature uses another
feature only through that feature's `api`, and the dependency has no cycle.

Each module lives under `mobile/`, for example `mobile/feature-topic`.

## One iOS framework

The iOS app links only `shared`. Two Kotlin frameworks would each carry their own runtime and
their own copy of the common types, and Swift could not pass a value from one to the other.

- `shared` depends on every `feature-*` module and lists each one with a screen in `export(...)`.
- `core-*` modules are not exported.

## Feature boundaries

- `api` holds the public declarations of a feature: the use-case contracts that another feature
  calls, and the entities in their signatures. Everything in `impl` is `internal`, so the compiler
  stops another feature from using it.
- A use case that only its own feature calls stays in `impl`. A feature that nobody calls has no
  `api` package.
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

Kotlin/Native puts every `public` declaration of an exported module into the iOS framework.

- Default declarations to `internal`.
- Swift uses only view models with their `UiState`, `News`, and `Event`, and the `app` entry
  points that start Koin.
- Mark a public declaration that Swift does not use with `@HiddenFromObjC`: the `api` contracts and
  the Koin module functions that `shared` calls.
- SKIE exposes `Flow` as an `AsyncSequence` and sealed types as Swift enums. Do not write manual
  callback wrappers for Swift.

## Implementation order

1. Define behavior, state, outcomes, and ownership.
2. Add the feature module to `settings.gradle.kts`, and to the dependencies and `export(...)` of
   `shared`.
3. Add `api` only for contracts another feature calls.
4. Define domain ports, then implement data, mapping, and repositories.
5. Add view models and presentation mapping.
6. Declare the feature's Koin module.
7. Build the SwiftUI screen.
8. Add focused tests and verify the iOS build.
