package app.yap.admin.feature.models.data.api

import app.yap.contract.admin.TaskModelDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PUT
import io.ktor.client.statement.HttpResponse

internal interface ModelApi {

    @GET("admin/models")
    suspend fun list(): HttpResponse

    @Headers("Content-Type: application/json")
    @PUT("admin/models")
    suspend fun save(@Body tasks: List<TaskModelDto>): HttpResponse
}
