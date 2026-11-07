# Dependency Injection

Koin declares each graph node once and resolves it by contract type.

```kotlin
fun featureCharactersModule(): Module = module {
    singleOf(::DatabaseCharacterRepository) { bind<CharacterRepository>() }

    factoryOf(::CreateCharacterUseCase)
    factoryOf(::UpdateCharacterUseCase)
}
```

- A Gradle module that owns collaborators declares one public Koin module function in its root
  package: `core<Name>Module()` for a core module, `feature<Name>Module()` for a feature. Add it
  together with the first consumer of its bindings.
- Bind by contract type. Declare the implementation with `singleOf` or `factoryOf` and expose its
  contract with `bind<Contract>()`. Consumers ask for the contract, never for the implementation.
- Prefer that constructor form to a lambda such as `single<Contract> { Implementation(get()) }`:
  `verify()` reads the constructor of the declared class and cannot see inside a lambda. Use a
  lambda only for a value that no public constructor builds, such as `openDatabase(config)`.
- Use `single` for repositories and adapters, and `factory` for use cases.
- Pass configuration as module-function parameters, such as `coreDatabaseModule(config)`. A module
  never reads the environment.
- A definition that opens a resource sets `createdAtStart = true`, so a failure stops the start,
  and releases the resource with `onClose`.
- Resolve with `get()` only inside a definition. A use case, a repository, and a route function
  take constructor or function parameters and never see `Koin`.

## Composition root

`app` owns it. `appModules(config)` lists every module. `serverModule(config, modules)` installs
Koin, resolves each use case, and passes it to its route function. No other code touches Koin.

Koin stops with the application and runs every `onClose`, so the server needs no shutdown hook.

## Tests

- A test starts the app with `serverModule(config, modules)` and stub modules: the feature module
  and a module that binds the repository to a stub. A later binding overrides an earlier one.
- `AppModulesTest` runs `verify()` on `appModules`, so a missing binding fails the build, not the
  start. Add a new module to `appModules` and the test covers it.
