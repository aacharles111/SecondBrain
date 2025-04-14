# System Prompt Handling in Different AI Providers

This document explains how system prompts are handled by different AI providers in the SecondBrain app.

## What is a System Prompt?

A system prompt is a set of instructions given to an AI model that defines its behavior, capabilities, and constraints. Unlike user prompts, which are specific queries or requests, system prompts set the overall context and rules for the AI's responses.

## Provider-Specific Implementations

### 1. OpenAI (GPT models)

**Format:** Dedicated system message in the messages array

```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant that creates concise summaries."
    },
    {
      "role": "user",
      "content": "Summarize this article: ..."
    }
  ],
  "temperature": 0.3
}
```

**Implementation Notes:**
- OpenAI models have native support for system prompts via the `role: "system"` message
- System messages should be placed at the beginning of the conversation
- The system message sets the behavior for the entire conversation
- Implementation: `OpenAiPromptFormatter.formatPrompt()`

### 2. Anthropic (Claude models)

**Format:** Top-level system field separate from messages

```json
{
  "model": "claude-3-opus-20240229",
  "system": "You are a helpful assistant that creates concise summaries.",
  "messages": [
    {
      "role": "user",
      "content": "Summarize this article: ..."
    }
  ],
  "temperature": 0.3
}
```

**Implementation Notes:**
- Claude uses a top-level `system` field separate from the messages array
- The system prompt applies to the entire conversation
- Claude's API is different from OpenAI's in this regard
- Implementation: `ClaudePromptFormatter.formatPrompt()`

### 3. Google (Gemini models)

**Format:** Conversation-based approach (no dedicated system field)

```json
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": "You are an AI assistant with the following instructions: You are a helpful assistant that creates concise summaries."
        }
      ]
    },
    {
      "role": "model",
      "parts": [
        {
          "text": "I understand and will follow these instructions."
        }
      ]
    },
    {
      "role": "user",
      "parts": [
        {
          "text": "Summarize this article: ..."
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.3,
    "maxOutputTokens": 1000
  }
}
```

**Implementation Notes:**
- Gemini does not have a dedicated system prompt field
- We implement system prompts as a conversation with:
  1. A user message containing the system instructions
  2. A model response acknowledging the instructions
  3. The actual user query
- This approach simulates the effect of a system prompt
- Implementation: `GeminiPromptFormatter.formatPrompt()`

### 4. DeepSeek

**Format:** Similar to OpenAI with dedicated system message

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant that creates concise summaries."
    },
    {
      "role": "user",
      "content": "Summarize this article: ..."
    }
  ],
  "temperature": 0.3
}
```

**Implementation Notes:**
- DeepSeek follows the OpenAI format with a dedicated system message
- System messages should be placed at the beginning of the conversation
- Implementation: `DeepSeekPromptFormatter.formatPrompt()`

### 5. OpenRouter

**Format:** Varies based on the underlying model

```json
{
  "model": "openai/gpt-4o",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant that creates concise summaries."
    },
    {
      "role": "user",
      "content": "Summarize this article: ..."
    }
  ],
  "temperature": 0.3
}
```

**Implementation Notes:**
- OpenRouter supports multiple model providers
- It generally follows the OpenAI format with a dedicated system message
- For models that don't support system messages, OpenRouter handles the conversion
- Implementation: `OpenRouterPromptFormatter.formatPrompt()`

## Best Practices

1. **Keep system prompts concise and clear**
   - Focus on defining the AI's role and constraints
   - Avoid including specific instructions that should be in the user prompt

2. **Be consistent across providers**
   - Use the same system prompt content across different providers
   - Let the formatters handle the provider-specific implementation details

3. **Test system prompts with each provider**
   - Different providers may interpret system prompts differently
   - Verify that the behavior is consistent across providers

4. **Use the provider-specific formatters**
   - Always use the appropriate formatter for each provider
   - Don't manually construct the prompt format

## Implementation in SecondBrain

In the SecondBrain app, system prompt handling is implemented through:

1. The `BaseAiProvider` interface, which defines the `processSystemPrompt` method
2. Provider-specific implementations of this method in each provider class
3. Utility formatter classes in the `com.secondbrain.data.service.ai.provider` package
4. The `SystemPromptHandler` utility class for common functionality

This architecture ensures consistent handling of system prompts across all AI providers while respecting their individual API requirements.
