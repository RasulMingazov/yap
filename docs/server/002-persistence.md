# Persistence

Each feature owns its persisted data, and shared database infrastructure stays feature-independent.

- A feature owns the repositories for its persisted aggregates, its tables, and its Flyway
  migrations.
- `core-database` owns the connection, transaction bootstrap, and migration execution only.
- A repository is an interface with one Exposed implementation, so use-case tests can stub it. Add
  another source layer only for a real second boundary.
- Keep an invariant inside the transaction that owns its atomic change.
- Keep Flyway migrations under the feature's `src/main/resources/db/migration`.
- All feature migrations share one global Flyway version sequence: a new migration takes the next
  free `V<n>` across every feature.
- Until the app has real users, edit the existing migration in place. With
  `DATABASE_RESET_ON_MIGRATION_CHANGE=true`, the server clears the database and migrates from
  scratch when an applied migration changed, so a deploy needs no manual step. Before launch, turn
  the flag off and make migrations forward-only.
- Translate persistence-specific failures at the persistence boundary.

PostgreSQL-specific behavior follows [Backend Integration](../testing/003-backend-integration.md).
