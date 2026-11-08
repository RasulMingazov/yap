package app.yap.admin.feature.characters.data.api

import app.yap.contract.admin.CharacterProfileDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

internal interface CharacterApi {

    @Headers("Content-Type: application/json")
    @POST("admin/characters")
    suspend fun create(@Body profile: CharacterProfileDto): HttpResponse

    @GET("admin/characters/{characterId}")
    suspend fun find(@Path characterId: String): HttpResponse

    @GET("admin/characters")
    suspend fun list(): HttpResponse

    @Headers("Content-Type: application/json")
    @PUT("admin/characters/{characterId}")
    suspend fun update(
        @Path characterId: String,
        @Body profile: CharacterProfileDto,
    ): HttpResponse
}
