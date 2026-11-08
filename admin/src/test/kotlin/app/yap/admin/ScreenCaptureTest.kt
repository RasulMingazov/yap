package app.yap.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.design.AdminMaterialTheme
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.ToolbarBack
import app.yap.admin.feature.characters.domain.CharacterId
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
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.presentation.TopicUiStateMapper
import app.yap.admin.feature.topics.presentation.TopicViewModel
import app.yap.admin.feature.topics.presentation.TopicsUiStateMapper
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import app.yap.admin.feature.topics.presentation.ui.ReleaseDatePicker
import app.yap.admin.feature.topics.presentation.ui.TopicContent
import app.yap.admin.feature.topics.presentation.ui.TopicsContent
import app.yap.admin.navigation.NavigationViewModel.Screen
import app.yap.admin.navigation.ui.AdminFrame
import java.io.File
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat

internal class ScreenCaptureTest {

    @Test
    fun `GIVEN every state of Topics WHEN capturing THEN each state has an image`() {
        val states = mapOf(
            "loading" to TopicsViewModel.DataState(),
            "empty" to TopicsViewModel.DataState(topics = LoadTopicsResult.Loaded(emptyList())),
            "error" to TopicsViewModel.DataState(topics = LoadTopicsResult.Failed),
            "content" to TopicsViewModel.DataState(topics = LoadTopicsResult.Loaded(TOPICS)),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "topics-$state", current = Screen.Topics) {
                TopicsContent(
                    onEvent = {},
                    onNewTopicClick = {},
                    onTopicClick = {},
                    uiState = TopicsUiStateMapper.map(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN every state of the Topic form WHEN capturing THEN each state has an image`() {
        val new = TopicViewModel.DataState(load = TopicViewModel.Load.LOADED)
        val opened = TopicViewModel.DataState().saved(SAVED_TOPIC)
        val states = mapOf(
            "loading" to TopicViewModel.DataState(),
            "error" to TopicViewModel.DataState(load = TopicViewModel.Load.FAILED),
            "new" to new,
            "opened" to opened,
            "rejected" to new.copy(
                form = StubTopic.stubContent(
                    durationMinutes = 0,
                    releaseDate = null,
                ),
                saveStatus = TopicViewModel.SaveStatus.REJECTED,
            ),
            "saving" to opened.copy(isSaving = true),
            "saved" to opened.copy(saveStatus = TopicViewModel.SaveStatus.SAVED),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "topic-$state", current = Screen.Topics) {
                TopicContent(
                    back = TOPICS_BACK,
                    onEvent = {},
                    uiState = TopicUiStateMapper.map(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN the release date picker WHEN capturing THEN it has an image`() {
        val image = capture(name = "topic-date-picker", current = Screen.Topics) {
            TopicContent(
                back = TOPICS_BACK,
                onEvent = {},
                uiState = TopicUiStateMapper.map(TopicViewModel.DataState().saved(SAVED_TOPIC)),
            )
            ReleaseDatePicker(
                date = StubTopic.RELEASE_DATE,
                onDatePick = {},
                onDismiss = {},
            )
        }

        assertTrue(image.length() > 0)
    }

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
            "opened" to saved,
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
            "filled" to filled,
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
            scene.render(SETTLED_NANOS)
            scene.render(2 * SETTLED_NANOS)
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
        const val SETTLED_NANOS = 1_000_000_000L
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
        val SAVED_TOPIC = StubTopic.stubTopic(
            content = StubTopic.stubContent(
                setting = "Evening at the airport. The learner gets into a taxi to the Hilton on Park Street. The " +
                    "driver plays loud music and likes to talk about traffic. The ride takes about 20 minutes " +
                    "and costs about 30 dollars.",
                title = "A taxi from the airport to the hotel on a rainy evening, with a driver who talks a lot",
            ),
        )
        val SCREENS = File("build/screens")
        val TOPICS = listOf(
            StubTopic.stubTopic(),
            StubTopic.stubTopic(
                content = StubTopic.stubContent(
                    releaseDate = LocalDate.of(2025, 10, 25),
                    title = "A job interview for a junior designer role at a small studio that makes posters, " +
                        "books and signs for museums, theatres, concert halls and the city libraries of the north",
                ),
                topicId = "5a4b3c2d-1e0f-4a9b-8c7d-6e5f4a3b2c1d",
            ),
            StubTopic.stubTopic(
                content = StubTopic.stubContent(
                    releaseDate = LocalDate.of(2025, 10, 26),
                    title = "Small talk with a neighbour",
                ),
                topicId = "3c2b1a0f-9e8d-4c7b-a6f5-4e3d2c1b0a9f",
            ),
        )
        val TOPICS_BACK = ToolbarBack(
            label = "Темы",
            onClick = {},
        )
    }
}
