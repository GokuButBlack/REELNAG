package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@Serializable
data class ThinkingConfig(
    val thinkingLevel: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

@Serializable
data class AiStudioConceptDto(
    val title: String = "",
    val openingHook: String = "",
    val explanation: String = "",
    val sceneOutline: String = "",
    val suggestedShots: String = "",
    val onScreenText: String = "",
    val caption: String = "",
    val hashtags: String = "",
    val callToAction: String = "",
    val editingSuggestions: String = ""
)

data class StudioRequestParams(
    val inputType: String, // Topic, Trend, Own Idea, Saved Reel, Reference URL
    val inputText: String,
    val niche: String,
    val audience: String,
    val videoLength: String,
    val tone: String,
    val goal: String,
    val personalityName: String
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-4-argon:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiStudioService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    suspend fun generateThreeConcepts(params: StudioRequestParams): Result<List<AiStudioConceptDto>> =
        withContext(Dispatchers.IO) {
            if (!isApiKeyConfigured()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Gemini API key is not configured yet. To generate live AI concepts, open the Secrets panel in AI Studio and set GEMINI_API_KEY. Everything else in ReelNag works without an API key!"
                    )
                )
            }

            val prompt = buildString {
                appendLine("Create 3 distinct, high-retention Instagram Reel video concepts for a creator.")
                appendLine("Input Source Type: ${params.inputType}")
                appendLine("Input Inspiration / Seed: ${params.inputText}")
                appendLine("Niche: ${params.niche}")
                appendLine("Target Audience: ${params.audience}")
                appendLine("Target Video Length: ${params.videoLength}")
                appendLine("Desired Tone: ${params.tone}")
                appendLine("Primary Goal: ${params.goal}")
                appendLine("Creator's Companion Personality Style: ${params.personalityName}")
                appendLine()
                appendLine("Requirements:")
                appendLine("- Make each of the 3 concepts genuinely different in format (e.g., Concept 1: POV/Storytime or Fast Hook, Concept 2: Visual Tutorial/Demonstration, Concept 3: Contrarian/Myth-Busting or Relatable Skit).")
                appendLine("- Never claim to scrape live Instagram metrics or copyright-protected audio; focus on original filming, scripting, and editing execution.")
            }

            val schema = buildJsonObject {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("title") { put("type", "STRING") }
                        putJsonObject("openingHook") { put("type", "STRING") }
                        putJsonObject("explanation") { put("type", "STRING") }
                        putJsonObject("sceneOutline") { put("type", "STRING") }
                        putJsonObject("suggestedShots") { put("type", "STRING") }
                        putJsonObject("onScreenText") { put("type", "STRING") }
                        putJsonObject("caption") { put("type", "STRING") }
                        putJsonObject("hashtags") { put("type", "STRING") }
                        putJsonObject("callToAction") { put("type", "STRING") }
                        putJsonObject("editingSuggestions") { put("type", "STRING") }
                    }
                    putJsonArray("required") {
                        add(kotlinx.serialization.json.JsonPrimitive("title"))
                        add(kotlinx.serialization.json.JsonPrimitive("openingHook"))
                        add(kotlinx.serialization.json.JsonPrimitive("explanation"))
                        add(kotlinx.serialization.json.JsonPrimitive("sceneOutline"))
                        add(kotlinx.serialization.json.JsonPrimitive("suggestedShots"))
                        add(kotlinx.serialization.json.JsonPrimitive("onScreenText"))
                        add(kotlinx.serialization.json.JsonPrimitive("caption"))
                        add(kotlinx.serialization.json.JsonPrimitive("hashtags"))
                        add(kotlinx.serialization.json.JsonPrimitive("callToAction"))
                        add(kotlinx.serialization.json.JsonPrimitive("editingSuggestions"))
                    }
                }
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(
                    parts = listOf(
                        Part(
                            text = "You are ReelNag's AI Content Studio director. You craft original, actionable, scene-by-scene short-form video concepts with witty, practical direction."
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = schema,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
                )
            )

            try {
                val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val textPart = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull { !it.text.isNullOrBlank() }
                    ?.text
                    ?: return@withContext Result.failure(Exception("Gemini returned an empty response. Please try again."))

                val concepts = json.decodeFromString<List<AiStudioConceptDto>>(textPart)
                if (concepts.isEmpty()) {
                    Result.failure(Exception("No concepts were generated. Try refining your topic."))
                } else {
                    Result.success(concepts)
                }
            } catch (e: Exception) {
                Result.failure(
                    Exception("AI Studio generation failed (${e.localizedMessage ?: "Network/API error"}). Check your GEMINI_API_KEY in AI Studio Secrets or try again.")
                )
            }
        }
}
