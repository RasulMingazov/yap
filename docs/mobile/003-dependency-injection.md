# Dependency Injection

Koin declares each graph node once and resolves it by contract type.

```kotlin
@HiddenFromObjC
fun featureTopicModule(): Module = module {
    single { get<ApiClient>().createTopicApi() }
    singleOf(::DefaultTopicRepository) { bind<TopicRepository>() }

    factoryOf(::DefaultGetTopicOfDayUseCase) { bind<GetTopicOfDayUseCase>() }

    factoryOf(::TopicOfDayViewModel)
}
```

- Name the Koin module of a feature `feature<Name>Module()`, and of a core module
  `core<Name>Module()`.
- A Koin module function is public, because `shared` calls it, and hidden from Swift with
  `@HiddenFromObjC`.
- Bind by contract type. Declare the implementation with `singleOf` or `factoryOf` and expose its
  contract with `bind<Contract>()`. Consumers ask for the contract, never for the implementation.
- Prefer that constructor form to a lambda: `verify()` reads the constructor of the declared class
  and cannot see inside a lambda. Use a lambda only for a value that no public constructor builds,
  such as `createTopicApi()`.
- Use `single` for shared instances and `factory` for stateless collaborators and view models.
- Pass configuration as module-function parameters, not through global state.
- Release owned resources with `onClose`; a definition that opens a client, database, or socket
  must close it.
- Resolve with `get()` only inside a definition. Application code receives constructor parameters
  and never sees `Koin`, `Scope`, or `KoinComponent`.

## Composition root

The `app` package of `shared` owns the composition root: `appModules(...)` lists every module and
`initKoin(...)` starts Koin. The iOS app calls `initKoin` once at launch with its configuration,
such as the server address. No other code starts Koin.

Swift gets a view model through one public factory in `app`, such as `ViewModels.topicOfDay()`.
Swift never sees Koin types.

## Scopes

- Application-wide collaborators are root singletons and live for the process.
- Account-owned state lives in feature singletons and is dropped on sign-out: the feature
  registers a `UserDataCleaner` (`core-common`), and `app` runs every cleaner when the session
  ends.

## Verification

Cover module wiring with a Koin `verify()` test, so a missing binding fails the build rather than
the first screen.
