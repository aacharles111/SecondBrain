package com.secondbrain.data.service.youtube

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with YouTube videos
 */
@Singleton
class YouTubeService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "YouTubeService"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000
    }

    /**
     * Extract video ID from a YouTube URL
     */
    fun extractVideoId(url: String): String? {
        return when {
            url.contains("youtube.com/watch") -> {
                val uri = java.net.URI(url)
                val query = uri.query.split("&")
                query.find { it.startsWith("v=") }?.substring(2)
            }
            url.contains("youtu.be/") -> {
                val uri = java.net.URI(url)
                uri.path.substring(1)
            }
            else -> null
        }
    }

    /**
     * Get video details from YouTube
     */
    suspend fun getVideoDetails(videoId: String): Result<YouTubeVideoDetails> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching video details for ID: $videoId")

            // Fetch the video page
            val url = "https://www.youtube.com/watch?v=$videoId"
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            // Extract basic metadata
            val title = doc.select("meta[property=og:title]").attr("content")
            val description = doc.select("meta[property=og:description]").attr("content")
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

            // Extract channel info
            val channelName = doc.select("link[itemprop=name]").attr("content")
            val channelUrl = doc.select("span[itemprop=author] link[itemprop=url]").attr("href")
            val channelId = channelUrl.substringAfterLast("/")

            // Try to extract duration
            val durationText = try {
                val scriptTags = doc.select("script")
                var durationStr = ""
                for (script in scriptTags) {
                    val content = script.html()
                    if (content.contains("\"lengthSeconds\":")) {
                        val regex = "\"lengthSeconds\":\"(\\d+)\"".toRegex()
                        val matchResult = regex.find(content)
                        if (matchResult != null) {
                            val seconds = matchResult.groupValues[1].toLong()
                            durationStr = formatDuration(seconds)
                            break
                        }
                    }
                }
                durationStr
            } catch (e: Exception) {
                "Unknown"
            }

            // Create video details
            val videoDetails = YouTubeVideoDetails(
                id = videoId,
                title = title.ifEmpty { "YouTube Video" },
                description = description,
                channelTitle = channelName.ifEmpty { "Unknown Channel" },
                channelId = channelId,
                publishedAt = "", // Not easily available without API
                thumbnailUrl = thumbnailUrl,
                duration = 0, // Not easily available without API
                durationText = durationText,
                viewCount = 0, // Not easily available without API
                likeCount = 0, // Not easily available without API
                commentCount = 0, // Not easily available without API
                tags = emptyList() // Not easily available without API
            )

            Result.success(videoDetails)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video details", e)
            Result.failure(e)
        }
    }

    /**
     * Format duration in seconds to a human-readable string
     */
    private fun formatDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }
}

/**
 * Data class for YouTube video details
 */
data class YouTubeVideoDetails(
    val id: String,
    val title: String,
    val description: String,
    val channelTitle: String,
    val channelId: String,
    val publishedAt: String,
    val thumbnailUrl: String,
    val duration: Long, // in seconds
    val durationText: String, // formatted duration
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val tags: List<String>
)
