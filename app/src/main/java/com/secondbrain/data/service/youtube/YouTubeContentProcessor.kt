package com.secondbrain.data.service.youtube

import android.content.Context
import android.util.Log
import com.secondbrain.data.service.ai.AiProvider
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import com.secondbrain.data.service.youtube.YouTubePromptGenerator
import com.secondbrain.util.UrlContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Specialized processor for YouTube content with enhanced features
 */
@Singleton
class YouTubeContentProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val youTubeService: YouTubeService,
    private val youTubeTranscriptScraper: YouTubeTranscriptScraper,
    private val youtubeThumbnailService: YouTubeThumbnailService,
    private val youTubePromptGenerator: YouTubePromptGenerator,
    private val aiProvider: AiProvider
) {
    companion object {
        private const val TAG = "YouTubeContentProcessor"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000
    }

    /**
     * Process a YouTube URL to extract and enhance content
     *
     * @param url The YouTube URL
     * @param preferredLanguage The preferred language for transcript
     * @return A Result containing the processed content or an error
     */
    suspend fun processYouTubeUrl(
        url: String,
        preferredLanguage: String? = null
    ): Result<UrlContent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing YouTube URL: $url")

            // Extract video ID
            val videoId = youTubeService.extractVideoId(url) ?: ""

            if (videoId.isEmpty()) {
                return@withContext Result.failure(IOException("Could not extract YouTube video ID from URL: $url"))
            }

            // Get video details
            val videoDetailsResult = youTubeService.getVideoDetails(videoId)

            if (videoDetailsResult.isFailure) {
                // Fallback to basic extraction if API fails
                return@withContext extractBasicContent(url, videoId)
            }

            val videoDetails = videoDetailsResult.getOrNull()!!

            // Get best thumbnail
            val thumbnailUrl = youtubeThumbnailService.getBestThumbnailUrl(videoId)

            // Try to get transcript
            val language = preferredLanguage ?: "en"
            val transcriptResult = youTubeTranscriptScraper.fetchTranscript(videoId, language)

            val hasTranscript = transcriptResult.isSuccess
            val transcriptSegments = transcriptResult.getOrNull() ?: emptyList()

            // Process transcript for better summarization
            val processedTranscript = if (hasTranscript) {
                youTubeTranscriptScraper.getOptimizedTranscriptForSummarization(transcriptSegments)
            } else {
                "Transcript not available for this video."
            }

            // Detect key segments
            val keySegments = if (hasTranscript) {
                youTubeTranscriptScraper.detectKeySegments(transcriptSegments)
            } else {
                emptyMap()
            }

            // Create enhanced content with all the details
            val enhancedContent = buildString {
                append("YouTube Video: ${videoDetails.title}\n")
                append("Channel: ${videoDetails.channelTitle}\n")
                append("Published: ${videoDetails.publishedAt}\n")
                append("Duration: ${videoDetails.durationText}\n")
                append("Views: ${formatCount(videoDetails.viewCount)}\n")
                append("Likes: ${formatCount(videoDetails.likeCount)}\n")
                append("Comments: ${formatCount(videoDetails.commentCount)}\n\n")

                if (videoDetails.tags.isNotEmpty()) {
                    append("Tags: ${videoDetails.tags.joinToString(", ")}\n\n")
                }

                append("Description:\n${videoDetails.description}\n\n")

                if (hasTranscript) {
                    append("Transcript:\n$processedTranscript")
                } else {
                    append("Transcript: Not available for this video.")
                }
            }

            return@withContext Result.success(
                UrlContent(
                    title = videoDetails.title,
                    content = enhancedContent,
                    url = url,
                    thumbnailUrl = thumbnailUrl,
                    sourceType = "YouTube",
                    metadata = mapOf(
                        "videoId" to videoId,
                        "channelTitle" to videoDetails.channelTitle,
                        "duration" to videoDetails.duration.toString(),
                        "durationText" to videoDetails.durationText,
                        "viewCount" to videoDetails.viewCount.toString(),
                        "likeCount" to videoDetails.likeCount.toString(),
                        "hasTranscript" to hasTranscript.toString(),
                        "hasIntro" to keySegments.containsKey("intro").toString(),
                        "hasConclusion" to keySegments.containsKey("conclusion").toString()
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing YouTube content: $url", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Extract basic content as fallback
     */
    private suspend fun extractBasicContent(url: String, videoId: String): Result<UrlContent> = withContext(Dispatchers.IO) {
        try {
            // Get video info using Jsoup
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            val title = doc.select("meta[property=og:title]").attr("content")
            val description = doc.select("meta[property=og:description]").attr("content")
            val thumbnailUrl = youtubeThumbnailService.getBestThumbnailUrl(videoId)

            // Create enhanced content with video ID and more context
            val enhancedContent = """YouTube Video: $title
                |Video ID: $videoId
                |URL: $url
                |
                |Description:
                |$description
                |
                |This content was extracted from a YouTube video.
                """.trimMargin()

            return@withContext Result.success(
                UrlContent(
                    title = title.ifEmpty { "YouTube Video" },
                    content = enhancedContent,
                    url = url,
                    thumbnailUrl = thumbnailUrl,
                    sourceType = "YouTube",
                    metadata = mapOf("videoId" to videoId)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting basic YouTube content: $url", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Generate a transcript-based summary of a YouTube video
     */
    suspend fun generateTranscriptSummary(
        videoId: String,
        summaryType: SummaryType = SummaryType.DETAILED,
        language: String = "en"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get video details
            val videoDetailsResult = youTubeService.getVideoDetails(videoId)
            if (videoDetailsResult.isFailure) {
                return@withContext Result.failure(IOException("Failed to get video details for ID: $videoId"))
            }

            val videoDetails = videoDetailsResult.getOrNull()!!

            // Get transcript
            val transcriptResult = youTubeTranscriptScraper.fetchTranscript(videoId, language)
            if (transcriptResult.isFailure) {
                return@withContext Result.failure(IOException("No transcript available for video ID: $videoId"))
            }

            val transcriptSegments = transcriptResult.getOrNull()!!

            // Process transcript for better summarization
            val processedTranscript = youTubeTranscriptScraper.getOptimizedTranscriptForSummarization(transcriptSegments)

            // Detect key segments
            val keySegments = youTubeTranscriptScraper.detectKeySegments(transcriptSegments)

            // Create content for summarization
            val contentToSummarize = buildString {
                append("Title: ${videoDetails.title}\n")
                append("Channel: ${videoDetails.channelTitle}\n\n")

                if (keySegments.containsKey("intro")) {
                    append("Introduction:\n${keySegments["intro"]}\n\n")
                }

                if (keySegments.containsKey("main")) {
                    append("Main Content:\n${keySegments["main"]}\n\n")
                }

                if (keySegments.containsKey("conclusion")) {
                    append("Conclusion:\n${keySegments["conclusion"]}\n\n")
                }

                if (!keySegments.containsKey("main")) {
                    append("Full Transcript:\n$processedTranscript\n")
                }
            }

            // Generate YouTube-specific prompts
            val hasTranscript = transcriptResult.isSuccess
            val systemPrompt = youTubePromptGenerator.generateSystemPrompt(
                videoDetails = videoDetails,
                summaryType = summaryType,
                hasTranscript = hasTranscript
            )

            val userPrompt = youTubePromptGenerator.generateUserPrompt(
                videoDetails = videoDetails,
                summaryType = summaryType,
                hasTranscript = hasTranscript,
                language = language
            )

            // Generate summary using AI
            val options = SummarizationOptions(
                summaryType = summaryType,
                language = language,
                maxLength = 1000,
                systemPrompt = systemPrompt,
                customInstructions = userPrompt
            )

            return@withContext aiProvider.summarizeText(contentToSummarize, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating transcript summary for video ID: $videoId", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Format large numbers for display (e.g., 1.2M, 4.5K)
     */
    private fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000_000 -> String.format("%.1fB", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
