package com.secondbrain.data.service.youtube

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scraping YouTube video transcripts with enhanced reliability and language support
 */
@Singleton
class YouTubeTranscriptScraper @Inject constructor() {
    companion object {
        private const val TAG = "YouTubeTranscriptScraper"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000

        // Common languages for transcripts
        private val COMMON_LANGUAGES = listOf(
            "en", // English
            "es", // Spanish
            "fr", // French
            "de", // German
            "it", // Italian
            "pt", // Portuguese
            "ru", // Russian
            "ja", // Japanese
            "ko", // Korean
            "zh", // Chinese
            "ar", // Arabic
            "hi"  // Hindi
        )
    }

    /**
     * Fetch transcript for a YouTube video with enhanced language support
     *
     * @param videoId The YouTube video ID
     * @param preferredLanguage The preferred language code (ISO 639-1, e.g., 'en' for English)
     * @return A Result containing the transcript segments or an error
     */
    suspend fun fetchTranscript(
        videoId: String,
        preferredLanguage: String = Locale.getDefault().language
    ): Result<List<TranscriptSegment>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching transcript for video ID: $videoId, preferred language: $preferredLanguage")

            // Try the preferred language first
            val preferredResult = fetchTranscriptWithLanguage(videoId, preferredLanguage)
            if (preferredResult.isSuccess && preferredResult.getOrNull()?.isNotEmpty() == true) {
                return@withContext preferredResult
            }

            // If preferred language fails, try English as fallback
            if (preferredLanguage != "en") {
                Log.d(TAG, "Preferred language transcript not found, trying English")
                val englishResult = fetchTranscriptWithLanguage(videoId, "en")
                if (englishResult.isSuccess && englishResult.getOrNull()?.isNotEmpty() == true) {
                    return@withContext englishResult
                }
            }

            // If English fails, try other common languages
            for (language in COMMON_LANGUAGES) {
                if (language != preferredLanguage && language != "en") {
                    Log.d(TAG, "Trying language: $language")
                    val result = fetchTranscriptWithLanguage(videoId, language)
                    if (result.isSuccess && result.getOrNull()?.isNotEmpty() == true) {
                        return@withContext result
                    }
                }
            }

            // If all language-specific attempts fail, try without specifying language
            Log.d(TAG, "All language-specific attempts failed, trying without language specification")
            val genericResult = fetchTranscriptWithLanguage(videoId, null)
            if (genericResult.isSuccess && genericResult.getOrNull()?.isNotEmpty() == true) {
                return@withContext genericResult
            }

            // If XML method fails, try alternative method
            Log.d(TAG, "XML method failed, trying alternative method")
            val alternativeResult = fetchTranscriptAlternative(videoId)
            if (alternativeResult.isSuccess && alternativeResult.getOrNull()?.isNotEmpty() == true) {
                return@withContext alternativeResult
            }

            // If all methods fail, return failure
            return@withContext Result.failure(IOException("No transcript available for video: $videoId"))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transcript", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Fetch transcript with a specific language
     */
    private suspend fun fetchTranscriptWithLanguage(
        videoId: String,
        languageCode: String?
    ): Result<List<TranscriptSegment>> = withContext(Dispatchers.IO) {
        try {
            // Fetch the video page
            val url = "https://www.youtube.com/watch?v=$videoId"
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            // Extract the page source
            val html = doc.html()

            // Look for the transcript data in the page source
            val pattern = Pattern.compile("\"captionTracks\":\\[\\{\"baseUrl\":\"(.*?)\"")
            val matcher = pattern.matcher(html)

            if (!matcher.find()) {
                return@withContext Result.failure(IOException("No transcript found for video: $videoId"))
            }

            // Get the transcript URL
            var transcriptUrl = matcher.group(1)
                .replace("\\u0026", "&")
                .replace("\\", "")

            // Add language parameter if specified
            if (languageCode != null && !transcriptUrl.contains("lang=")) {
                transcriptUrl += "&lang=$languageCode"
            }

            // Fetch the transcript XML
            val transcriptXml = Jsoup.connect(transcriptUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .execute()
                .body()

            // Parse the transcript XML
            val segments = parseTranscriptXml(transcriptXml)

            if (segments.isEmpty()) {
                return@withContext Result.failure(IOException("Failed to parse transcript for video: $videoId with language: $languageCode"))
            }

            Result.success(segments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transcript with language: $languageCode", e)
            Result.failure(e)
        }
    }

    /**
     * Parse transcript XML to extract text and timestamps
     */
    private fun parseTranscriptXml(xml: String): List<TranscriptSegment> {
        val segments = mutableListOf<TranscriptSegment>()

        try {
            // Extract text elements
            val textPattern = Pattern.compile("<text start=\"(\\d+\\.\\d+)\" dur=\"(\\d+\\.\\d+)\">(.*?)</text>")
            val matcher = textPattern.matcher(xml)

            while (matcher.find()) {
                val startTime = matcher.group(1).toDouble()
                val duration = matcher.group(2).toDouble()
                val text = matcher.group(3)
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .replace("<[^>]*>".toRegex(), "") // Remove HTML tags

                segments.add(
                    TranscriptSegment(
                        text = text,
                        startTime = startTime,
                        duration = duration,
                        formattedStartTime = formatTime(startTime)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing transcript XML", e)
        }

        return segments
    }

    /**
     * Alternative method to fetch transcript using YouTube's internal API
     * This is a backup method in case the XML approach fails
     */
    suspend fun fetchTranscriptAlternative(videoId: String): Result<List<TranscriptSegment>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching transcript (alternative method) for video ID: $videoId")

            // Fetch the video page
            val url = "https://www.youtube.com/watch?v=$videoId"
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            // Extract the page source
            val html = doc.html()

            // Look for the ytInitialData JSON
            val dataPattern = Pattern.compile("var ytInitialData = (.*?);</script>")
            val dataMatcher = dataPattern.matcher(html)

            if (!dataMatcher.find()) {
                return@withContext Result.failure(IOException("Could not find transcript data for video: $videoId"))
            }

            val jsonData = dataMatcher.group(1)
            val jsonObject = JSONObject(jsonData)

            // Navigate through the JSON structure to find transcript data
            // This is complex and may change with YouTube's UI updates
            // Simplified version for demonstration

            val segments = mutableListOf<TranscriptSegment>()

            // This is a placeholder - actual implementation would parse the JSON structure
            // to extract transcript segments

            if (segments.isEmpty()) {
                return@withContext Result.failure(IOException("Failed to parse transcript data for video: $videoId"))
            }

            Result.success(segments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transcript (alternative method)", e)
            Result.failure(e)
        }
    }

    /**
     * Format time in seconds to MM:SS format
     */
    private fun formatTime(timeSeconds: Double): String {
        val totalSeconds = timeSeconds.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    /**
     * Get full transcript text
     */
    fun getFullTranscriptText(segments: List<TranscriptSegment>): String {
        return segments.joinToString("\n") { it.text }
    }

    /**
     * Get transcript with timestamps
     */
    fun getTranscriptWithTimestamps(segments: List<TranscriptSegment>): String {
        return segments.joinToString("\n") { "[${it.formattedStartTime}] ${it.text}" }
    }

    /**
     * Get transcript optimized for summarization
     * This removes redundant phrases, filler words, and groups text into logical paragraphs
     */
    fun getOptimizedTranscriptForSummarization(segments: List<TranscriptSegment>): String {
        // Step 1: Combine all text
        val fullText = getFullTranscriptText(segments)

        // Step 2: Remove common filler phrases and words
        val fillerPhrases = listOf(
            "um", "uh", "like", "you know", "I mean", "sort of", "kind of",
            "actually", "basically", "literally", "so yeah", "right", "okay so",
            "you see", "you see what I'm saying", "you know what I mean"
        )

        var cleanedText = fullText
        fillerPhrases.forEach { phrase ->
            cleanedText = cleanedText.replace("\\b$phrase\\b".toRegex(RegexOption.IGNORE_CASE), "")
        }

        // Step 3: Remove repeated sentences or phrases (common in auto-generated transcripts)
        val sentences = cleanedText.split(".", "!", "?").filter { it.trim().isNotEmpty() }
        val uniqueSentences = mutableListOf<String>()

        for (sentence in sentences) {
            val trimmed = sentence.trim()
            if (uniqueSentences.isEmpty() || !uniqueSentences.last().equals(trimmed, ignoreCase = true)) {
                uniqueSentences.add(trimmed)
            }
        }

        // Step 4: Group into paragraphs (approximately every 5 sentences)
        val paragraphs = uniqueSentences.chunked(5).map { it.joinToString(". ") + "." }

        return paragraphs.joinToString("\n\n")
    }

    /**
     * Detect key segments in a transcript (e.g., intro, main points, conclusion)
     */
    fun detectKeySegments(segments: List<TranscriptSegment>): Map<String, String> {
        val result = mutableMapOf<String, String>()

        if (segments.isEmpty()) return result

        // Get intro (first ~10% of transcript)
        val introSize = (segments.size * 0.1).toInt().coerceAtLeast(1)
        val introSegments = segments.take(introSize)
        result["intro"] = getFullTranscriptText(introSegments)

        // Get conclusion (last ~10% of transcript)
        val conclusionSize = (segments.size * 0.1).toInt().coerceAtLeast(1)
        val conclusionSegments = segments.takeLast(conclusionSize)
        result["conclusion"] = getFullTranscriptText(conclusionSegments)

        // Get main content (middle ~80%)
        val mainSegments = segments.drop(introSize).dropLast(conclusionSize)
        result["main"] = getFullTranscriptText(mainSegments)

        return result
    }
}

/**
 * Data class for transcript segment
 */
data class TranscriptSegment(
    val text: String,
    val startTime: Double, // in seconds
    val duration: Double, // in seconds
    val formattedStartTime: String // MM:SS format
)
