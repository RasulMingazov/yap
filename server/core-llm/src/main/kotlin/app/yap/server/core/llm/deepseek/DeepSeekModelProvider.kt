package app.yap.server.core.llm.deepseek

import app.yap.server.core.llm.ModelProvider
import app.yap.server.core.llm.ModelReply
import app.yap.server.core.llm.ModelRequest
import app.yap.server.core.llm.runSuspendCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DeepSeekModelProvider::class.java)

class DeepSeekModelProvider internal constructor(
    private val config: DeepSeekConfig,
    private val engine: HttpClientEngine,
) : ModelProvider, AutoCloseable {

    constructor(config: DeepSeekConfig) : this(
        config = config,
        engine = CIO.create(),
    )

    private val httpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout.inWholeMilliseconds
        }
    }

    override suspend fun json(request: ModelRequest): ModelReply<JsonObject> = reply(
        body = request.toDto(responseFormat = ResponseFormatDto(type = JSON_OBJECT)),
        read = { content -> Json.parseToJsonElement(content).jsonObject },
    )

    override suspend fun text(request: ModelRequest): ModelReply<String> = reply(
        body = request.toDto(responseFormat = null),
        read = { content -> content },
    )

    override fun close() {
        httpClient.close()
        engine.close()
    }

    private suspend fun <T> reply(body: ChatRequestDto, read: (String) -> T): ModelReply<T> =
        runSuspendCatching { read(complete(body)) }
            .fold(
                onSuccess = { value -> ModelReply.Answered(value) },
                onFailure = { cause ->
                    logger.warn("DeepSeek call failed", cause)
                    ModelReply.Failed
                },
            )

    private suspend fun complete(body: ChatRequestDto): String {
        val choice = httpClient
            .post(COMPLETIONS_URL) {
                bearerAuth(config.apiKey)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            .body<ChatResponseDto>()
            .choices
            .first()
        check(choice.finishReason == FINISH_REASON_STOP) { "DeepSeek stopped with ${choice.finishReason}" }
        return checkNotNull(choice.message.content?.takeIf(String::isNotBlank)) { "DeepSeek answered with no content" }
    }
}

private const val COMPLETIONS_URL = "https://api.deepseek.com/chat/completions"
private const val FINISH_REASON_STOP = "stop"
private const val JSON_OBJECT = "json_object"
