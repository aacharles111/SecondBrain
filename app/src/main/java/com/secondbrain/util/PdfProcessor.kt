package com.secondbrain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for processing PDF files
 */
object PdfProcessor {

    private const val TAG = "PdfProcessor"

    /**
     * Extract text from a PDF file
     *
     * @param context The application context
     * @param uri The URI of the PDF file
     * @return A Result containing the extracted text or an error
     */
    suspend fun extractText(context: Context, uri: Uri): Result<PdfContent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting text from PDF: $uri")

            // Open the PDF document
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IOException("Could not open PDF file"))

            val reader = PdfReader(inputStream)
            val pdfDocument = PdfDocument(reader)

            // Extract metadata
            val info = pdfDocument.documentInfo
            val title = info.title ?: "Untitled PDF"
            val author = info.author ?: "Unknown Author"
            val pageCount = pdfDocument.numberOfPages

            // Extract text from each page
            val textBuilder = StringBuilder()
            for (i in 1..pageCount) {
                val page = pdfDocument.getPage(i)
                val text = PdfTextExtractor.getTextFromPage(page)
                textBuilder.append(text).append("\n\n")
            }

            // Close resources
            pdfDocument.close()
            reader.close()
            inputStream.close()

            val content = textBuilder.toString().trim()

            // Generate thumbnail from first page
            val thumbnailUrl = generateThumbnail(context, uri)

            return@withContext Result.success(
                PdfContent(
                    title = title,
                    author = author,
                    pageCount = pageCount,
                    content = content,
                    uri = uri.toString(),
                    thumbnailUrl = thumbnailUrl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from PDF", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Generate a thumbnail from the first page of a PDF
     *
     * @param context The application context
     * @param uri The URI of the PDF file
     * @return The file path of the generated thumbnail, or null if generation failed
     */
    private suspend fun generateThumbnail(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Create a temporary file to store the thumbnail
            val thumbnailFile = File(context.cacheDir, "pdf_thumbnail_${System.currentTimeMillis()}.png")

            // Open the PDF file
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return@withContext null

            // Create a PDF renderer
            val renderer = PdfRenderer(fileDescriptor)

            // Open the first page
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)

                // Create a bitmap with the page dimensions
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )

                // Render the page to the bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Save the bitmap to the file
                FileOutputStream(thumbnailFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Close resources
                page.close()
                renderer.close()
                fileDescriptor.close()

                return@withContext thumbnailFile.absolutePath
            }

            // Close resources if no pages were rendered
            renderer.close()
            fileDescriptor.close()

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF thumbnail", e)
            return@withContext null
        }
    }
}

/**
 * Data class representing content extracted from a PDF
 */
data class PdfContent(
    val title: String,
    val author: String,
    val pageCount: Int,
    val content: String,
    val uri: String,
    val thumbnailUrl: String? = null
)
