# AI Provider Utilities

This package contains utility classes for handling provider-specific formatting and implementation details for different AI providers.

## Overview

Each AI provider (OpenAI, Claude, Gemini, etc.) has its own way of handling system prompts and formatting requests. These utility classes abstract away those differences, providing a consistent interface for the rest of the application.

## Available Formatters

- **GeminiPromptFormatter**: Formats prompts for Google's Gemini API
- **OpenAiPromptFormatter**: Formats prompts for OpenAI's API
- **ClaudePromptFormatter**: Formats prompts for Anthropic's Claude API
- **DeepSeekPromptFormatter**: Formats prompts for DeepSeek's API
- **OpenRouterPromptFormatter**: Formats prompts for OpenRouter's API

## Usage

Each formatter provides a `formatPrompt` method that takes a system prompt and a user prompt and returns the appropriate format for the specific provider.

```kotlin
// Example with Gemini
val contents = GeminiPromptFormatter.formatPrompt(systemPrompt, userPrompt)
val request = GeminiRequest(
    contents = contents,
    generationConfig = GenerationConfig(...)
)

// Example with OpenAI
val messages = OpenAiPromptFormatter.formatPrompt(systemPrompt, userPrompt)
val request = ChatCompletionRequest(
    model = "gpt-4o",
    messages = messages,
    temperature = 0.3
)
```

## Documentation

For detailed information about how system prompts are handled by each provider, see the [SystemPromptHandlingDocs.md](SystemPromptHandlingDocs.md) file in this package.

## Integration with BaseAiProvider

These formatters are used by the `BaseAiProvider` implementations to ensure consistent handling of system prompts across all AI providers. The `processSystemPrompt` method in each provider class uses the appropriate formatter to format system prompts according to the provider's requirements.

## Extending

To add support for a new AI provider:

1. Create a new formatter class in this package
2. Implement the appropriate formatting logic
3. Update the `SystemPromptHandler` class to include the new provider
4. Document the new provider in the SystemPromptHandlingDocs.md file
