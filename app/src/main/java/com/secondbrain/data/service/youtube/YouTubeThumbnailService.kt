package com.secondbrain.data.service.youtube

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling YouTube video thumbnails with quality fallbacks and caching
 */
@Singleton
class YouTubeThumbnailService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "YouTubeThumbnailService"
        
        // YouTube thumbnail quality options in order of preference
        private val THUMBNAIL_QUALITIES = listOf(
            "maxresdefault.jpg",  // 1080p
            "sddefault.jpg",      // 640p
            "hqdefault.jpg",      // 480p
            "mqdefault.jpg",      // 320p
            "default.jpg"         // 120p
        )
        
        // Cache size - 20 thumbnails
        private const val CACHE_SIZE = 20
    }
    
    // In-memory cache for thumbnail URLs
    private val thumbnailUrlCache = LruCache<String, String>(CACHE_SIZE)
    
    /**
     * Get the best available thumbnail URL for a YouTube video
     * 
     * @param videoId The YouTube video ID
     * @return The best available thumbnail URL
     */
    suspend fun getBestThumbnailUrl(videoId: String): String = withContext(Dispatchers.IO) {
        // Check cache first
        thumbnailUrlCache.get(videoId)?.let {
            Log.d(TAG, "Thumbnail URL cache hit for video ID: $videoId")
            return@withContext it
        }
        
        Log.d(TAG, "Finding best thumbnail for video ID: $videoId")
        
        // Try each quality option in order
        for (quality in THUMBNAIL_QUALITIES) {
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/$quality"
            if (isUrlAccessible(thumbnailUrl)) {
                // Cache the result
                thumbnailUrlCache.put(videoId, thumbnailUrl)
                Log.d(TAG, "Found best thumbnail at quality: $quality")
                return@withContext thumbnailUrl
            }
        }
        
        // Fallback to the lowest quality if none are accessible
        val fallbackUrl = "https://img.youtube.com/vi/$videoId/default.jpg"
        thumbnailUrlCache.put(videoId, fallbackUrl)
        Log.d(TAG, "No accessible thumbnails found, using fallback")
        return@withContext fallbackUrl
    }
    
    /**
     * Check if a URL is accessible
     */
    private fun isUrlAccessible(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error checking URL accessibility: $urlString", e)
            false
        }
    }
    
    /**
     * Get all available thumbnail URLs for a video
     * Useful for UI that wants to show multiple thumbnail options
     */
    suspend fun getAllThumbnailUrls(videoId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val thumbnails = mutableMapOf<String, String>()
        
        for (quality in THUMBNAIL_QUALITIES) {
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/$quality"
            if (isUrlAccessible(thumbnailUrl)) {
                val qualityName = when(quality) {
                    "maxresdefault.jpg" -> "HD (1080p)"
                    "sddefault.jpg" -> "SD (640p)"
                    "hqdefault.jpg" -> "HQ (480p)"
                    "mqdefault.jpg" -> "MQ (320p)"
                    "default.jpg" -> "Default (120p)"
                    else -> quality
                }
                thumbnails[qualityName] = thumbnailUrl
            }
        }
        
        return@withContext thumbnails
    }
    
    /**
     * Download a thumbnail as a Bitmap
     */
    suspend fun downloadThumbnail(videoId: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val thumbnailUrl = getBestThumbnailUrl(videoId)
            val url = URL(thumbnailUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()
            
            if (bitmap != null) {
                Result.success(bitmap)
            } else {
                Result.failure(IOException("Failed to decode bitmap from URL: $thumbnailUrl"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading thumbnail for video ID: $videoId", e)
            Result.failure(e)
        }
    }
}
