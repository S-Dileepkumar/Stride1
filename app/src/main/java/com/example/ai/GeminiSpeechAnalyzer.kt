package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SpeechAnalysisResult(
    val encouragingNote: String,
    val clarityScore: Int,
    val clarityFeedback: String,
    val fillerWordCount: Int,
    val fillerWordsList: List<String>,
    val vocabularyVariety: String,
    val vocabularySuggestions: String,
    val toneConfidence: String,
    val actionableTip: String
)

class GeminiSpeechAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeTranscript(topic: String, transcript: String): SpeechAnalysisResult =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()

            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext fallbackLocalAnalysis(transcript)
            }

            try {
                val prompt = """
                    You are an encouraging, expert speech coach inspired by Atomic Habits.
                    Analyze the following 1-minute speech transcript on the topic "$topic".
                    
                    Transcript:
                    "$transcript"
                    
                    Provide a JSON response with the following exact keys:
                    1. "encouragingNote": An uplifting, identity-reinforcing opening statement highlighting what the speaker did well first.
                    2. "clarityScore": An integer score from 0 to 100 for overall speech clarity and structural flow (beginning, middle, end).
                    3. "clarityFeedback": 2-3 sentences evaluating structure and coherence.
                    4. "fillerWordCount": Total integer count of filler words found (e.g. "um", "uh", "like", "so", "you know", "i mean", "basically", "kind of").
                    5. "fillerWordsList": Array of strings of the specific filler words detected.
                    6. "vocabularyVariety": Evaluation of vocabulary diversity and repetition.
                    7. "vocabularySuggestions": 1-2 specific stronger word choices to replace repetitive or weak words.
                    8. "toneConfidence": Analysis of tone & confidence inferred from phrasing (e.g., hedging words "I guess", "maybe", "I'm not sure" vs assertive statements).
                    9. "actionableTip": ONE specific, practical tip for tomorrow's 1-minute practice.
                    
                    Respond strictly in JSON format.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.4)
                    })
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (!response.isSuccessful || responseString.isEmpty()) {
                    return@withContext fallbackLocalAnalysis(transcript)
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResult = parts?.optJSONObject(0)?.optString("text") ?: ""

                if (textResult.isNotEmpty()) {
                    parseResultJson(textResult, transcript)
                } else {
                    fallbackLocalAnalysis(transcript)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                fallbackLocalAnalysis(transcript)
            }
        }

    private fun parseResultJson(jsonStr: String, rawTranscript: String): SpeechAnalysisResult {
        return try {
            val json = JSONObject(jsonStr)
            val encouragingNote = json.optString("encouragingNote", "Great job taking time to practice your speaking today!")
            val clarityScore = json.optInt("clarityScore", 78)
            val clarityFeedback = json.optString("clarityFeedback", "Your speech had a clear flow and communicated key points effectively.")
            val fillerWordCount = json.optInt("fillerWordCount", countFillerWordsLocally(rawTranscript))
            
            val fillerList = mutableListOf<String>()
            val fillerJsonArray = json.optJSONArray("fillerWordsList")
            if (fillerJsonArray != null) {
                for (i in 0 until fillerJsonArray.length()) {
                    fillerList.add(fillerJsonArray.getString(i))
                }
            } else if (fillerWordCount > 0) {
                fillerList.add("um")
            }

            val vocabularyVariety = json.optString("vocabularyVariety", "Good vocabulary variety across key statements.")
            val vocabularySuggestions = json.optString("vocabularySuggestions", "Try substituting 'good' with 'exceptional' or 'compelling'.")
            val toneConfidence = json.optString("toneConfidence", "Assertive tone overall with clear value statements.")
            val actionableTip = json.optString("actionableTip", "Focus on pausing briefly instead of using filler words when switching topics.")

            SpeechAnalysisResult(
                encouragingNote = encouragingNote,
                clarityScore = clarityScore,
                clarityFeedback = clarityFeedback,
                fillerWordCount = fillerWordCount,
                fillerWordsList = fillerList,
                vocabularyVariety = vocabularyVariety,
                vocabularySuggestions = vocabularySuggestions,
                toneConfidence = toneConfidence,
                actionableTip = actionableTip
            )
        } catch (e: Exception) {
            fallbackLocalAnalysis(rawTranscript)
        }
    }

    private fun fallbackLocalAnalysis(transcript: String): SpeechAnalysisResult {
        val lower = transcript.lowercase()
        val fillerWords = listOf("um", "uh", "like", "so", "you know", "i mean", "basically", "kind of", "sort of", "maybe")
        val foundFillers = mutableListOf<String>()
        var count = 0

        for (filler in fillerWords) {
            val occurrences = lower.split(filler).size - 1
            if (occurrences > 0) {
                count += occurrences
                foundFillers.add("$filler ($occurrences)")
            }
        }

        val wordCount = transcript.split("\\s+".toRegex()).size
        val clarity = if (wordCount > 40) 82 else if (wordCount > 15) 70 else 55

        val hedging = listOf("maybe", "i guess", "i think", "i'm not sure", "probably")
        val hasHedging = hedging.any { lower.contains(it) }

        val tone = if (hasHedging) {
            "Contains hedging language ('maybe', 'I guess'). Try framing statements with direct confidence."
        } else {
            "Strong, confident framing with minimal hesitant markers."
        }

        return SpeechAnalysisResult(
            encouragingNote = "You are building a powerful speaking habit! Expressing your thoughts out loud for 60 seconds builds vocal authority.",
            clarityScore = clarity,
            clarityFeedback = "Your 1-minute session contained approximately $wordCount words with a recognizable narrative structure.",
            fillerWordCount = count,
            fillerWordsList = foundFillers,
            vocabularyVariety = "Solid foundational vocabulary used.",
            vocabularySuggestions = "Consider replacing passive phrases with active verbs (e.g., 'we accomplished' instead of 'we were able to do').",
            toneConfidence = tone,
            actionableTip = "Embrace 1-second silent pauses to replace filler words when gathering your next thought."
        )
    }

    private fun countFillerWordsLocally(transcript: String): Int {
        val lower = transcript.lowercase()
        val fillerWords = listOf("um", "uh", "like", "so", "you know", "i mean", "basically")
        var count = 0
        for (filler in fillerWords) {
            count += (lower.split(filler).size - 1)
        }
        return count
    }
}
