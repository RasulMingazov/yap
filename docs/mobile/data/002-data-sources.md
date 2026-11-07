# Data Sources

A data source stays specific to one external protocol, so infrastructure representations never
leak into domain contracts.

## Representations

- Wire models come from `contract-mobile`, which the server shares, and end in `Dto`. Follow
  [Shared Contracts](../../shared/README.md).
- Persisted models end in `Db`.
- No data source accepts or returns a domain entity.
- Keep HTTP, JSON, storage, and platform SDK behavior beside its source.
- Do not add a forwarding `RemoteDataSource` or `LocalDataSource` with no behavior.

## Local storage

- Use Room for structured local data.
- Inject the DAO, not the database.
- Put a multi-step write in one `@Transaction` DAO method.
- Do not pass the user ID through repositories.

## Mapping

- Convert data representations to domain only at repository boundaries.
- Put pure extensions in `data/mapper`, grouped by the domain model they produce: `toDomain()`
  and `toDb()`.
- For a cached response, map the DTO to storage and build the domain model from storage. Build it
  before writing, so a malformed response fails before it reaches the cache.
- Apply serialization annotations only to DTOs and persisted models.

## Errors

- Always rethrow `CancellationException`; `runSuspendCatching` from `core-common` does.
- Never expose protocol, storage, or provider errors through repository contracts.
- A failed request must not erase the last successful cache.

## Networking

The app owns one Ktor client, declared by `coreNetworkModule(baseUrl)`. Feature data sources
resolve `ApiClient`, which wraps it; they neither build nor close it.

- Mark authenticated requests with `authenticated()` instead of adding headers by hand.
- The shared client refreshes the token and retries once after `401`. Repositories and sources do
  not.

## Verification

Test protocol behavior and serialization at the source boundary, and non-trivial mapping
separately. Follow [Test Structure](../../testing/001-structure.md).
