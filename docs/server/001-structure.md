# Server Structure

The server uses feature-first Gradle modules. A feature keeps its product scenarios together, and
shared infrastructure stays feature-independent.

## Modules

- `app` owns the process lifecycle, the settings from environment variables and `.env`, shared
  Ktor plugins, error mapping, `/health`, the admin key check, and manual graph construction.
- `core-database` owns the connection, the migration bootstrap, and transactions, not feature tables.
- `core-llm` owns the model provider interface and its adapters.
- `feature-*` owns one product slice, such as `feature-characters` or `feature-topics`, and may
  depend on `core-*`.

Core modules never depend on features. Features never depend on `app`. A feature uses another
feature only through that feature's use cases, and the dependency has no cycle.

A module that needs settings owns their type, such as `DatabaseConfig` in `core-database`. `app`
reads the values and builds it at start.

Each module lives under `server/`, for example `server/app`.

## Feature shape

Use this as a menu, not an empty package template:

```text
feature-topics/
├── api/          routes and server-only HTTP contracts
├── model/        framework-free values, rules, and failures
├── usecase/      one class per business scenario
└── persistence/  repository, tables, and migrations
```

## Use cases

A use case is one business scenario: what the owner or a user does, in the words of the issue or
the spec.

```kotlin
internal class PublishTopicUseCase(
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(id: TopicId): PublishOutcome = ...
}
```

- Name it as an action plus `UseCase`: `PublishTopicUseCase`, `GetCharacterUseCase`.
- Every route of a business scenario calls a use case, also for a plain read or write. A use case
  that only calls the repository is correct: the scenario grows there later.
- A technical route, such as `/health` or `GET /admin/server`, is not a business scenario. It
  answers in `app` without a use case.
- Give it one public `suspend operator fun invoke`. Take dependencies through the constructor.
- Do not write a `*Service` class that collects several scenarios.
- Put a rule that two use cases need into `model/`, as a function or a class named after the rule,
  such as `TopicPublishability`. Do not put it into a base class or a helper service.
- A use case may call another use case when its scenario includes that one. Do not create cycles.
- Return typed outcomes for expected failures. Throw only for broken invariants.

## Routes and adapters

- Routes validate and translate HTTP only, then call a use case.
- Use [shared DTOs](../shared/README.md) for admin payloads. Keep server-only HTTP contracts beside
  their routes.
- The route answers each outcome of its use case with a status, in a `when` over the outcome:
  `is UpdateCharacterOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)`. Do not turn an
  outcome into an exception.
- A request that does not parse ends in a Ktor exception: `BadRequestException` for a body that is
  not the DTO, and `NotFoundException` for a path identifier that is not a UUID. `app` maps these
  exceptions to statuses in one place.
- `app` mounts admin routes behind the admin key check. A route never checks the key itself.
- Do not expose provider exceptions, rows, or framework types as feature models.
- Keep provider-specific configuration in `core-llm`.
- Preserve coroutine cancellation, and translate adapter failures at their boundary.
- Default declarations to `internal`; widen visibility for a real boundary only.
- Wire dependencies manually in `app`.

Persistence rules follow [Server Persistence](002-persistence.md).
