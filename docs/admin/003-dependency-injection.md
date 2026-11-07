# Dependency Injection

Koin declares each graph node once and resolves it by contract type. The rules of the
[server guide](../server/003-dependency-injection.md) apply. This guide lists what the admin app
adds.

```kotlin
internal fun featureCharactersModule(): Module = module {
    singleOf(::RemoteCharacterRepository) { bind<CharacterRepository>() }

    factoryOf(::DefaultGetCharactersUseCase) { bind<GetCharactersUseCase>() }

    factoryOf(::CharactersViewModel)
}
```

- The admin app is one Gradle module. Each owner package declares one internal Koin module
  function in its root package: `coreNetworkModule()`, `feature<Name>Module()`, and
  `navigationModule()`.
- Use `single` for repositories, the HTTP client, and the preferences node. Use `factory` for use
  cases and view models.
- A view model that takes a value from its screen, such as `CharacterViewModel` and its
  `characterId`, reads the value from the parameters of a lambda definition:
  `factory { (characterId: CharacterId?) -> CharacterViewModel(...) }`. Koin finds a parameter of
  `factoryOf` by its type, and it cannot find `null`.
- A screen and a view model take constructor or function parameters and never see `Koin`.

## Composition root

`Main.kt` owns it. `appModules()` lists every module. `main` starts Koin with `koinApplication`,
resolves each view model once, and passes it to `AdminShell`. A screen with a parameter gets a
function, such as `(CharacterId?) -> CharacterViewModel`. No other code touches Koin.

Closing the window closes Koin, and Koin runs every `onClose`: the HTTP client closes.

## Tests

`AppModulesTest` runs `verify()` on `appModules()`, so a missing binding fails the build, not the
start. The test names each type that no definition gives: the HTTP engine, which
`adminHttpClient()` makes, and each parameter that a screen passes to its view model.
