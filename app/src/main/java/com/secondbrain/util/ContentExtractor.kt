package com.secondbrain.util

import android.content.Context
import android.util.Log
import com.secondbrain.data.service.youtube.YouTubeService
import com.secondbrain.data.service.youtube.YouTubeTranscriptScraper
import com.secondbrain.data.service.youtube.YouTubeVideoDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for extracting content from various sources
 */
@Singleton
class ContentExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val youTubeService: YouTubeService,
    private val youTubeTranscriptScraper: YouTubeTranscriptScraper
) {

    companion object {
        private const val TAG = "ContentExtractor"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000
    }

    /**
     * Extract content from a URL
     *
     * @param url The URL to extract content from
     * @return A Result containing the extracted content or an error
     */
    suspend fun extractFromUrl(url: String): Result<UrlContent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting content from URL: $url")

            // Validate URL
            val validatedUrl = validateUrl(url)

            when {
                isYouTubeUrl(validatedUrl) -> extractYouTubeContent(validatedUrl)
                isGoogleDocsUrl(validatedUrl) -> extractGoogleDocsContent(validatedUrl)
                isGoogleSlidesUrl(validatedUrl) -> extractGoogleSlidesContent(validatedUrl)
                isPdfUrl(validatedUrl) -> extractPdfContent(validatedUrl)
                else -> extractWebpageContent(validatedUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting content from URL: $url", e)
            Result.failure(e)
        }
    }

    /**
     * Validate and normalize a URL
     *
     * @param url The URL to validate
     * @return The validated and normalized URL
     * @throws IllegalArgumentException if the URL is invalid
     */
    private fun validateUrl(url: String): String {
        // Add https:// if missing
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        // Validate URL format
        try {
            URL(normalizedUrl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: $normalizedUrl", e)
        }

        return normalizedUrl
    }

    /**
     * Check if a URL is a YouTube video
     */
    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com/watch") || url.contains("youtu.be/")
    }

    /**
     * Check if a URL is a Google Docs document
     */
    private fun isGoogleDocsUrl(url: String): Boolean {
        return url.contains("docs.google.com/document")
    }

    /**
     * Check if a URL is a Google Slides presentation
     */
    private fun isGoogleSlidesUrl(url: String): Boolean {
        return url.contains("docs.google.com/presentation")
    }

    /**
     * Check if a URL is a PDF
     */
    private fun isPdfUrl(url: String): Boolean {
        return url.endsWith(".pdf", ignoreCase = true)
    }

    /**
     * Extract content from a YouTube video
     */
    private suspend fun extractYouTubeContent(url: String): Result<UrlContent> {
        try {
            // Extract video ID using the YouTube service
            val videoId = youTubeService.extractVideoId(url) ?: ""

            if (videoId.isEmpty()) {
                return Result.failure(IOException("Could not extract YouTube video ID from URL: $url"))
            }

            // Get video details from the YouTube API
            val videoDetailsResult = youTubeService.getVideoDetails(videoId)

            if (videoDetailsResult.isFailure) {
                // Fallback to basic extraction if API fails
                return extractYouTubeContentBasic(url, videoId)
            }

            val videoDetails = videoDetailsResult.getOrNull()!!

            // Try to get transcript
            val transcriptResult = youTubeTranscriptScraper.fetchTranscript(videoId)
            val transcriptText = if (transcriptResult.isSuccess) {
                val segments = transcriptResult.getOrNull()!!
                youTubeTranscriptScraper.getTranscriptWithTimestamps(segments)
            } else {
                "Transcript not available for this video."
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

                append("Transcript:\n$transcriptText")
            }

            return Result.success(
                UrlContent(
                    title = videoDetails.title,
                    content = enhancedContent,
                    url = url,
                    thumbnailUrl = videoDetails.thumbnailUrl,
                    sourceType = "YouTube",
                    metadata = mapOf(
                        "videoId" to videoId,
                        "channelTitle" to videoDetails.channelTitle,
                        "duration" to videoDetails.duration.toString(),
                        "durationText" to videoDetails.durationText,
                        "viewCount" to videoDetails.viewCount.toString(),
                        "likeCount" to videoDetails.likeCount.toString(),
                        "hasTranscript" to transcriptResult.isSuccess.toString()
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting YouTube content: $url", e)
            return Result.failure(e)
        }
    }

    /**
     * Basic YouTube content extraction as fallback
     */
    private fun extractYouTubeContentBasic(url: String, videoId: String): Result<UrlContent> {
        try {
            // Get video info using Jsoup
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            val title = doc.select("meta[property=og:title]").attr("content")
            val description = doc.select("meta[property=og:description]").attr("content")
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

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

            return Result.success(
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
            return Result.failure(e)
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

    /**
     * Extract content from a Google Docs document
     */
    private fun extractGoogleDocsContent(url: String): Result<UrlContent> {
        // For now, just return basic info since we can't easily extract content
        return Result.success(
            UrlContent(
                title = "Google Docs Document",
                content = "Content from Google Docs document: $url",
                url = url,
                thumbnailUrl = null,
                sourceType = "Google Docs"
            )
        )
    }

    /**
     * Extract content from a Google Slides presentation
     */
    private fun extractGoogleSlidesContent(url: String): Result<UrlContent> {
        // For now, just return basic info since we can't easily extract content
        return Result.success(
            UrlContent(
                title = "Google Slides Presentation",
                content = "Content from Google Slides presentation: $url",
                url = url,
                thumbnailUrl = null,
                sourceType = "Google Slides"
            )
        )
    }

    /**
     * Extract content from a PDF
     */
    private fun extractPdfContent(url: String): Result<UrlContent> {
        // Try to get a thumbnail for the PDF
        val thumbnailUrl = when {
            // For Google Drive PDFs, we can construct a thumbnail URL
            url.contains("drive.google.com") -> {
                val fileId = url.substringAfter("id=").substringBefore("&")
                if (fileId.isNotEmpty()) {
                    "https://drive.google.com/thumbnail?id=$fileId&sz=w1000"
                } else null
            }
            // For other PDFs, we don't have a way to get a thumbnail without downloading
            else -> null
        }

        return Result.success(
            UrlContent(
                title = "PDF Document",
                content = "Content from PDF document: $url",
                url = url,
                thumbnailUrl = thumbnailUrl,
                sourceType = "PDF"
            )
        )
    }

    /**
     * Extract content from a webpage
     */
    private fun extractWebpageContent(url: String): Result<UrlContent> {
        try {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            return Result.success(extractContentFromDocument(doc, url))
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting webpage content: $url", e)
            return Result.failure(e)
        }
    }

    /**
     * Extract content from a Jsoup Document
     */
    private fun extractContentFromDocument(doc: Document, url: String): UrlContent {
        val title = doc.title()

        // Try to get the main content
        val content = extractMainContent(doc)

        // Get the thumbnail image - try multiple sources
        var thumbnailUrl = doc.select("meta[property=og:image]").attr("content")
        Log.d(TAG, "OpenGraph image: $thumbnailUrl")

        // If no OpenGraph image, try Twitter card image
        if (thumbnailUrl.isEmpty()) {
            thumbnailUrl = doc.select("meta[name=twitter:image]").attr("content")
            Log.d(TAG, "Twitter card image: $thumbnailUrl")
        }

        // If still no image, try to find the largest image in the content
        if (thumbnailUrl.isEmpty()) {
            thumbnailUrl = findLargestImage(doc)
            Log.d(TAG, "Largest image found: $thumbnailUrl")
        }

        // Make sure the URL is absolute
        if (thumbnailUrl.isNotEmpty() && !thumbnailUrl.startsWith("http")) {
            if (thumbnailUrl.startsWith("//")) {
                thumbnailUrl = "https:$thumbnailUrl"
            } else if (thumbnailUrl.startsWith("/")) {
                // Get the base URL
                val baseUrl = url.split("/").take(3).joinToString("/")
                thumbnailUrl = "$baseUrl$thumbnailUrl"
            }
            Log.d(TAG, "Converted to absolute URL: $thumbnailUrl")
        }

        // Determine source type
        val sourceType = when {
            url.contains("wikipedia.org") -> "Wikipedia"
            url.contains("medium.com") -> "Medium"
            url.contains("github.com") -> "GitHub"
            url.contains("stackoverflow.com") -> "Stack Overflow"
            else -> "Website"
        }

        return UrlContent(
            title = title,
            content = content,
            url = url,
            thumbnailUrl = thumbnailUrl.ifEmpty { null },
            sourceType = sourceType
        )
    }

    /**
     * Find the largest image in the document that might be suitable as a thumbnail
     */
    private fun findLargestImage(doc: Document): String {
        // Look for images with width and height attributes
        val images = doc.select("img[src][width][height]")

        // Find the largest image by area (width * height)
        var largestImage = ""
        var largestArea = 0

        for (img in images) {
            try {
                val width = img.attr("width").toInt()
                val height = img.attr("height").toInt()
                val area = width * height

                // Skip very small images
                if (area < 10000 || width < 100 || height < 100) continue

                // Skip likely icons and logos
                val src = img.attr("src")
                if (src.contains("logo", ignoreCase = true) ||
                    src.contains("icon", ignoreCase = true)) continue

                if (area > largestArea) {
                    largestArea = area
                    largestImage = src
                }
            } catch (e: Exception) {
                // Skip images with invalid dimensions
                continue
            }
        }

        // If no suitable image found with dimensions, try to find a large image without dimensions
        if (largestImage.isEmpty()) {
            // Look for images in the main content area
            val contentImages = doc.select(".content img, .post-content img, .entry-content img, article img")

            // Filter to only images with src attribute
            val imagesWithSrc = contentImages.filter { element -> element.hasAttr("src") }

            // Use the first content image if available
            if (imagesWithSrc.isNotEmpty()) {
                largestImage = imagesWithSrc.first().attr("src")
            }
        }

        // Make sure the URL is absolute
        if (largestImage.isNotEmpty() && !largestImage.startsWith("http")) {
            if (largestImage.startsWith("//")) {
                largestImage = "https:$largestImage"
            } else if (largestImage.startsWith("/")) {
                // Get the base URL
                val baseUrl = doc.baseUri()
                val domain = baseUrl.split("/").take(3).joinToString("/")
                largestImage = "$domain$largestImage"
            }
        }

        return largestImage
    }

    /**
     * Extract the main content from a document
     */
    private fun extractMainContent(doc: Document): String {
        // Try to find the main content using common selectors
        val contentSelectors = listOf(
            "article",
            ".content",
            ".post-content",
            ".entry-content",
            "#content",
            "main"
        )

        for (selector in contentSelectors) {
            val element = doc.select(selector).first()
            if (element != null) {
                // Remove unwanted elements
                element.select("script, style, nav, header, footer, .comments, .sidebar, .ads").remove()
                return element.text()
            }
        }

        // If no main content found, use the body text
        val body = doc.body()
        body.select("script, style, nav, header, footer, .comments, .sidebar, .ads").remove()
        return body.text()
    }
}

/**
 * Data class representing content extracted from a URL
 */
data class UrlContent(
    val title: String,
    val content: String,
    val url: String,
    val thumbnailUrl: String?,
    val sourceType: String,
    val metadata: Map<String, String> = emptyMap()
)
