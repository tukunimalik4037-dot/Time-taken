package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class SanitizedDailyStats(
    val date: String,
    val screenTimeMinutes: Int,
    val studyMinutes: Int,
    val socialMinutes: Int,
    val shortVideoMinutes: Int,
    val otherMinutes: Int,
    val completedTasks: Int,
    val totalTasks: Int
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateDailySummary(stats: SanitizedDailyStats): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are TimeTrack AI, a thoughtful productivity analytics engine.
            Analyze the following sanitized daily statistics and provide a concise, encouraging 2-3 sentence natural-language summary of the user's day.
            Highlight timetable completion and balance between study time and short-video or screen time.
            CRITICAL RULES:
            - Do not make medical, psychological, or clinical diagnoses (e.g. do not say 'addiction', 'ADHD', 'unhealthy').
            - Use constructive language such as 'Your timetable completion reached...', 'Your study sessions were strongest...', 'Short-video activity increased...'.
            
            Stats:
            - Date: ${stats.date}
            - Total Screen Time: ${stats.screenTimeMinutes / 60}h ${stats.screenTimeMinutes % 60}m
            - Study / Focus Time: ${stats.studyMinutes / 60}h ${stats.studyMinutes % 60}m
            - Social & Video Time: ${stats.socialMinutes / 60}h ${stats.socialMinutes % 60}m
            - Estimated Short-Video Time: ${stats.shortVideoMinutes}m
            - Timetable Activities: ${stats.completedTasks}/${stats.totalTasks} completed
        """.trimIndent()

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
                val response = service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            } catch (e: Exception) {
                // Fallback to local intelligent synthesis
            }
        }

        // Local fallback synthesis engine (Offline & Privacy Guarantee)
        val completionRate = if (stats.totalTasks > 0) (stats.completedTasks * 100 / stats.totalTasks) else 100
        val studyHours = stats.studyMinutes / 60
        val studyMins = stats.studyMinutes % 60
        val screenHours = stats.screenTimeMinutes / 60
        val screenMins = stats.screenTimeMinutes % 60

        buildString {
            if (completionRate >= 75) {
                append("Great consistency today! You accomplished $completionRate% of your scheduled timetable (${stats.completedTasks}/${stats.totalTasks} activities). ")
            } else {
                append("You completed ${stats.completedTasks} out of ${stats.totalTasks} scheduled timetable activities today. ")
            }
            if (stats.studyMinutes > 0) {
                append("Your focused study period accounted for ${studyHours}h ${studyMins}m. ")
            }
            if (stats.shortVideoMinutes > 30) {
                append("Short-video interactions accounted for an estimated ${stats.shortVideoMinutes} minutes of your ${screenHours}h ${screenMins}m total screen time.")
            } else {
                append("Your daily digital balance was well distributed across your scheduled sessions.")
            }
        }
    }
}
