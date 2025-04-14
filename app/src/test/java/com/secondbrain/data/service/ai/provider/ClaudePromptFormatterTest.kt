package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.ClaudeApiClient.ClaudeMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class ClaudePromptFormatterTest {

    @Test
    fun `formatPrompt with null system prompt returns empty system string and user message`() {
        // Arrange
        val systemPrompt: String? = null
        val userPrompt = "Summarize this text"

        // Act
        val (formattedSystemPrompt, messages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals("", formattedSystemPrompt)
        assertEquals(1, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals(userPrompt, messages[0].content)
    }

    @Test
    fun `formatPrompt with empty system prompt returns empty system string and user message`() {
        // Arrange
        val systemPrompt = ""
        val userPrompt = "Summarize this text"

        // Act
        val (formattedSystemPrompt, messages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals("", formattedSystemPrompt)
        assertEquals(1, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals(userPrompt, messages[0].content)
    }

    @Test
    fun `formatPrompt with system prompt returns system string and user message`() {
        // Arrange
        val systemPrompt = "You are a helpful assistant"
        val userPrompt = "Summarize this text"

        // Act
        val (formattedSystemPrompt, messages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(systemPrompt, formattedSystemPrompt)
        assertEquals(1, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals(userPrompt, messages[0].content)
    }
}
