# Repositories

The port lives in `impl/domain/repository`; the implementation, mapping, cache, and coordination
live in `impl/data/repository`. A repository never crosses into `api`.

## Boundary

Use one repository per primary domain model or cohesive aggregate.

- Do not model endpoints, screens, DTOs, tables, value objects, commands, or result types as
  repositories.
- Keep operations that mutate one aggregate together.
- Split repositories when state, lifetime, cache policy, or reasons to change are independent.

## Naming

| Shape | Observe | Fetch |
| --- | --- | --- |
| Collection | `observeAll(): Flow<List<T>>` | `getAll(forceUpdate: Boolean): List<T>` |
| Current value | `observe(): Flow<T?>` | `get(forceUpdate: Boolean): T?` |
| Value by ID | `observeById(id: Id): Flow<T?>` | `getById(id: Id, forceUpdate: Boolean): T?` |

- Fetch methods return the fetched value, never `Unit`.
- Do not repeat the model name in methods.
- Name mutations after domain intent: `start()`, `send(answer)`, `complete(id)`.
- Expose `StateFlow` only when synchronous access is part of the domain contract; otherwise
  expose `Flow`.
- Do not give `forceUpdate` a default value.

## Cache and observation

Persistent storage is the cached and observable source. Do not duplicate its snapshot in a
repository-owned mutable collection or flow.

- `forceUpdate = false` returns the local snapshot, including an empty one, without a request.
- `forceUpdate = true` requests the latest value and replaces the cache only after success.
- Return and publish the same mapped value.
- Preserve the last successful cache on failure.
- Observation emits local snapshots and never starts network or storage work.

A repository over state that only the server holds and that has no offline use, such as a running
dialog, has mutations only: no `observe`, no `get`, and no local storage. Each mutation returns
the domain value from the server reply.

## Concurrency and mutations

- Serialize only operations whose interleaving would corrupt local state. Do not add locks to
  plain refreshes.
- Publish complete immutable snapshots.
- Publish mutations after authoritative success, and update the cache before returning.
- Publish an authoritative response instead of fetching it again when it has enough data.
- Clear account-owned caches through a `UserDataCleaner`; repositories do not track the account.

## Verification

Cover initial load, cache reuse, forced refresh, failed-refresh preservation, and mutation
consistency. Follow [Test Structure](../../testing/001-structure.md).
