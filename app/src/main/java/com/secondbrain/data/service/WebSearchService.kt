package com.secondbrain.data.service

import android.util.Log
import com.secondbrain.data.model.WebSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for performing web searches
 */
@Singleton
class WebSearchService @Inject constructor() {

    companion object {
        private const val TAG = "WebSearchService"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private const val TIMEOUT_MS = 10000

        // API endpoints
        private const val WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php"
        private const val WIKIDATA_API_URL = "https://www.wikidata.org/w/api.php"
    }

    /**
     * Search DuckDuckGo for a query
     */
    suspend fun searchDuckDuckGo(query: String, maxResults: Int = 5): Result<List<WebSearchResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching DuckDuckGo for: $query")

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://html.duckduckgo.com/html/?q=$encodedQuery"
            Log.d(TAG, "DuckDuckGo search URL: $url")

            val connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)

            Log.d(TAG, "Executing DuckDuckGo search request...")
            val doc = connection.get()
            Log.d(TAG, "DuckDuckGo search response received, document title: ${doc.title()}")

            // Extract search results
            val results = mutableListOf<WebSearchResult>()

            // DuckDuckGo HTML results are in divs with class 'result'
            val elements = doc.select(".result")
            Log.d(TAG, "Found ${elements.size} DuckDuckGo results")

            for (element in elements.take(maxResults)) {
                try {
                    val titleElement = element.select(".result__title").firstOrNull()
                    val linkElement = element.select(".result__url").firstOrNull() ?:
                                      element.select(".result__a").firstOrNull()
                    val snippetElement = element.select(".result__snippet").firstOrNull()

                    if (titleElement != null) {
                        val title = titleElement.text()
                        var link = ""

                        // Get the URL - either from the visible URL element or from the link
                        if (linkElement != null && linkElement.hasAttr("href")) {
                            link = linkElement.attr("href")
                            // Fix relative URLs
                            if (link.startsWith("/")) {
                                link = "https://duckduckgo.com$link"
                            }
                        } else {
                            val parent = titleElement.parent()
                            if (parent != null && parent.hasAttr("href")) {
                                link = parent.attr("href")
                            }
                        }

                        // Clean up the URL if it's a redirect
                        if (link.contains("/l/?kh=") || link.contains("/l/?uddg=")) {
                            try {
                                // Extract the actual URL from DuckDuckGo's redirect
                                link = link.substringAfter("uddg=").substringBefore("&")
                                link = URLDecoder.decode(link, "UTF-8")
                            } catch (e: Exception) {
                                Log.w(TAG, "Error decoding DuckDuckGo redirect URL: $link", e)
                            }
                        }

                        val snippet = snippetElement?.text() ?: ""

                        // Extract domain for display
                        val domain = try {
                            val uri = java.net.URI(link)
                            uri.host
                        } catch (e: Exception) {
                            ""
                        }

                        Log.d(TAG, "Found DuckDuckGo result: title='$title', link='$link', domain='$domain'")

                        // Only add results with valid URLs
                        if (link.isNotEmpty() && (link.startsWith("http") || link.startsWith("https"))) {
                            results.add(
                                WebSearchResult(
                                    title = title,
                                    url = link,
                                    snippet = snippet,
                                    source = "Web",
                                    thumbnailUrl = null // DuckDuckGo HTML results don't include thumbnails
                                )
                            )
                        } else {
                            Log.w(TAG, "Skipping result with invalid URL: $link")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing DuckDuckGo search result element", e)
                }
            }

            Log.d(TAG, "Returning ${results.size} DuckDuckGo search results")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching DuckDuckGo", e)
            Result.failure(e)
        }
    }

    /**
     * Search Wikipedia for a query
     */
    suspend fun searchWikipedia(query: String, maxResults: Int = 5): Result<List<WebSearchResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching Wikipedia for: $query")

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$WIKIPEDIA_API_URL?action=query&list=search&srsearch=$encodedQuery&format=json&utf8=1"

            val response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .execute()
                .body()

            val jsonObject = JSONObject(response)
            val searchResults = jsonObject.getJSONObject("query").getJSONArray("search")

            val results = mutableListOf<WebSearchResult>()

            for (i in 0 until minOf(searchResults.length(), maxResults)) {
                val result = searchResults.getJSONObject(i)
                val title = result.getString("title")
                val snippet = Jsoup.parse(result.getString("snippet")).text()

                // Wikipedia article URL
                val articleUrl = "https://en.wikipedia.org/wiki/${title.replace(" ", "_")}"

                // Try to get a thumbnail
                val thumbnailUrl = try {
                    val pageImageUrl = "$WIKIPEDIA_API_URL?action=query&titles=${URLEncoder.encode(title, "UTF-8")}&prop=pageimages&format=json&pithumbsize=100"
                    val pageImageResponse = Jsoup.connect(pageImageUrl)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .ignoreContentType(true)
                        .execute()
                        .body()

                    val pageImageJson = JSONObject(pageImageResponse)
                    val pages = pageImageJson.getJSONObject("query").getJSONObject("pages")
                    val pageId = pages.keys().next()

                    if (pages.getJSONObject(pageId).has("thumbnail")) {
                        pages.getJSONObject(pageId).getJSONObject("thumbnail").getString("source")
                    } else null
                } catch (e: Exception) {
                    null
                }

                results.add(
                    WebSearchResult(
                        title = title,
                        url = articleUrl,
                        snippet = snippet,
                        source = "Wikipedia",
                        thumbnailUrl = thumbnailUrl
                    )
                )
            }

            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Wikipedia", e)
            Result.failure(e)
        }
    }

    /**
     * Search WikiData for a query
     */
    suspend fun searchWikiData(query: String, maxResults: Int = 5): Result<List<WebSearchResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching WikiData for: $query")

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$WIKIDATA_API_URL?action=wbsearchentities&search=$encodedQuery&language=en&format=json"

            val response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .execute()
                .body()

            val jsonObject = JSONObject(response)
            val searchResults = jsonObject.getJSONArray("search")

            val results = mutableListOf<WebSearchResult>()

            for (i in 0 until minOf(searchResults.length(), maxResults)) {
                val result = searchResults.getJSONObject(i)
                val id = result.getString("id")
                val title = result.getString("label")
                val description = if (result.has("description")) result.getString("description") else ""

                // WikiData entity URL
                val entityUrl = "https://www.wikidata.org/wiki/$id"

                results.add(
                    WebSearchResult(
                        title = title,
                        url = entityUrl,
                        snippet = description,
                        source = "WikiData"
                    )
                )
            }

            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching WikiData", e)
            Result.failure(e)
        }
    }

    /**
     * Search across multiple sources
     */
    suspend fun search(query: String, sources: List<String>, maxResults: Int = 5): Result<List<WebSearchResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching across sources: $sources for query: $query")

            val allResults = mutableListOf<WebSearchResult>()
            val errors = mutableListOf<String>()

            // Search each selected source
            if (sources.contains("Web")) {
                searchDuckDuckGo(query, maxResults).onSuccess { results ->
                    Log.d(TAG, "Web search returned ${results.size} results")
                    allResults.addAll(results)
                }.onFailure { error ->
                    Log.e(TAG, "Web search failed", error)
                    errors.add("Web: ${error.message}")
                }
            }

            if (sources.contains("Wikipedia")) {
                searchWikipedia(query, maxResults).onSuccess { results ->
                    Log.d(TAG, "Wikipedia search returned ${results.size} results")
                    allResults.addAll(results)
                }.onFailure { error ->
                    Log.e(TAG, "Wikipedia search failed", error)
                    errors.add("Wikipedia: ${error.message}")
                }
            }

            if (sources.contains("WikiData")) {
                searchWikiData(query, maxResults).onSuccess { results ->
                    Log.d(TAG, "WikiData search returned ${results.size} results")
                    allResults.addAll(results)
                }.onFailure { error ->
                    Log.e(TAG, "WikiData search failed", error)
                    errors.add("WikiData: ${error.message}")
                }
            }

            // Log the total results
            Log.d(TAG, "Total search results across all sources: ${allResults.size}")

            // If we have results, return them even if some sources failed
            if (allResults.isNotEmpty()) {
                return@withContext Result.success(allResults)
            }

            // If all sources failed, return a failure with all error messages
            if (errors.isNotEmpty()) {
                return@withContext Result.failure(Exception("Search failed: ${errors.joinToString("; ")}."))
            }

            // If no results and no errors, return empty list
            Result.success(allResults)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching across sources", e)
            Result.failure(e)
        }
    }
}
