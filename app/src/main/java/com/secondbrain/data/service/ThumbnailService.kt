package com.secondbrain.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.secondbrain.R
import com.secondbrain.data.model.CardType
import com.secondbrain.data.service.youtube.YouTubeThumbnailService
import com.secondbrain.util.ContentExtractor
import com.secondbrain.util.PdfProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized service for handling thumbnails for all content types
 */
@Singleton
class ThumbnailService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val youTubeThumbnailService: YouTubeThumbnailService,
    private val linkPreviewService: LinkPreviewService
) {
    companion object {
        private const val TAG = "ThumbnailService"
        private const val CACHE_SIZE = 50 // Cache size for in-memory URL cache
        private const val CONNECT_TIMEOUT = 10000
        private const val READ_TIMEOUT = 10000
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

        // Default colors for text-based thumbnails
        private val THUMBNAIL_COLORS = arrayOf(
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#FF5722")  // Deep Orange
        )
    }

    // In-memory cache for thumbnail URLs
    private val thumbnailUrlCache = LruCache<String, String>(CACHE_SIZE)

    /**
     * Get a thumbnail for any URL
     *
     * @param url The URL to get a thumbnail for
     * @param type The type of content (URL, PDF, YouTube, etc.)
     * @param title Optional title for fallback thumbnail generation
     * @return The URL of the thumbnail, or null if no thumbnail could be found
     */
    suspend fun getThumbnailForUrl(
        url: String,
        type: CardType,
        title: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting thumbnail for URL: $url of type: $type")

            // Check cache first
            val cacheKey = "${url}_${type.name}"
            thumbnailUrlCache.get(cacheKey)?.let {
                Log.d(TAG, "Thumbnail URL cache hit for: $cacheKey")
                return@withContext it
            }

            // Get thumbnail based on content type
            var thumbnailUrl: String? = null

            when (type) {
                CardType.URL -> {
                    thumbnailUrl = if (isYouTubeUrl(url)) {
                        getYouTubeThumbnail(url)
                    } else {
                        getWebpageThumbnail(url)
                    }
                }
                CardType.PDF -> thumbnailUrl = getPdfThumbnail(url)
                CardType.SEARCH -> thumbnailUrl = getSearchResultThumbnail(url, title)
                CardType.NOTE -> thumbnailUrl = generateTextThumbnail(title ?: "Note", type)
                CardType.AUDIO -> thumbnailUrl = generateTextThumbnail(title ?: "Audio", type)
            }

            // If we still don't have a thumbnail, generate a text-based one
            if (thumbnailUrl.isNullOrEmpty()) {
                Log.d(TAG, "No thumbnail found, generating fallback text thumbnail")
                thumbnailUrl = generateTextThumbnail(title ?: url, type)
            }

            // If we found a thumbnail, cache it
            if (!thumbnailUrl.isNullOrEmpty()) {
                Log.d(TAG, "Caching thumbnail URL: $thumbnailUrl for key: $cacheKey")
                thumbnailUrlCache.put(cacheKey, thumbnailUrl)
            } else {
                Log.e(TAG, "Failed to generate any thumbnail for: $url")
            }

            return@withContext thumbnailUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error getting thumbnail for URL: $url", e)
            // Always return a valid thumbnail, even in case of errors
            val fallbackThumbnail = generateTextThumbnail(title ?: url, type)
            Log.d(TAG, "Generated fallback thumbnail due to error: $fallbackThumbnail")
            return@withContext fallbackThumbnail
        }
    }

    /**
     * Get a thumbnail for a YouTube video
     */
    private suspend fun getYouTubeThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val videoId = extractYouTubeVideoId(url) ?: return@withContext null
            return@withContext youTubeThumbnailService.getBestThumbnailUrl(videoId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting YouTube thumbnail for URL: $url", e)
            return@withContext null
        }
    }

    /**
     * Get a thumbnail for a webpage
     */
    private suspend fun getWebpageThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            // Skip if URL is empty or not a valid URL
            if (url.isEmpty()) {
                Log.d(TAG, "Empty URL provided for webpage thumbnail")
                return@withContext null
            }

            // First try using LinkPreviewService
            val linkPreviewThumbnail = linkPreviewService.getThumbnailUrl(url)
            if (!linkPreviewThumbnail.isNullOrEmpty()) {
                Log.d(TAG, "Found thumbnail using LinkPreviewService: $linkPreviewThumbnail")

                // If it's a remote URL, download it locally
                if (linkPreviewThumbnail.startsWith("http") && isUrlAccessible(linkPreviewThumbnail)) {
                    val localThumbnail = downloadAndSaveThumbnail(linkPreviewThumbnail)
                    if (localThumbnail != null) {
                        Log.d(TAG, "Downloaded LinkPreview thumbnail to: $localThumbnail")
                        return@withContext localThumbnail
                    }
                }

                return@withContext linkPreviewThumbnail
            }

            // If LinkPreviewService fails, fall back to manual extraction
            Log.d(TAG, "LinkPreviewService failed, falling back to manual extraction")

            try {
                // Try to get the webpage
                val doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(CONNECT_TIMEOUT)
                    .get()

                // Try OpenGraph image
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

                // If we have a valid thumbnail URL, download it locally
                if (thumbnailUrl.isNotEmpty() && thumbnailUrl.startsWith("http") && isUrlAccessible(thumbnailUrl)) {
                    val localThumbnail = downloadAndSaveThumbnail(thumbnailUrl)
                    if (localThumbnail != null) {
                        Log.d(TAG, "Downloaded thumbnail to: $localThumbnail")
                        return@withContext localThumbnail
                    }

                    // If we couldn't download it, still return the URL
                    return@withContext thumbnailUrl
                }

                // If we still don't have a valid thumbnail, try favicon
                val favicon = doc.select("link[rel~=icon]").attr("href")
                if (favicon.isNotEmpty()) {
                    val faviconUrl = if (favicon.startsWith("http")) {
                        favicon
                    } else if (favicon.startsWith("//")) {
                        "https:$favicon"
                    } else {
                        val baseUrl = url.split("/").take(3).joinToString("/")
                        "$baseUrl${if (favicon.startsWith("/")) "" else "/"}$favicon"
                    }

                    Log.d(TAG, "Trying favicon: $faviconUrl")
                    if (faviconUrl.startsWith("http") && isUrlAccessible(faviconUrl)) {
                        val localFavicon = downloadAndSaveThumbnail(faviconUrl)
                        if (localFavicon != null) {
                            Log.d(TAG, "Downloaded favicon to: $localFavicon")
                            return@withContext localFavicon
                        }

                        // If we couldn't download it, still return the URL
                        return@withContext faviconUrl
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during manual webpage extraction for URL: $url", e)
                // Continue to fallback method
            }

            // If all else fails, generate a text-based thumbnail
            Log.d(TAG, "No thumbnail found, generating text-based thumbnail")
            val domain = try {
                val uri = java.net.URI(url)
                val host = uri.host ?: url
                if (host.startsWith("www.")) host.substring(4) else host
            } catch (e: Exception) { url }

            val textThumbnail = generateTextThumbnail(domain, CardType.URL)
            Log.d(TAG, "Generated text thumbnail: $textThumbnail")
            return@withContext textThumbnail
        } catch (e: Exception) {
            Log.e(TAG, "Error getting webpage thumbnail for URL: $url", e)
            // Generate a text-based thumbnail as a last resort
            val domain = try {
                val uri = java.net.URI(url)
                val host = uri.host ?: url
                if (host.startsWith("www.")) host.substring(4) else host
            } catch (e: Exception) { url }

            val textThumbnail = generateTextThumbnail(domain, CardType.URL)
            Log.d(TAG, "Generated fallback text thumbnail: $textThumbnail")
            return@withContext textThumbnail
        }
    }

    /**
     * Get a thumbnail for a PDF
     */
    private suspend fun getPdfThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            // For Google Drive PDFs, we can construct a thumbnail URL
            if (url.contains("drive.google.com")) {
                val fileId = url.substringAfter("id=").substringBefore("&")
                if (fileId.isNotEmpty()) {
                    val thumbnailUrl = "https://drive.google.com/thumbnail?id=$fileId&sz=w1000"
                    if (isUrlAccessible(thumbnailUrl)) {
                        return@withContext thumbnailUrl
                    }
                }
            }

            // For local PDFs, we need to download and generate a thumbnail
            // This would be handled elsewhere when the PDF is uploaded

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting PDF thumbnail for URL: $url", e)
            return@withContext null
        }
    }

    /**
     * Get a thumbnail for a search result
     */
    private suspend fun getSearchResultThumbnail(url: String, title: String?): String? = withContext(Dispatchers.IO) {
        try {
            // For Wikipedia articles, we can use the Wikipedia API
            if (url.contains("wikipedia.org")) {
                val articleTitle = title ?: url.substringAfterLast("/").replace("_", " ")
                return@withContext getWikipediaThumbnail(articleTitle)
            }

            // For other search results, try to get the webpage thumbnail
            return@withContext getWebpageThumbnail(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting search result thumbnail for URL: $url", e)
            return@withContext null
        }
    }

    /**
     * Get a thumbnail for a Wikipedia article
     */
    private suspend fun getWikipediaThumbnail(title: String): String? = withContext(Dispatchers.IO) {
        try {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val apiUrl = "https://en.wikipedia.org/w/api.php?action=query&titles=$encodedTitle&prop=pageimages&format=json&pithumbsize=500"

            val response = Jsoup.connect(apiUrl)
                .userAgent(USER_AGENT)
                .timeout(CONNECT_TIMEOUT)
                .ignoreContentType(true)
                .execute()
                .body()

            val jsonResponse = org.json.JSONObject(response)
            val pages = jsonResponse.getJSONObject("query").getJSONObject("pages")
            val pageId = pages.keys().next()

            if (pages.getJSONObject(pageId).has("thumbnail")) {
                return@withContext pages.getJSONObject(pageId).getJSONObject("thumbnail").getString("source")
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Wikipedia thumbnail for title: $title", e)
            return@withContext null
        }
    }

    /**
     * Generate a text-based thumbnail for content without images
     */
    suspend fun generateTextThumbnail(text: String, type: CardType): String = withContext(Dispatchers.IO) {
        try {
            // Create a unique filename based on the text
            val hash = MessageDigest.getInstance("MD5").digest(text.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val filename = "thumbnail_$hash.png"

            // Check if the file already exists
            val file = File(context.cacheDir, filename)
            if (file.exists()) {
                Log.d(TAG, "Using existing thumbnail file: ${file.absolutePath}")
                return@withContext "file://${file.absolutePath}"
            }

            // Create a bitmap for the thumbnail
            val width = 500
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Choose a background color based on the text
            val colorIndex = Math.abs(text.hashCode()) % THUMBNAIL_COLORS.size
            val backgroundColor = THUMBNAIL_COLORS[colorIndex]

            // Fill the background
            canvas.drawColor(backgroundColor)

            // Draw the first letter or an icon
            val paint = Paint().apply {
                color = Color.WHITE
                textSize = 120f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            // Get the first letter or an icon based on type
            val letter = when (type) {
                CardType.NOTE -> "N"
                CardType.AUDIO -> "A"
                CardType.PDF -> "P"
                CardType.SEARCH -> "S"
                else -> text.firstOrNull()?.uppercase() ?: "?"
            }

            // Draw the letter in the center
            val xPos = width / 2
            val yPos = (height / 2) - ((paint.descent() + paint.ascent()) / 2)
            canvas.drawText(letter, xPos.toFloat(), yPos, paint)

            // Save the bitmap to a file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Log.d(TAG, "Generated new thumbnail file: ${file.absolutePath}")

            // Return a proper file URI that can be used by AsyncImage
            val fileUri = "file://${file.absolutePath}"
            Log.d(TAG, "Returning thumbnail URI: $fileUri")
            return@withContext fileUri
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text thumbnail for: $text", e)
            return@withContext ""
        }
    }

    /**
     * Download a thumbnail and save it locally
     */
    suspend fun downloadAndSaveThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            // Skip if URL is empty or not a valid URL
            if (url.isEmpty() || !url.startsWith("http")) {
                Log.d(TAG, "Invalid URL for thumbnail download: $url")
                return@withContext null
            }

            // Create a unique filename based on the URL
            val hash = MessageDigest.getInstance("MD5").digest(url.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val filename = "thumbnail_$hash.png"

            // Check if the file already exists
            val file = File(context.cacheDir, filename)
            if (file.exists()) {
                Log.d(TAG, "Using existing downloaded thumbnail: ${file.absolutePath}")
                return@withContext "file://${file.absolutePath}"
            }

            Log.d(TAG, "Downloading thumbnail from URL: $url")

            // Download the image
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.setRequestProperty("User-Agent", USER_AGENT)

            val inputStream = connection.inputStream
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()

            if (bitmap != null) {
                // Save the bitmap to a file
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }

                Log.d(TAG, "Successfully downloaded and saved thumbnail: ${file.absolutePath}")

                // Return a proper file URI that can be used by AsyncImage
                val fileUri = "file://${file.absolutePath}"
                Log.d(TAG, "Returning downloaded thumbnail URI: $fileUri")
                return@withContext fileUri
            } else {
                Log.e(TAG, "Failed to decode bitmap from URL: $url")
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading thumbnail from URL: $url", e)
            return@withContext null
        }
    }

    /**
     * Check if a URL is accessible
     */
    private suspend fun isUrlAccessible(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT

            val responseCode = connection.responseCode
            connection.disconnect()

            return@withContext responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            return@withContext false
        }
    }

    /**
     * Find the largest image in a document
     */
    private fun findLargestImage(doc: org.jsoup.nodes.Document): String {
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

        // If no suitable image found with width/height attributes, try all images
        if (largestImage.isEmpty()) {
            val allImages = doc.select("img[src]")
            for (img in allImages) {
                val src = img.attr("src")

                // Skip likely icons and logos
                if (src.contains("logo", ignoreCase = true) ||
                    src.contains("icon", ignoreCase = true)) continue

                // Take the first reasonably-sized image
                if (src.isNotEmpty() && !src.endsWith(".svg")) {
                    largestImage = src
                    break
                }
            }
        }

        return largestImage
    }

    /**
     * Extract YouTube video ID from URL
     */
    private fun extractYouTubeVideoId(url: String): String? {
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
     * Check if a URL is a YouTube URL
     */
    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }
}
