# Wire Contracts

`contract-admin` holds the wire contracts of the admin API. The server and the admin app share them.

## Naming

- Name every serialized wire type with the `Dto` suffix.
- Name each DTO by its payload meaning.

```text
CharacterDto
TopicDraftDto
PromptVersionDto
```

## Boundary

- DTOs contain wire data only. They are not domain entities, database rows, or use-case results.
- The server maps DTOs to feature models in its routes.
- The admin app maps DTOs to its own models at its data boundary.
