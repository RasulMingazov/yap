# UI and Compose

These rules cover what this project decides: what belongs in state versus in a composable.

```text
Model → ViewModel.DataState → UiStateMapper → StateFlow<ViewModel.UiState> → Compose
Model/view-model outcome → ViewModel.News → Compose one-shot effect
```

## UI state

- Put every repeatable value required to render the screen in `UiState`: texts, item order,
  visibility, availability, enabled and loading state, stable keys, and persistent inline errors.
- A mapper derives those from models and injected configuration. It never touches theme values,
  dimensions, focus, scrolling, or animation.
- Never add a consumable snackbar or dialog flag to `UiState`.
- Copy is Russian. Count-dependent copy picks its one/few/many form by the Russian rule in the mapper.

## News

- `News` is one-shot output: snackbars and dialogs.
- A persistent inline error or banner that must survive resubscription stays in `UiState`.
- Resolve copy before emitting; a composable must not map an error to snackbar text.
- Explicit cancellation emits no news. A recoverable failure emits exactly one `ShowSnackbar`.
- Do not mirror `News` into `UiState` or add an `ErrorConsumed` event to prevent replay.

## Compose

Collect `UiState` with `collectAsStateWithLifecycle()` and `News` once at the screen boundary. Pass
focused state and callbacks down; a child receives state and lambdas, never the view model.

- Do not fetch, call use cases, interpret outcomes, or launch work during composition.
- Do not choose copy, visibility, ordering, or availability with product-specific `when` branches.
  Render `UiState` and emit intent.
- Keep only transient visual state, such as focus, scrolling, and text field input, in Compose.

## Look

The admin app has its own neutral look from the design canvas page «Админка», not the Yap design
system of the mobile app.

- Keep colors, type, spacing, and radii in one admin theme object. A screen reads them from it.
- Do not write a literal color or size in a screen. A new value goes into the theme object first.

## Verification

Mapper tests are the primary verification surface: assert the complete observable `UiState` and,
separately, the exact `News` subtype and payload. Use Compose UI tests only for rendering and
semantics a mapper test cannot prove.
