# Dependency Injection

Koin declares each graph node once and resolves it by contract type.

```kotlin
@HiddenFromObjC
fun featureTopicModule(): Module = module {
    single<TopicRepository> { DefaultTopicRepository(api = get<ApiClient>().createTopicApi()) }

    factory<GetTopicOfDayUseCase> { DefaultGetTopicOfDayUseCase(repository = get()) }

    factory { TopicOfDayViewModel(getTopicOfDayUseCase = get()) }
}
```

- Name the Koin module of a feature `feature<Name>Module()`, and of a core module
  `core<Name>Module()`.
- A Koin module function is public, because `shared` calls it, and hidden from Swift with
  `@HiddenFromObjC`.
- Bind by contract type, never by implementation type.
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
