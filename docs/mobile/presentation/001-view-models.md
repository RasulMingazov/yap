# View Models

`<Screen>ViewModel` lives in Kotlin and is the boundary between the shared logic and SwiftUI. It
owns screen state, operations, and one-shot output.

```kotlin
class TopicOfDayViewModel internal constructor(
    private val getTopicOfDayUseCase: GetTopicOfDayUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(TopicOfDayUiStateMapper::map)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    fun onEvent(event: Event) = when (event) {
        is Event.RetryClicked -> onRetryClicked()
        is Event.StartClicked -> onStartClicked()
    }

    sealed interface News {

        data class OpenDialog(val topicId: String) : News

        data class ShowError(val message: String) : News
    }

    sealed interface Event {

        data object RetryClicked : Event

        data object StartClicked : Event
    }
}
```

## Rules

- The view model is `public` for Swift; its constructor is `internal`, and Koin builds it.
- Nest `UiState`, `News`, `Event`, and their payloads in the owning view model. Name events after
  intent: `StartClicked`, `ErrorDismissed`.
- Extend `ViewModel` from `org.jetbrains.androidx.lifecycle` and launch work in `viewModelScope`.
  The SwiftUI screen owns the view model and clears it when the screen goes away.
- Keep internal facts in an immutable `DataState` and map it to `UiState` with one mapper object.
- Put every text, order, and visibility in `UiState`. Copy is Russian; count-dependent copy picks
  its one/few/many form in the mapper.
- `News` is one-shot output. Navigation that follows an outcome, such as opening the dialog after
  «Начать», is a `News` that the Swift screen turns into a route.
- Depend on use cases only; never inject a repository or a data source.
- Set busy flags before launching guarded work and clear stale errors on retry.
- Do not switch dispatchers. `viewModelScope` is enough.
- Release owned resources in `onCleared()`.

Start with one view model per screen. Extract a second one only when a section has an independent
lifecycle or a reusable interaction.

## Verification

Mapper and view-model tests are the main verification: assert the complete `UiState` and the exact
`News`. Follow [Test Structure](../../testing/001-structure.md).
