# View Models

`<Screen>ViewModel` is the boundary between the admin logic and Compose. It owns screen state,
operations, and one-shot output.

```kotlin
internal class CharacterViewModel(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val saveCharacterUseCase: SaveCharacterUseCase,
) : ViewModel() {

    private val dataState = MutableStateFlow(DataState())
    val uiState: StateFlow<UiState> = dataState.mapState(viewModelScope, CharacterUiStateMapper)

    private val newsChannel = Channel<News>(Channel.BUFFERED)
    val news: Flow<News> = newsChannel.receiveAsFlow()

    fun onEvent(event: Event) = when (event) {
        is Event.RetryClicked -> onRetryClicked()
        is Event.SaveClicked -> onSaveClicked()
    }

    override fun onCleared() {
        super.onCleared()
        newsChannel.close()
    }

    data class DataState(val character: Character? = null, val isLoading: Boolean = false)

    data class UiState(val error: Error?, val isLoading: Boolean) {

        data class Error(val message: String)
    }

    sealed interface News {

        data class ShowSnackbar(val message: String) : News
    }

    sealed interface Event {

        data object RetryClicked : Event

        data object SaveClicked : Event
    }
}
```

## Rules

- Nest `UiState`, `News`, `Event`, and their payloads in the owning view model; never create
  top-level `CharacterUiState` types. Name events after intent: `SaveClicked`, `ErrorDismissed`.
- Extend `ViewModel` from `org.jetbrains.androidx.lifecycle`, keep it `internal`, and launch work
  in `viewModelScope`.
- Keep internal facts in an immutable `DataState` and map it to `UiState` with one mapper object,
  through `mapState`: a `StateFlow` mapping helper that the first view model adds.
- Keep only raw facts in `DataState`: domain data that is `null` until it loads, busy flags
  (`isLoading`, `isSaving`), failure flags (`isLoadFailed`), the domain result of the last call
  when its kind matters, and the user's input. Do not keep a type that names a screen state, such
  as a `LOADING`/`LOADED`/`FAILED` enum, a `LoadResult`, or a `Saving` status. Do not add derived
  getters.
- Let the mapper decide the screen state and every status text from these facts.
- A `UiState` mapper object implements `(DataState) -> UiState`. Its only public function is
  `override fun invoke(state: DataState): UiState`, and the view model passes the object to
  `mapState`. Every other function of the mapper is `private`. Do not split a mapper into a second
  public object to pass Detekt: `TooManyFunctions` does not count private functions.
- In a `UiState` mapper, build each non-trivial field with its own named function; keep
  constructor calls free of inline `when`, nested `let`, and `?:` chains. Choose between texts with
  an exhaustive `when`, one branch per outcome.
- Set busy flags before launching guarded work and clear stale errors on retry without erasing
  unrelated state.
- Depend on use cases only; never inject a repository or the HTTP client.
- `News` carries snackbars and dialogs, never a screen switch.
- Do not switch dispatchers. `viewModelScope` is enough.
- Route non-trivial events to named `on...` functions and handle typed outcomes exhaustively.
- Release owned resources in `onCleared()`.

A view model owns loading, retry, screen errors, and saving. Compose owns colors, dimensions,
focus, scrolling, and animation.

Start with one view model per screen. Extract a second one only when a section has an independent
lifecycle or a reusable interaction, such as the Try panel.

## Verification

Test event-to-state and event-to-news behavior, typed outcomes, duplicate-action guards,
cancellation, and cleanup. Follow [Test Structure](../testing/001-structure.md).
