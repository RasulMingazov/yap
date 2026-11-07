package app.yap.admin.navigation

import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.StubServerSettings
import app.yap.admin.feature.server.domain.usecase.StubGetServerSettingsUseCase
import app.yap.admin.navigation.NavigationViewModel.Event
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.NavigationViewModel.UiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

internal class NavigationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN no saved server settings WHEN the app starts THEN it opens Server`() = runTest(dispatcher) {
        val env = Environment(settings = null)

        advanceUntilIdle()

        assertEquals(expected = Screen.Server, actual = env.screen())
    }

    @Test
    fun `GIVEN saved server settings WHEN the app starts THEN it opens Topics`() = runTest(dispatcher) {
        val env = Environment(settings = StubServerSettings.stubServerSettings())

        advanceUntilIdle()

        assertEquals(expected = Screen.Topics, actual = env.screen())
    }

    @Test
    fun `GIVEN Topics is open WHEN Server is clicked THEN Server opens`() = runTest(dispatcher) {
        val env = Environment(settings = StubServerSettings.stubServerSettings())
        advanceUntilIdle()

        env.viewModel.onEvent(Event.ScreenClicked(Screen.Server))
        advanceUntilIdle()

        assertEquals(expected = Screen.Server, actual = env.screen())
    }

    private class Environment(
        settings: ServerSettings?,
    ) {

        val viewModel = NavigationViewModel(
            getServerSettingsUseCase = StubGetServerSettingsUseCase(settings = settings),
        )

        fun screen(): Screen? = (viewModel.uiState.value as? UiState.Content)?.screen
    }
}
