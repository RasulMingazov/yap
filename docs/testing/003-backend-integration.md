# Backend Integration Tests

Database invariants are PostgreSQL behavior. An in-memory fake cannot prove that the production
adapter preserves transactions, constraints, concurrency, or cascades.

## When to use PostgreSQL

Use Testcontainers with PostgreSQL when the behavior depends on the production database:

- transaction atomicity;
- concurrent updates;
- uniqueness and foreign-key constraints;
- reuse and idempotency enforced by persisted state;
- cascades and Flyway migrations.

Use faster focused tests for behavior owned elsewhere:

- use-case tests with repository stubs for orchestration and outcomes;
- route tests for HTTP parsing and serialization;
- adapter tests for model provider translation.

Do not reproduce a database algorithm in a fake and treat the result as PostgreSQL verification.

Example scenario:

```text
GIVEN two draft topics for the same date
WHEN both are published concurrently
THEN only one is published for that date
```

Follow [Test Structure](001-structure.md) for naming and scenario shape.

## Corner cases

- Coordinate concurrency with a barrier or latch; timing based on delays is not deterministic.
- Isolate database state between tests. A rollback is insufficient for migrations or behavior
  using multiple connections.
- Verify a clean bootstrap from all current migrations.
- Pin the same PostgreSQL major in deployment and Testcontainers configuration; never use `latest`.
- The first integration test sets up how a missing container runtime is handled:
  - locally, the suite is skipped with an assumption. Report that the integration suite was not
    run. Do not silently substitute a fake or claim database verification;
  - in CI (`CI=true`), the suite fails instead of skipping, so a green build always includes the
    PostgreSQL tests.
