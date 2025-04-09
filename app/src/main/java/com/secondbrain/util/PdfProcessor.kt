package com.secondbrain.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            
            return@withContext Result.success(
                PdfContent(
                    title = title,
                    author = author,
                    pageCount = pageCount,
                    content = content,
                    uri = uri.toString()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from PDF", e)
            return@withContext Result.failure(e)
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
    val uri: String
)
