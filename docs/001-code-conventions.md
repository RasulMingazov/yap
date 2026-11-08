# Code Conventions

Project-wide additions to the official Kotlin coding conventions. Everything the Kotlin style guide already covers is out of scope here.

## Names

Name a thing by what it is, not by where it shows or how it is built.

- `UpNext`, not `Home`; `fetched`, not `remote`, for a stored row; `topicId`, not `id`.
- Name lambda parameters after the entity: `topics ->`, not `it` or `list ->`.
- Remove words that no longer match the model, such as `catalog` or `episode`: the product calls it a topic, so the code says `topic`.

## Remove what has no use now

- Delete unused colors, constants, columns, and parameters.
- Delete flags and guards that cannot trigger.
- Delete wrappers that only rethrow and one-line private helpers that only rename a call.
- Add a contract, use case, or entity only together with its first consumer. Do not build for a later slice.

## Formatting

- Put each call of a chain on its own line.
- Put each named argument on its own line.

## `when` on subtypes

When a `when` dispatches on subtypes, use `is` for every branch — including singletons: `is LoginResult.Cancelled`, not `LoginResult.Cancelled`. Do not mix a value branch with type branches in the same dispatch.

```kotlin
when (result) {
    is LoginResult.Cancelled -> onCancelled()
    is LoginResult.Failure -> onFailure(result)
}
```

Equality branches stay appropriate when the dispatch is genuinely about enum values, constants, or strings.

## Alphabetical ordering

Prefer alphabetical order within the same semantic group: constructor parameters, injected dependencies and stored properties, function parameters with no meaningful domain order, named arguments, fields in presentation state and test builders, and sibling bindings with equivalent roles.

```kotlin
internal class LoginViewModel(
    private val authenticateUseCase: AuthenticateUseCase,
    private val navigator: Navigator,
    private val observeSessionUseCase: ObserveSessionUseCase,
)
```

Alphabetize required and defaulted parameters within their own groups. Do not alphabetize when order is part of a wire format, serialization contract, database schema, public compatibility requirement, lifecycle sequence, UI order, or domain narrative.

## Declaration order

Read a class top to bottom without jumping. Public API comes first, then private helpers in the
order the code above them calls, depth first: a helper sits below its only caller, and a helper
called from two places sits below the later one. Here `login` calls `requestLogin`, then `outcomeOf`.

```kotlin
override suspend fun login(): LoginOutcome = ...

private suspend fun requestLogin(nonce: String): ApiResult<LoginDto> = ...

private suspend fun outcomeOf(result: ApiResult<LoginDto>): LoginOutcome = ...
```

This outranks alphabetical ordering, which applies to declarations with no call relationship.
Nested types and `companion object` stay at the bottom.

In a view model, a private holder sits directly above the public stream it backs, with no blank line
between them, so each pair reads as one unit: `dataState` then `uiState`, then the news channel then
`news`. Properties before `init`, `init` before functions.

```kotlin
private val dataState = MutableStateFlow(DataState())
val uiState: StateFlow<UiState> = dataState.mapState(...)

private val newsChannel = Channel<News>(Channel.BUFFERED)
val news: Flow<News> = newsChannel.receiveAsFlow()
```

A blank line separates one pair from the next, never the two halves of a pair.

## Extension functions

- Write an extension for a type you do not own, such as `Modifier.pullStart()`, or for a mapping
  that belongs to another layer, such as `CharacterDto.toDomain()`.
- Put the behavior of your own type into the type as a member, such as `val isComplete`.
- Give a function one job. Cleaning input and checking a rule are two functions.
- Return a typed outcome, not `null`, when the caller must know why the input fails.

## Early returns

Prefer guard clauses when they remove nesting and make preconditions visible before the happy path. Do not split a short, already-clear expression into several returns, do not use labeled or non-local returns that obscure control flow, and preserve exhaustive `when` expressions when every outcome must be handled.

## Comments

Kotlin sources, `.kt` and `.kts` alike, carry no explanatory comments or KDoc, tests included. Say it through names, types, and small functions instead; a reason that code cannot express belongs in the commit message or the owning guide. Comments the toolchain acts on, such as `@file:Suppress` or generated-file markers, stay. Comments in non-Kotlin files are out of scope.
