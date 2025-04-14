package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.OpenRouterApiClient.OpenRouterMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenRouterPromptFormatterTest {

    @Test
    fun `formatPrompt with null system prompt returns only user message`() {
        // Arrange
        val systemPrompt: String? = null
        val userPrompt = "Summarize this text"
        val modelId = "openai/gpt-4"

        // Act
        val result = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

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
        val modelId = "openai/gpt-4"

        // Act
        val result = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

        // Assert
        assertEquals(1, result.size)
        assertEquals("user", result[0].role)
        assertEquals(userPrompt, result[0].content)
    }

    @Test
    fun `formatPrompt with system prompt returns system and user messages for OpenAI model`() {
        // Arrange
        val systemPrompt = "You are a helpful assistant"
        val userPrompt = "Summarize this text"
        val modelId = "openai/gpt-4"

        // Act
        val result = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

        // Assert
        assertEquals(2, result.size)
        
        // First message should be system
        assertEquals("system", result[0].role)
        assertEquals(systemPrompt, result[0].content)
        
        // Second message should be user
        assertEquals("user", result[1].role)
        assertEquals(userPrompt, result[1].content)
    }

    @Test
    fun `formatPrompt with system prompt returns system and user messages for Claude model`() {
        // Arrange
        val systemPrompt = "You are a helpful assistant"
        val userPrompt = "Summarize this text"
        val modelId = "anthropic/claude-3-opus"

        // Act
        val result = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

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
