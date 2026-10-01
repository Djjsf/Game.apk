package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ScorePayload(
    @Json(name = "name") val playerName: String,
    @Json(name = "data") val scoreData: ScoreData
)

@JsonClass(generateAdapter = true)
data class ScoreData(
    @Json(name = "score") val score: Int,
    @Json(name = "burgers") val burgers: Int,
    @Json(name = "bounces") val bounces: Int,
    @Json(name = "timestamp") val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class RemoteScoreItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "data") val data: ScoreData? = null
)

interface LeaderboardApiService {

    @GET("objects")
    suspend fun getGlobalScores(): List<RemoteScoreItem>

    @POST("objects")
    suspend fun submitGlobalScore(@Body payload: ScorePayload): RemoteScoreItem

    companion object {
        private const val BASE_URL = "https://api.restful-api.dev/"

        fun create(): LeaderboardApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(LeaderboardApiService::class.java)
        }
    }
}
