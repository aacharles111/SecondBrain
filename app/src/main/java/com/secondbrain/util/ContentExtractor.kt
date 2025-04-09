package com.secondbrain.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL

/**
 * Utility class for extracting content from various sources
 */
object ContentExtractor {
    
    private const val TAG = "ContentExtractor"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    private const val TIMEOUT_MS = 10000
    
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
    private fun extractYouTubeContent(url: String): Result<UrlContent> {
        try {
            // Extract video ID
            val videoId = when {
                url.contains("youtube.com/watch") -> {
                    val uri = java.net.URI(url)
                    val query = uri.query.split("&")
                    query.find { it.startsWith("v=") }?.substring(2) ?: ""
                }
                url.contains("youtu.be/") -> {
                    val uri = java.net.URI(url)
                    uri.path.substring(1)
                }
                else -> ""
            }
            
            if (videoId.isEmpty()) {
                return Result.failure(IOException("Could not extract YouTube video ID from URL: $url"))
            }
            
            // Get video info using Jsoup
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()
            
            val title = doc.select("meta[property=og:title]").attr("content")
            val description = doc.select("meta[property=og:description]").attr("content")
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
            
            return Result.success(
                UrlContent(
                    title = title.ifEmpty { "YouTube Video" },
                    content = description,
                    url = url,
                    thumbnailUrl = thumbnailUrl,
                    sourceType = "YouTube"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting YouTube content: $url", e)
            return Result.failure(e)
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
        // For now, just return basic info since PDF extraction requires additional libraries
        return Result.success(
            UrlContent(
                title = "PDF Document",
                content = "Content from PDF document: $url",
                url = url,
                thumbnailUrl = null,
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
        
        // Get the thumbnail image
        val thumbnailUrl = doc.select("meta[property=og:image]").attr("content")
        
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
    val sourceType: String
)
