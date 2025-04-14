package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.GeminiApiClient.GeminiContent
import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiPromptFormatterTest {

    @Test
    fun `formatPrompt with null system prompt returns only user message`() {
        // Arrange
        val systemPrompt: String? = null
        val userPrompt = "Summarize this text"

        // Act
        val result = GeminiPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(1, result.size)
        assertEquals(userPrompt, result[0].parts?.get(0)?.text)
    }

    @Test
    fun `formatPrompt with empty system prompt returns only user message`() {
        // Arrange
        val systemPrompt = ""
        val userPrompt = "Summarize this text"

        // Act
        val result = GeminiPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(1, result.size)
        assertEquals(userPrompt, result[0].parts?.get(0)?.text)
    }

    @Test
    fun `formatPrompt with system prompt returns conversation format`() {
        // Arrange
        val systemPrompt = "You are a helpful assistant"
        val userPrompt = "Summarize this text"

        // Act
        val result = GeminiPromptFormatter.formatPrompt(systemPrompt, userPrompt)

        // Assert
        assertEquals(3, result.size)
        
        // First message should be user with system instructions
        assertEquals("user", result[0].role)
        assertEquals("You are an AI assistant with the following instructions: You are a helpful assistant", 
            result[0].parts?.get(0)?.text)
        
        // Second message should be model acknowledgment
        assertEquals("model", result[1].role)
        assertEquals("I understand and will follow these instructions.", 
            result[1].parts?.get(0)?.text)
        
        // Third message should be the actual user query
        assertEquals("user", result[2].role)
        assertEquals(userPrompt, result[2].parts?.get(0)?.text)
    }
}
