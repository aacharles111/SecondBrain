package com.secondbrain.util

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.util.UUID

/**
 * Utility class for handling Markdown files
 */
object MarkdownUtils {
    
    /**
     * Save a note as a Markdown file
     * @param context The application context
     * @param title The note title
     * @param content The note content
     * @param sourceUrl The source URL (optional)
     * @return The path to the saved file
     */
    fun saveMarkdownFile(
        context: Context,
        title: String,
        content: String,
        sourceUrl: String? = null
    ): String {
        // Create a file name based on the title
        val fileName = "${title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${UUID.randomUUID().toString().substring(0, 8)}.md"
        
        // Get the app's files directory
        val notesDir = File(context.filesDir, "notes")
        if (!notesDir.exists()) {
            notesDir.mkdirs()
        }
        
        // Create the file
        val file = File(notesDir, fileName)
        
        // Create YAML front matter
        val frontMatter = buildString {
            append("---\n")
            append("title: $title\n")
            append("date: ${System.currentTimeMillis()}\n")
            if (sourceUrl != null) {
                append("url: $sourceUrl\n")
            }
            append("---\n\n")
        }
        
        // Write the content to the file
        FileWriter(file).use { writer ->
            writer.write(frontMatter)
            writer.write(content)
        }
        
        return file.absolutePath
    }
    
    /**
     * Read a Markdown file
     * @param filePath The path to the file
     * @return The file content
     */
    fun readMarkdownFile(filePath: String): String {
        val file = File(filePath)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }
    
    /**
     * Delete a Markdown file
     * @param filePath The path to the file
     * @return True if the file was deleted, false otherwise
     */
    fun deleteMarkdownFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
    
    /**
     * Extract YAML front matter from Markdown content
     * @param content The Markdown content
     * @return A map of front matter keys and values
     */
    fun extractFrontMatter(content: String): Map<String, String> {
        val frontMatterRegex = Regex("^---\\s*\n(.*?)\n---\\s*\n", RegexOption.DOT_MATCHES_ALL)
        val match = frontMatterRegex.find(content) ?: return emptyMap()
        
        val frontMatterContent = match.groupValues[1]
        val result = mutableMapOf<String, String>()
        
        // Parse each line of the front matter
        frontMatterContent.lines().forEach { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                result[key] = value
            }
        }
        
        return result
    }
    
    /**
     * Extract content without front matter
     * @param content The Markdown content
     * @return The content without front matter
     */
    fun extractContentWithoutFrontMatter(content: String): String {
        val frontMatterRegex = Regex("^---\\s*\n(.*?)\n---\\s*\n", RegexOption.DOT_MATCHES_ALL)
        return frontMatterRegex.replace(content, "")
    }
}
