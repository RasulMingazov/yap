# Domain

Domain expresses product language and policy without frameworks or infrastructure. Keep Koin,
Ktor, serialization, storage, resources, and platform SDKs out of it.

## Use cases

```kotlin
interface GetTopicOfDayUseCase {

    suspend operator fun invoke(): TopicOfDay
}

internal class DefaultGetTopicOfDayUseCase(
    private val repository: TopicRepository,
) : GetTopicOfDayUseCase {

    override suspend fun invoke(): TopicOfDay = repository.getToday(forceUpdate = false)
}
```

- Use the `UseCase` suffix and the `Default...UseCase` implementation name.
- The contract lives in `api` when another feature calls the use case, and stays `internal` in
  `impl` otherwise. The implementation is always `internal` in `impl`.
- Expose every repository operation that presentation uses through a use-case contract. A thin
  delegation is fine: the use case is the presentation test boundary.
- Presentation depends on use cases only and never receives a repository.
- Add a second production implementation only for a real substitution boundary.

## Entities and ports

- Keep entities immutable and enforce only genuine business invariants.
- An entity in an `api` use-case signature belongs to `api`; the rest stay `internal` in `impl`.
- Describe repository ports with domain types and read-only flows. Expose no DTOs, credentials,
  status codes, storage models, or mutable flows.
- Ports stay in `impl`.
- Use typed results only when expected alternatives change caller behavior.

Repository contracts follow [Repositories](data/001-repositories.md).
