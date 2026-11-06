# SwiftUI

SwiftUI renders the Kotlin `UiState` and sends `Event` back. Everything that a test can check
stays in Kotlin.

```text
Kotlin ViewModel.UiState → SwiftUI view
SwiftUI tap → ViewModel.onEvent(Event)
Kotlin ViewModel.News → SwiftUI one-shot effect or route
```

## Screens

- One Kotlin view model per screen. The screen observes `uiState` and `news` through SKIE as
  `AsyncSequence` and renders the current `UiState`.
- A view receives state and closures, never the view model, below the screen level.
- Do not decide copy, visibility, ordering, or availability in Swift. Render `UiState` and send
  intent.
- Keep only transient visual state in Swift: focus, scrolling, text field input, and animation.
- Switch on a Kotlin sealed type with an exhaustive `switch`; never with `default`.

## Navigation

- Swift owns navigation with `NavigationStack` and sheets.
- A tap that only opens another screen navigates in Swift.
- A screen change that follows an outcome comes as `News` from the view model, and the screen
  turns it into a route.

## Look

The design system «Yap» owns every visual value. The iOS app keeps them as Swift tokens.

- Read colors, type, spacing, radii, and motion from the tokens.
- Do not write a literal color, size, or duration in a screen. A new value goes into the design
  system first, then into the tokens.
- Screens follow the design canvas «Yap App Design», page «Figma · приложение».

## Verification

Kotlin tests cover the behavior. Swift UI tests or previews cover only rendering and accessibility.
