package app.yap.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.yap.admin.core.design.AdminMaterialTheme
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.ToolbarBack
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.presentation.CharacterUiStateMapper
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharactersUiStateMapper
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.characters.presentation.ui.CharacterContent
import app.yap.admin.feature.characters.presentation.ui.CharactersContent
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.presentation.LevelsUiStateMapper
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import app.yap.admin.feature.levels.presentation.toInputs
import app.yap.admin.feature.levels.presentation.ui.LevelsContent
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptVersion
import app.yap.admin.feature.prompts.domain.StubPrompt
import app.yap.admin.feature.prompts.presentation.PromptsUiStateMapper
import app.yap.admin.feature.prompts.presentation.PromptsViewModel
import app.yap.admin.feature.prompts.presentation.ui.PromptsContent
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.presentation.ServerUiStateMapper
import app.yap.admin.feature.server.presentation.ServerViewModel
import app.yap.admin.feature.server.presentation.ui.ServerContent
import app.yap.admin.feature.topics.domain.Interlocutor
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
import java.time.Duration
import java.time.Instant
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
                    uiState = TopicsUiStateMapper(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN every state of the Topic form WHEN capturing THEN each state has an image`() {
        val new = TopicViewModel.DataState(load = TopicViewModel.Load.LOADED)
        val opened = TopicViewModel.DataState(cast = CAST).saved(SAVED_TOPIC)
        val states = mapOf(
            "loading" to TopicViewModel.DataState(),
            "error" to TopicViewModel.DataState(load = TopicViewModel.Load.FAILED),
            "new" to new,
            "new-cast" to new.copy(cast = CAST),
            "opened" to opened,
            "guest" to TopicViewModel.DataState(cast = CAST).saved(GUEST_TOPIC),
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
            capture(name = "topic-$state", current = Screen.Topics, height = TOPIC_FORM_HEIGHT) {
                TopicContent(
                    back = TOPICS_BACK,
                    onEvent = {},
                    uiState = TopicUiStateMapper(dataState),
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
                uiState = TopicUiStateMapper(TopicViewModel.DataState().saved(SAVED_TOPIC)),
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
            "empty" to CharactersViewModel.DataState(characters = emptyList()),
            "error" to CharactersViewModel.DataState(isLoadFailed = true),
            "content" to CharactersViewModel.DataState(characters = CAST),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "characters-$state", current = Screen.Characters) {
                CharactersContent(
                    onCharacterClick = {},
                    onEvent = {},
                    onNewCharacterClick = {},
                    uiState = CharactersUiStateMapper(dataState),
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
            "error" to CharacterViewModel.DataState(characterId = CHARACTER_ID, isLoadFailed = true),
            "new" to CharacterViewModel.DataState(characterId = null),
            "opened" to saved,
            "empty-field" to CharacterViewModel.DataState(characterId = null).copy(
                form = StubCharacter.stubProfile(manner = ""),
                saveResult = SaveCharacterResult.Invalid,
            ),
            "saving" to saved.copy(isSaving = true),
            "saved" to saved.copy(saveResult = SaveCharacterResult.Saved(StubCharacter.stubCharacter())),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "character-$state", current = Screen.Characters) {
                CharacterContent(
                    back = ToolbarBack(
                        label = "Персонажи",
                        onClick = {},
                    ),
                    onEvent = {},
                    uiState = CharacterUiStateMapper(dataState),
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
            val uiState = ServerUiStateMapper(dataState)
            capture(name = "server-$state", current = Screen.Server, serverLabel = uiState.serverLabel) {
                ServerContent(
                    onEvent = {},
                    uiState = uiState,
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN every state of Levels WHEN capturing THEN each state has an image`() {
        val new = levelsDataState(StubLevel.stubLevels(range = null, settings = null))
        val saved = levelsDataState(LEVELS)
        val states = mapOf(
            "loading" to LevelsViewModel.DataState(),
            "error" to LevelsViewModel.DataState(isLoadFailed = true),
            "new" to new,
            "saved" to saved.copy(saveStatus = LevelsViewModel.SaveStatus.Saved),
            "chosen-b2" to saved.copy(chosen = LevelCode.B2),
            "saving" to saved.copy(isSaving = true),
            "invalid" to new.copy(saveStatus = LevelsViewModel.SaveStatus.Invalid),
            "no-idioms" to new.copy(
                forms = LevelCode.entries.associateWith { StubLevel.stubInput(idioms = null) },
                saveStatus = LevelsViewModel.SaveStatus.NoIdioms(LevelCode.A1),
            ),
            "unread-rating-from" to saved.unread(
                code = LevelCode.B2,
                field = LevelsViewModel.NumberField.RATING_FROM,
            ) { form -> form.copy(ratingFrom = "12OO") },
            "unread-max-words" to saved.unread(
                code = LevelCode.C1,
                field = LevelsViewModel.NumberField.MAX_WORDS,
            ) { form -> form.copy(maxWords = "twenty") },
        )

        val images = states.map { (state, dataState) ->
            capture(name = "levels-$state", current = Screen.Levels, height = LEVELS_HEIGHT) {
                LevelsContent(
                    onEvent = {},
                    uiState = LevelsUiStateMapper(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    @Test
    fun `GIVEN the idioms select WHEN clicking it THEN the open select has an image`() {
        val image = capture(name = "levels-idioms-open", current = Screen.Levels, click = IDIOMS_SELECT) {
            LevelsContent(
                onEvent = {},
                uiState = LevelsUiStateMapper(levelsDataState(LEVELS)),
            )
        }

        assertTrue(image.length() > 0)
    }

    @Test
    fun `GIVEN every state of Prompts WHEN capturing THEN each state has an image`() {
        val listed = PromptsViewModel.DataState(
            load = PromptsViewModel.Load.LOADED,
            summaries = StubPrompt.stubPromptSummaries(),
        )
        val content = listed.loaded(REPLY_PROMPT)
        val states = mapOf(
            "loading" to PromptsViewModel.DataState(),
            "error" to PromptsViewModel.DataState(load = PromptsViewModel.Load.FAILED),
            "content" to content,
            "long-text" to content.copy(
                texts = mapOf(PromptKey.TOPIC_REPLY to "${REPLY_PROMPT.text}\n\n${REPLY_PROMPT.text}"),
            ),
            "no-version" to listed
                .copy(chosen = PromptKey.TOPIC_HINTS)
                .loaded(NEW_PROMPT),
            "prompt-loading" to content.copy(
                chosen = PromptKey.TOPIC_HINTS,
                promptLoad = PromptsViewModel.Load.LOADING,
            ),
            "prompt-error" to content.copy(
                chosen = PromptKey.TOPIC_HINTS,
                promptLoad = PromptsViewModel.Load.FAILED,
            ),
            "saving" to content.copy(sendStatus = PromptsViewModel.SendStatus.Saving),
            "restoring" to content.copy(sendStatus = PromptsViewModel.SendStatus.Restoring),
            "saved" to content.copy(sendStatus = PromptsViewModel.SendStatus.Saved),
            "empty-text" to content.copy(
                sendStatus = PromptsViewModel.SendStatus.EmptyText,
                texts = mapOf(PromptKey.TOPIC_REPLY to ""),
            ),
            "restored" to listed.sent(
                prompt = REPLY_PROMPT.copy(versions = replyVersions(current = RESTORED_NUMBER)),
                sendStatus = PromptsViewModel.SendStatus.Restored(RESTORED_NUMBER),
            ),
        )

        val images = states.map { (state, dataState) ->
            capture(name = "prompts-$state", current = Screen.Prompts, height = PROMPTS_HEIGHT) {
                PromptsContent(
                    onEvent = {},
                    uiState = PromptsUiStateMapper(dataState),
                )
            }
        }

        assertTrue(images.all { image -> image.length() > 0 })
    }

    private fun capture(
        name: String,
        current: Screen,
        click: Offset? = null,
        height: Dp = AdminTheme.Sizes.windowHeight,
        serverLabel: String = CONNECTED_LABEL,
        content: @Composable () -> Unit,
    ): File {
        val scene = ImageComposeScene(
            width = AdminTheme.Sizes.windowWidth.value.toInt(),
            height = height.value.toInt(),
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
            click?.let { position ->
                scene.sendPointerEvent(PointerEventType.Press, position)
                scene.sendPointerEvent(PointerEventType.Release, position)
            }
            (1..SETTLED_FRAMES).map { frame -> scene.render(frame * SETTLED_NANOS) }.last()
        } finally {
            scene.close()
        }
        val png = image.encodeToData(EncodedImageFormat.PNG) ?: error("The image of $name did not encode")
        return File(SCREENS, "$name.png").apply {
            parentFile.mkdirs()
            writeBytes(png.bytes)
        }
    }

    private fun levelsDataState(levels: List<Level>): LevelsViewModel.DataState = LevelsViewModel.DataState(
        forms = levels.toInputs(),
        levels = levels,
    )

    private fun LevelsViewModel.DataState.unread(
        code: LevelCode,
        field: LevelsViewModel.NumberField,
        change: (LevelInput) -> LevelInput,
    ): LevelsViewModel.DataState = copy(
        forms = forms + (code to change(forms.getValue(code))),
        saveStatus = LevelsViewModel.SaveStatus.UnreadNumber(
            code = code,
            field = field,
        ),
    )

    private companion object {

        const val LONG_ADDRESS = "https://staging.yap-admin.example.com"
        const val REPLY_VERSIONS = 7
        const val RESTORED_NUMBER = 5
        const val SETTLED_FRAMES = 5
        const val SETTLED_NANOS = 1_000_000_000L
        const val VERSION_GAP_DAYS = 3L
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
        val CONNECTED_LABEL = ServerUiStateMapper(
            ServerViewModel.DataState(connection = ServerConnection.Connected(environment = "staging")),
        ).serverLabel
        val GUEST_TOPIC = StubTopic.stubTopic(
            content = StubTopic.stubContent(
                interlocutor = Interlocutor.Guest(
                    description = "An older taxi driver. He speaks slowly, with a light accent. Polite, but he " +
                        "does not chat.",
                ),
                role = "Taxi driver",
                steps = listOf(
                    "Name the Hilton on Park Street" to "The driver knows where to go.",
                    "Ask how long the ride takes" to "The ride takes about 20 minutes.",
                    "Ask the price" to "The ride costs about 30 dollars.",
                    "Ask the driver to turn the music down; it does not have to be polite" to "The music is quieter.",
                    "Pay and say goodbye" to "You are at the hotel.",
                ).map { (task, outcome) ->
                    StubTopic.stubStep(
                        outcome = outcome,
                        passWhen = "The learner does the task in English, in any words.",
                        task = task,
                    )
                },
            ),
        )
        val IDIOMS_SELECT = Offset(731f, 328f)
        val LEVELS = listOf(
            savedLevel(
                code = LevelCode.A1,
                from = 0,
                idioms = Idioms.NEVER,
                maxWords = 8,
                to = 849,
            ),
            savedLevel(
                code = LevelCode.A2,
                from = 850,
                idioms = Idioms.NEVER,
                maxWords = 12,
                to = 1049,
            ),
            savedLevel(
                code = LevelCode.B1,
                from = 1050,
                idioms = Idioms.RARELY,
                maxWords = 16,
                to = 1199,
            ),
            savedLevel(
                code = LevelCode.B2,
                from = 1200,
                idioms = Idioms.SOMETIMES,
                maxWords = 20,
                to = 1399,
            ),
            savedLevel(
                code = LevelCode.C1,
                from = 1400,
                idioms = Idioms.FREELY,
                maxWords = 25,
                to = 1599,
            ),
            savedLevel(
                code = LevelCode.C2,
                from = 1600,
                idioms = Idioms.FREELY,
                maxWords = 30,
                to = null,
            ),
            savedLevel(
                code = LevelCode.NO_RATING,
                from = null,
                idioms = Idioms.RARELY,
                maxWords = 14,
                to = null,
            ),
        )
        val LEVELS_HEIGHT = 1000.dp
        val NEW_PROMPT = StubPrompt.stubPrompt(
            promptKey = PromptKey.TOPIC_HINTS,
            text = null,
            variables = listOf("{step.task}", "{topic.setting}", "{level.rules}", "{hint.level}"),
            versions = emptyList(),
        )
        val PROMPTS_HEIGHT = 1400.dp
        val REPLY_PROMPT = StubPrompt.stubPrompt(
            text = listOf(
                "You are {interlocutor}.",
                "",
                "Scene: {topic.setting}",
                "Your role: {topic.role}",
                "",
                "{level.rules}",
                "",
                "The learner is on step {step.n}: \"{step.task}\". The check said: {check.verdict}.",
                "- If the step passed, react and move towards the next step.",
                "- If not, do not react as if it is done.",
                "",
                "Never switch to Russian, correct mistakes or leave the role.",
                "Return JSON: {\"line\": \"...\", \"translation\": \"...\"}",
            ).joinToString("\n"),
            variables = listOf(
                "{interlocutor}",
                "{topic.setting}",
                "{topic.role}",
                "{level.rules}",
                "{step.n}",
                "{step.task}",
                "{check.verdict}",
            ),
            versions = replyVersions(current = REPLY_VERSIONS),
        )
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
        val TOPIC_FORM_HEIGHT = 1800.dp

        fun replyVersions(current: Int): List<PromptVersion> = (REPLY_VERSIONS downTo 1).map { number ->
            StubPrompt.stubPromptVersion(
                isCurrent = number == current,
                number = number,
                savedAt = Instant
                    .parse(StubPrompt.SAVED_AT)
                    .minus(Duration.ofDays((REPLY_VERSIONS - number) * VERSION_GAP_DAYS)),
            )
        }

        fun savedLevel(
            code: LevelCode,
            from: Int?,
            idioms: Idioms,
            maxWords: Int,
            to: Int?,
        ): Level {
            val vocabulary = if (code == LevelCode.NO_RATING) "A2–B1, слова темы" else code.name
            return StubLevel.stubLevel(
                code = code,
                range = from?.let { start -> StubLevel.stubRange(from = start, to = to) },
                settings = StubLevel.stubSettings(
                    block = "Speak in short sentences of up to $maxWords words.\n" +
                        "Use $vocabulary words. Explain a rare word in a few simple words.\n" +
                        "Idioms: ${idioms.name.lowercase()}.\n" +
                        "Ask one question at a time and wait for the answer.",
                    idioms = idioms,
                    maxWords = maxWords,
                    ratingFrom = from,
                    vocabulary = vocabulary,
                ),
            )
        }
    }
}
