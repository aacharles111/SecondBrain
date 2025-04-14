package com.secondbrain.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * A simple Markdown text component that supports basic formatting
 * For a more complete solution, consider using a library like Markwon
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        val lines = markdown.split("\n")

        for (line in lines) {
            // Handle headers
            when {
                line.startsWith("# ") -> {
                    append(line.substring(2))
                    addStyle(
                        SpanStyle(
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        length - line.substring(2).length,
                        length
                    )
                }
                line.startsWith("## ") -> {
                    append(line.substring(3))
                    addStyle(
                        SpanStyle(
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        length - line.substring(3).length,
                        length
                    )
                }
                line.startsWith("### ") -> {
                    append(line.substring(4))
                    addStyle(
                        SpanStyle(
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        length - line.substring(4).length,
                        length
                    )
                }
                // Handle bullet points
                line.startsWith("- ") -> {
                    append("• ${line.substring(2)}")
                }
                line.startsWith("* ") -> {
                    append("• ${line.substring(2)}")
                }
                // Handle code blocks
                line.startsWith("```") -> {
                    // Skip code block markers
                    if (line.length > 3) {
                        append(line.substring(3))
                    }
                }
                line.startsWith("`") && line.endsWith("`") -> {
                    append(line.substring(1, line.length - 1))
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        length - (line.length - 2),
                        length
                    )
                }
                // Regular text
                else -> {
                    append(line)
                }
            }

            // Add newline after each line except the last one
            append("\n")
        }

        // Process inline formatting
        val text = toString()

        // Bold
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        boldRegex.findAll(text).forEach { result ->
            val range = result.range
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                range.first,
                range.last + 1
            )

            // Check if this is a label like **Title:** or **Summary:**
            val content = result.groupValues[1]
            if (content.endsWith(":")) {
                // Add extra styling for labels
                addStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    ),
                    range.first,
                    range.last + 1
                )
            }
        }

        // Italic
        val italicRegex = "\\*(.*?)\\*".toRegex()
        italicRegex.findAll(text).forEach { result ->
            val range = result.range
            addStyle(
                SpanStyle(fontStyle = FontStyle.Italic),
                range.first,
                range.last + 1
            )
        }

        // Underline
        val underlineRegex = "__(.*?)__".toRegex()
        underlineRegex.findAll(text).forEach { result ->
            val range = result.range
            addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline),
                range.first,
                range.last + 1
            )
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}
