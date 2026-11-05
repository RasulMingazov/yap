# Admin App

These documents define the implementation rules under `admin/`, the Compose Desktop app for the
owner. All Kotlin in this area also follows [Code conventions](../001-code-conventions.md).

The admin app is a thin client: the server derives every rule and holds the prompts and keys. The
admin app renders what the server returns and sends the owner's edits back.

| Document | Read when changing |
| --- | --- |
| [View models](001-view-models.md) | screen state, events, one-shot output, or loading and errors |
| [UI and Compose](002-ui-compose.md) | what belongs in `UiState` versus in a composable |
