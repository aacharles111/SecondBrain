package com.secondbrain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.secondbrain.R
import com.secondbrain.data.model.CardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Utility class for generating thumbnails
 */
object ThumbnailGenerator {
    private const val TAG = "ThumbnailGenerator"

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

    /**
     * Generate a text-based thumbnail
     *
     * @param context The application context
     * @param text The text to use for the thumbnail (usually the title)
     * @param type The type of content
     * @param width The width of the thumbnail
     * @param height The height of the thumbnail
     * @return The file path of the generated thumbnail
     */
    suspend fun generateTextThumbnail(
        context: Context,
        text: String,
        type: CardType,
        width: Int = 500,
        height: Int = 300
    ): String = withContext(Dispatchers.IO) {
        try {
            // Create a unique filename based on the text and type
            val hash = MessageDigest.getInstance("MD5").digest("${text}_${type.name}".toByteArray())
                .joinToString("") { "%02x".format(it) }
            val filename = "thumbnail_$hash.png"

            // Check if the file already exists
            val file = File(context.cacheDir, filename)
            if (file.exists()) {
                return@withContext file.absolutePath
            }

            // Create a bitmap for the thumbnail
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Choose a background color based on the text
            val colorIndex = Math.abs(text.hashCode()) % THUMBNAIL_COLORS.size
            val backgroundColor = THUMBNAIL_COLORS[colorIndex]

            // Fill the background
            canvas.drawColor(backgroundColor)

            // Get the appropriate icon for the content type
            val icon: Drawable? = when (type) {
                CardType.NOTE -> ContextCompat.getDrawable(context, R.drawable.ic_note)
                CardType.PDF -> ContextCompat.getDrawable(context, R.drawable.ic_pdf)
                CardType.AUDIO -> ContextCompat.getDrawable(context, R.drawable.ic_audio)
                CardType.SEARCH -> ContextCompat.getDrawable(context, R.drawable.ic_search)
                CardType.URL -> ContextCompat.getDrawable(context, R.drawable.ic_link)
            }

            if (icon != null) {
                // Draw the icon in the center
                val iconSize = width / 3
                val left = (width - iconSize) / 2
                val top = (height - iconSize) / 2

                icon.setBounds(left, top, left + iconSize, top + iconSize)
                icon.setTint(Color.WHITE)
                icon.alpha = 200 // Slightly transparent
                icon.draw(canvas)

                // Draw the first letter of the text
                val letter = text.firstOrNull()?.uppercase() ?: "?"
                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = width / 4f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }

                val xPos = width / 2
                val yPos = (height * 0.75).toInt()
                canvas.drawText(letter, xPos.toFloat(), yPos.toFloat(), paint)
            } else {
                // If no icon, just draw the first letter
                val letter = text.firstOrNull()?.uppercase() ?: "?"
                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = width / 3f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }

                val xPos = width / 2
                val yPos = height / 2 - ((paint.descent() + paint.ascent()) / 2)
                canvas.drawText(letter, xPos.toFloat(), yPos, paint)
            }

            // Draw a subtle pattern or texture
            drawSubtlePattern(canvas, width, height)

            // Save the bitmap to a file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            return@withContext file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text thumbnail for: $text", e)
            return@withContext ""
        }
    }

    /**
     * Draw a subtle pattern or texture on the canvas
     */
    private fun drawSubtlePattern(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 30 // Very transparent
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Draw a grid pattern
        val gridSize = width / 10
        for (x in 0..width step gridSize) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
        }

        for (y in 0..height step gridSize) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        }
    }

    /**
     * Generate a domain-based thumbnail for a URL
     */
    suspend fun generateDomainThumbnail(
        context: Context,
        url: String,
        width: Int = 500,
        height: Int = 300
    ): String = withContext(Dispatchers.IO) {
        try {
            // Extract the domain from the URL
            val domain = extractDomain(url)

            // Create a unique filename based on the domain
            val hash = MessageDigest.getInstance("MD5").digest(domain.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val filename = "domain_$hash.png"

            // Check if the file already exists
            val file = File(context.cacheDir, filename)
            if (file.exists()) {
                return@withContext file.absolutePath
            }

            // Create a bitmap for the thumbnail
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Choose a background color based on the domain
            val colorIndex = Math.abs(domain.hashCode()) % THUMBNAIL_COLORS.size
            val backgroundColor = THUMBNAIL_COLORS[colorIndex]

            // Fill the background
            canvas.drawColor(backgroundColor)

            // Draw the domain name
            val paint = Paint().apply {
                color = Color.WHITE
                textSize = width / 12f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val xPos = width / 2
            val yPos = (height / 2 + 50).toFloat()
            canvas.drawText(domain, xPos.toFloat(), yPos, paint)

            // Draw a globe icon
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_globe)
            if (icon != null) {
                val iconSize = width / 3
                val left = (width - iconSize) / 2
                val top = (height - iconSize) / 2 - 50

                icon.setBounds(left, top, left + iconSize, top + iconSize)
                icon.setTint(Color.WHITE)
                icon.alpha = 200 // Slightly transparent
                icon.draw(canvas)
            }

            // Save the bitmap to a file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            return@withContext file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error generating domain thumbnail for: $url", e)
            return@withContext ""
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
