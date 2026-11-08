package app.yap.admin.feature.topics.data.api

import app.yap.contract.admin.TopicContentDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

internal interface TopicApi {

    @GET("admin/topics/calendar")
    suspend fun calendar(): HttpResponse

    @Headers("Content-Type: application/json")
    @POST("admin/topics")
    suspend fun create(@Body content: TopicContentDto): HttpResponse

    @GET("admin/topics/{topicId}")
    suspend fun find(@Path topicId: String): HttpResponse

    @Headers("Content-Type: application/json")
    @POST("admin/topics/{topicId}/publish")
    suspend fun publish(
        @Path topicId: String,
        @Body content: TopicContentDto,
    ): HttpResponse

    @Headers("Content-Type: application/json")
    @PUT("admin/topics/{topicId}")
    suspend fun update(
        @Path topicId: String,
        @Body content: TopicContentDto,
    ): HttpResponse
}
