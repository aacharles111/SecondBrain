package com.secondbrain.data.service

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
 * Service for fetching link previews
 */
@Singleton
class LinkPreviewService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LinkPreviewService"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000
    }

    /**
     * Data class to hold preview data
     */
    data class PreviewData(
        val url: String,
        val title: String?,
        val description: String?,
        val imageUrl: String?,
        val siteName: String?,
        val favIcon: String?
    )

    /**
     * Get a preview for a URL
     *
     * @param url The URL to get a preview for
     * @return A Result containing the preview data or an error
     */
    suspend fun getPreview(url: String): Result<PreviewData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting preview for URL: $url")

            // Fetch the webpage
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            // Extract metadata
            val title = doc.title()
            val description = doc.select("meta[name=description]").attr("content")
                .ifEmpty { doc.select("meta[property=og:description]").attr("content") }

            // Get the thumbnail image - try multiple sources
            var imageUrl = doc.select("meta[property=og:image]").attr("content")

            // If no OpenGraph image, try Twitter card image
            if (imageUrl.isEmpty()) {
                imageUrl = doc.select("meta[name=twitter:image]").attr("content")
            }

            // Get site name
            val siteName = doc.select("meta[property=og:site_name]").attr("content")
                .ifEmpty { extractDomain(url) }

            // Get favicon
            val favIcon = doc.select("link[rel~=icon]").attr("href")

            // Make sure URLs are absolute
            val absoluteImageUrl = makeAbsoluteUrl(imageUrl, url)
            val absoluteFavIcon = makeAbsoluteUrl(favIcon, url)

            val previewData = PreviewData(
                url = url,
                title = title,
                description = description,
                imageUrl = absoluteImageUrl,
                siteName = siteName,
                favIcon = absoluteFavIcon
            )

            return@withContext Result.success(previewData)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting preview for URL: $url", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Get a thumbnail URL for a URL
     *
     * @param url The URL to get a thumbnail for
     * @return The thumbnail URL, or null if no thumbnail could be found
     */
    suspend fun getThumbnailUrl(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val previewResult = getPreview(url)

            if (previewResult.isSuccess) {
                val previewData = previewResult.getOrNull()!!

                // Check if we have an image URL
                if (!previewData.imageUrl.isNullOrEmpty()) {
                    return@withContext previewData.imageUrl
                }

                // If no image URL but we have a favicon, use that
                if (!previewData.favIcon.isNullOrEmpty()) {
                    return@withContext previewData.favIcon
                }
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting thumbnail URL for URL: $url", e)
            return@withContext null
        }
    }

    /**
     * Get metadata for a URL
     *
     * @param url The URL to get metadata for
     * @return A map of metadata, or an empty map if no metadata could be found
     */
    suspend fun getMetadata(url: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val previewResult = getPreview(url)

            if (previewResult.isSuccess) {
                val previewData = previewResult.getOrNull()!!

                val metadata = mutableMapOf<String, String>()

                // Add available metadata
                if (!previewData.title.isNullOrEmpty()) {
                    metadata["title"] = previewData.title
                }

                if (!previewData.description.isNullOrEmpty()) {
                    metadata["description"] = previewData.description
                }

                if (!previewData.siteName.isNullOrEmpty()) {
                    metadata["siteName"] = previewData.siteName
                }

                if (!previewData.url.isNotEmpty()) {
                    metadata["url"] = previewData.url
                }

                if (!previewData.imageUrl.isNullOrEmpty()) {
                    metadata["imageUrl"] = previewData.imageUrl
                }

                if (!previewData.favIcon.isNullOrEmpty()) {
                    metadata["favIcon"] = previewData.favIcon
                }

                return@withContext metadata
            }

            return@withContext emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting metadata for URL: $url", e)
            return@withContext emptyMap()
        }
    }

    /**
     * Make a URL absolute
     *
     * @param url The URL to make absolute
     * @param baseUrl The base URL to use
     * @return The absolute URL
     */
    private fun makeAbsoluteUrl(url: String, baseUrl: String): String? {
        if (url.isEmpty()) return null

        return when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> {
                val baseUri = java.net.URI(baseUrl)
                "${baseUri.scheme}://${baseUri.host}$url"
            }
            else -> {
                val baseUri = java.net.URI(baseUrl)
                "${baseUri.scheme}://${baseUri.host}/${url}"
            }
        }
    }

    /**
     * Extract the domain from a URL
     */
    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: return url

            // Remove www. prefix if present
            if (host.startsWith("www.")) {
                host.substring(4)
            } else {
                host
            }
        } catch (e: Exception) {
            url
        }
    }
}
