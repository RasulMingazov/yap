package app.yap.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import app.yap.admin.core.design.AdminMaterialTheme
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.ToolbarBack
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.LoadResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.presentation.CharacterUiStateMapper
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharactersUiStateMapper
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.characters.presentation.ui.CharacterContent
import app.yap.admin.feature.characters.presentation.ui.CharactersContent
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.presentation.ServerUiStateMapper
import app.yap.admin.feature.server.presentation.ServerViewModel
import app.yap.admin.feature.server.presentation.ui.ServerContent
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.ui.AdminFrame
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat

internal class ScreenCaptureTest {

    @Test
    fun `GIVEN every state of Characters WHEN capturing THEN each state has an image`() {
        val states = mapOf(
            "loading" to CharactersViewModel.DataState(),
            "empty" to CharactersViewModel.DataState(characters = LoadResult.Loaded(emptyList())),
            "error" to CharactersViewModel.DataState(characters = LoadResult.Failed),
            "content" to CharactersViewModel.DataState(characters = LoadResult.Loaded(CAST)),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "characters-$state", current = Screen.Characters) {
                CharactersContent(
                    onCharacterClick = {},
                    onEvent = {},
                    onNewCharacterClick = {},
                    uiState = CharactersUiStateMapper.map(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN every state of the Character form WHEN capturing THEN each state has an image`() {
        val saved = CharacterViewModel.DataState(characterId = CHARACTER_ID).saved(StubCharacter.stubProfile())
        val states = mapOf(
            "loading" to CharacterViewModel.DataState(characterId = CHARACTER_ID),
            "error" to CharacterViewModel.DataState(characterId = CHARACTER_ID).copy(load = CharacterViewModel.Load.FAILED),
            "new" to CharacterViewModel.DataState(characterId = null),
            "empty-field" to CharacterViewModel.DataState(characterId = null).copy(
                form = StubCharacter.stubProfile(manner = ""),
                saveStatus = CharacterViewModel.SaveStatus.EMPTY_FIELD,
            ),
            "saving" to saved.copy(isSaving = true),
            "saved" to saved.copy(saveStatus = CharacterViewModel.SaveStatus.SAVED),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "character-$state", current = Screen.Characters) {
                CharacterContent(
                    back = ToolbarBack(
                        label = "Персонажи",
                        onClick = {},
                    ),
                    onEvent = {},
                    uiState = CharacterUiStateMapper.map(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN every state of Server WHEN capturing THEN each state has an image`() {
        val filled = ServerViewModel.DataState(
            address = LONG_ADDRESS,
            key = "local-admin-key",
        )
        val states = mapOf(
            "first-start" to ServerViewModel.DataState(),
            "connecting" to filled.copy(isConnecting = true),
            "connected" to filled.copy(connection = ServerConnection.Connected(environment = "staging")),
            "wrong-key" to filled.copy(connection = ServerConnection.Unauthorized),
            "unreachable" to filled.copy(connection = ServerConnection.Unreachable),
        )

        val images = states.map { (state, dataState) ->
            val uiState = ServerUiStateMapper.map(dataState)
            capture(name = "server-$state", current = Screen.Server, serverLabel = uiState.serverLabel) {
                ServerContent(
                    onEvent = {},
                    uiState = uiState,
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    private fun capture(
        name: String,
        current: Screen,
        serverLabel: String = CONNECTED_LABEL,
        content: @Composable () -> Unit,
    ): File {
        val scene = ImageComposeScene(
            width = AdminTheme.Sizes.windowWidth.value.toInt(),
            height = AdminTheme.Sizes.windowHeight.value.toInt(),
            density = Density(1f),
        ) {
            AdminMaterialTheme {
                AdminFrame(
                    current = current,
                    onScreenClick = {},
                    serverLabel = serverLabel,
                    content = content,
                )
            }
        }
        val image = try {
            scene.render()
        } finally {
            scene.close()
        }
        val png = image.encodeToData(EncodedImageFormat.PNG) ?: error("The image of $name did not encode")
        return File(SCREENS, "$name.png").apply {
            parentFile.mkdirs()
            writeBytes(png.bytes)
        }
    }

    private companion object {

        const val LONG_ADDRESS = "https://staging.yap-admin.example.com"
        val CHARACTER_ID = CharacterId(StubCharacter.CHARACTER_ID)
        val CAST = listOf(
            StubCharacter.stubCharacter(),
            StubCharacter.stubCharacter(
                characterId = "7d1e0c2a-4b3f-4e6a-9c8d-2f1a0b3c4d5e",
                profile = StubCharacter.stubProfile(
                    name = "Mia",
                    personality = "Calm and precise. Corrects mistakes gently, explains why, and gives one short " +
                        "example from daily life before she moves on to the next question.",
                ),
            ),
            StubCharacter.stubCharacter(
                characterId = "9b8a7c6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d",
                profile = StubCharacter.stubProfile(
                    name = "Oliver Montgomery-Smith",
                    personality = "Grumpy but kind.",
                ),
            ),
        )
        val CONNECTED_LABEL = ServerUiStateMapper
            .map(ServerViewModel.DataState(connection = ServerConnection.Connected(environment = "staging")))
            .serverLabel
        val SCREENS = File("build/screens")
    }
}
