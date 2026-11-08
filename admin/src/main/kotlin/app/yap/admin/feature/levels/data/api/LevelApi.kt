package app.yap.admin.feature.levels.data.api

import app.yap.contract.admin.LevelInputDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PUT
import io.ktor.client.statement.HttpResponse

internal interface LevelApi {

    @GET("admin/levels")
    suspend fun list(): HttpResponse

    @Headers("Content-Type: application/json")
    @PUT("admin/levels")
    suspend fun save(@Body entries: List<LevelInputDto>): HttpResponse
}
