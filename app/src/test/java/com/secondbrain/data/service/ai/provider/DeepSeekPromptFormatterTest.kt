package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.DeepSeekApiClient.DeepSeekMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepSeekPromptFormatterTest {

    @Test
    fun `formatPrompt with null system prompt returns only user message`() {
        // Arrange
        val systemPrompt: String? = null
        val userPrompt = "Summarize this text"

        // Act
        val result = DeepSeekPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(1, result.size)
        assertEquals("user", result[0].role)
        assertEquals(userPrompt, result[0].content)
    }

    @Test
    fun `formatPrompt with empty system prompt returns only user message`() {
        // Arrange
        val systemPrompt = ""
        val userPrompt = "Summarize this text"

        // Act
        val result = DeepSeekPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(1, result.size)
        assertEquals("user", result[0].role)
        assertEquals(userPrompt, result[0].content)
    }

    @Test
    fun `formatPrompt with system prompt returns system and user messages`() {
        // Arrange
        val systemPrompt = "You are a helpful assistant"
        val userPrompt = "Summarize this text"

        // Act
        val result = DeepSeekPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(2, result.size)
        
        // First message should be system
        assertEquals("system", result[0].role)
        assertEquals(systemPrompt, result[0].content)
        
        // Second message should be user
        assertEquals("user", result[1].role)
        assertEquals(userPrompt, result[1].content)
    }
}
