package app.yap.admin.feature.prompts.data.api

import app.yap.contract.admin.PromptTextDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

internal interface PromptApi {

    @GET("admin/prompts/{promptKey}")
    suspend fun find(@Path promptKey: String): HttpResponse

    @GET("admin/prompts")
    suspend fun list(): HttpResponse

    @POST("admin/prompts/{promptKey}/versions/{number}/restore")
    suspend fun restore(
        @Path promptKey: String,
        @Path number: Int,
    ): HttpResponse

    @Headers("Content-Type: application/json")
    @POST("admin/prompts/{promptKey}/versions")
    suspend fun save(
        @Path promptKey: String,
        @Body text: PromptTextDto,
    ): HttpResponse
}
