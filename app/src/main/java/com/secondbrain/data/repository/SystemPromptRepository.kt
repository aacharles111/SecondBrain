package com.secondbrain.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.secondbrain.data.model.CardType
import com.secondbrain.data.service.ai.SummaryType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.systemPromptsDataStore: DataStore<Preferences> by preferencesDataStore(name = "system_prompts")

/**
 * Repository for managing system prompts for different content types and summary types
 */
@Singleton
class SystemPromptRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.systemPromptsDataStore

    companion object {
        // Keys for system prompts based on content type and summary type
        private fun getPromptKey(contentType: String, summaryType: SummaryType): String {
            return "prompt_${contentType.lowercase()}_${summaryType.name.lowercase()}"
        }

        // Default system prompts for different content types and summary types
        val DEFAULT_PROMPTS = mapOf(
            // URL content
            Pair(Pair(CardType.URL.name, SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of web content. " +
                "Extract the most important information from the web page, focusing on the main points. " +
                "Ignore advertisements, navigation elements, and other non-essential content. " +
                "Keep the summary brief and to the point, around 3-5 sentences. " +
                "If this is a YouTube video, focus on summarizing the video's main topic and key points."),

            // YouTube-specific prompts
            Pair(Pair("YOUTUBE", SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of YouTube videos. " +
                "Focus on the main topic, key points, and conclusions of the video. " +
                "If a transcript is available, use it to extract the most important information. " +
                "Include any significant demonstrations, examples, or insights shared in the video. " +
                "Keep the summary brief and to the point, around 3-5 sentences. " +
                "Mention the creator's name if available."),

            Pair(Pair("YOUTUBE", SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of YouTube videos. " +
                "Provide a comprehensive overview of the video content, including all important sections, " +
                "key points, demonstrations, and conclusions. If a transcript is available, use it to " +
                "extract detailed information with timestamps when possible. Include information about " +
                "the creator, their expertise, and the context of the video. Organize the information " +
                "logically, following the structure of the video. Include relevant details while still " +
                "maintaining clarity and focus."),

            Pair(Pair("YOUTUBE", SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of YouTube videos. " +
                "Extract the key points from the video and present them as a clear, organized list. " +
                "If a transcript is available, use it to identify the most important information. " +
                "Include timestamps when possible to help the user navigate to specific parts of the video. " +
                "Organize points in a logical sequence, following the structure of the video. " +
                "Focus on actionable insights, key facts, and main arguments."),

            Pair(Pair("YOUTUBE", SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of YouTube videos. " +
                "Identify the most important questions that the video addresses, and provide clear, concise " +
                "answers based on the content. If a transcript is available, use it to extract accurate information. " +
                "Format as Q&A pairs, organized by the video's main topics. Include timestamps when possible " +
                "to help the user navigate to the relevant parts of the video. Focus on questions that would " +
                "help someone understand the key points without watching the entire video."),

            Pair(Pair("YOUTUBE", SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from YouTube videos. " +
                "Identify and list the most important factual information from the video. " +
                "If a transcript is available, use it to extract accurate information with timestamps when possible. " +
                "Focus on verifiable data, statistics, definitions, steps in tutorials, and concrete information. " +
                "Avoid opinions or interpretations unless they represent the creator's main points. " +
                "Present facts in a clear, organized manner, grouped by topic if appropriate."),

            Pair(Pair(CardType.URL.name, SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of web content. " +
                "Provide a comprehensive overview of the web page, including all important sections, " +
                "key arguments, data points, and conclusions. Organize the information logically " +
                "and maintain the original structure where appropriate. Include relevant details " +
                "while still being concise. " +
                "If this is a YouTube video, provide a detailed summary of the video content, " +
                "including the main topics discussed, key points, and any conclusions or recommendations."),

            Pair(Pair(CardType.URL.name, SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of web content. " +
                "Extract the key points from the web page and present them as a clear, organized list. " +
                "Each bullet point should represent a distinct idea or piece of information. " +
                "Use hierarchical structure if appropriate, with main points and sub-points. " +
                "Focus on facts, arguments, and conclusions."),

            Pair(Pair(CardType.URL.name, SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of web content. " +
                "Identify the most important questions that the web page addresses, and provide " +
                "clear, concise answers based on the content. Format as Q&A pairs. " +
                "Cover the main topics and key information. If the content doesn't explicitly " +
                "frame information as questions, create appropriate questions that would be " +
                "answered by the main points in the content."),

            Pair(Pair(CardType.URL.name, SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from web content. " +
                "Identify and list the most important factual information from the web page. " +
                "Focus on verifiable data, statistics, dates, names, and concrete information. " +
                "Avoid opinions, interpretations, or subjective statements unless they are " +
                "central to the content's purpose. Present facts in a clear, organized manner."),

            // PDF content
            Pair(Pair(CardType.PDF.name, SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of PDF documents. " +
                "Extract the most important information from the document, focusing on the main thesis, " +
                "key arguments, and conclusions. Maintain the logical flow of the original document " +
                "while condensing it significantly. Keep the summary brief and to the point, " +
                "around 3-5 sentences per major section."),

            Pair(Pair(CardType.PDF.name, SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of PDF documents. " +
                "Provide a comprehensive overview of the document, including all important sections, " +
                "key arguments, data points, and conclusions. Preserve the document's structure " +
                "and organization. Include relevant details from figures, tables, and references " +
                "if they are central to understanding the content."),

            Pair(Pair(CardType.PDF.name, SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of PDF documents. " +
                "Extract the key points from each section of the document and present them as a " +
                "clear, organized list. Use hierarchical structure to reflect the document's organization, " +
                "with main points and sub-points. Include important data from tables and figures " +
                "where relevant."),

            Pair(Pair(CardType.PDF.name, SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of PDF documents. " +
                "Identify the most important questions that the document addresses, and provide " +
                "clear, concise answers based on the content. Format as Q&A pairs, organized by " +
                "the document's main sections. If the document doesn't explicitly frame information " +
                "as questions, create appropriate questions that would be answered by the main " +
                "points in each section."),

            Pair(Pair(CardType.PDF.name, SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from PDF documents. " +
                "Identify and list the most important factual information from the document. " +
                "Focus on verifiable data, statistics, dates, names, and concrete information. " +
                "Pay special attention to information presented in tables, figures, and highlighted " +
                "sections. Organize facts by the document's sections for clarity."),

            // AUDIO content
            Pair(Pair(CardType.AUDIO.name, SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of audio transcriptions. " +
                "Extract the most important information from the conversation or monologue, " +
                "focusing on main topics, key points, and conclusions. Ignore filler words, " +
                "repetitions, and tangential remarks. Keep the summary brief and to the point, " +
                "around 3-5 sentences."),

            Pair(Pair(CardType.AUDIO.name, SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of audio transcriptions. " +
                "Provide a comprehensive overview of the conversation or monologue, including all " +
                "important topics, key points, arguments, and conclusions. Maintain the logical flow " +
                "of the discussion. Note any significant emotional tones or emphasis if relevant. " +
                "Include speaker identification if multiple speakers are present."),

            Pair(Pair(CardType.AUDIO.name, SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of audio transcriptions. " +
                "Extract the key points from the conversation or monologue and present them as a " +
                "clear, organized list. Each bullet point should represent a distinct topic or " +
                "important statement. Use hierarchical structure if appropriate, with main points " +
                "and sub-points. Include speaker attribution if multiple speakers are present."),

            Pair(Pair(CardType.AUDIO.name, SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of audio transcriptions. " +
                "Identify the most important questions that are addressed in the audio, and provide " +
                "clear, concise answers based on the content. Format as Q&A pairs. If the audio " +
                "doesn't explicitly frame information as questions, create appropriate questions " +
                "that would be answered by the main points discussed. Include speaker attribution " +
                "if multiple speakers are present."),

            Pair(Pair(CardType.AUDIO.name, SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from audio transcriptions. " +
                "Identify and list the most important factual information from the conversation or monologue. " +
                "Focus on verifiable data, statistics, dates, names, and concrete information. " +
                "Ignore opinions or subjective statements unless they are central to the discussion. " +
                "Include speaker attribution if multiple speakers are present."),

            // NOTE content
            Pair(Pair(CardType.NOTE.name, SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of notes. " +
                "Extract the most important information from the notes, focusing on main ideas " +
                "and key points. Maintain the logical structure of the original notes while " +
                "condensing significantly. Keep the summary brief and to the point."),

            Pair(Pair(CardType.NOTE.name, SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of notes. " +
                "Provide a comprehensive overview of the notes, including all important sections, " +
                "key points, and any conclusions. Preserve the original structure and organization. " +
                "Include relevant details and examples that support the main ideas."),

            Pair(Pair(CardType.NOTE.name, SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of notes. " +
                "Extract the key points from the notes and present them as a clear, organized list. " +
                "Use hierarchical structure to reflect the notes' organization, with main points " +
                "and sub-points. Preserve any existing bullet point structure while condensing " +
                "and clarifying the content."),

            Pair(Pair(CardType.NOTE.name, SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of notes. " +
                "Identify the most important topics in the notes, and frame them as questions with " +
                "clear, concise answers based on the content. Format as Q&A pairs, organized by " +
                "the notes' main sections. Create questions that would help someone understand " +
                "and recall the key information in the notes."),

            Pair(Pair(CardType.NOTE.name, SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from notes. " +
                "Identify and list the most important factual information from the notes. " +
                "Focus on verifiable data, definitions, concepts, and concrete information. " +
                "Organize facts by topic for clarity. Preserve any important relationships " +
                "between facts that are indicated in the notes."),

            // SEARCH content
            Pair(Pair(CardType.SEARCH.name, SummaryType.CONCISE),
                "You are a helpful assistant that creates concise summaries of search results. " +
                "Extract the most important information related to the search query, focusing on " +
                "direct answers and key points. Keep the summary brief and to the point, " +
                "around 3-5 sentences. Prioritize information that directly addresses the search query."),

            Pair(Pair(CardType.SEARCH.name, SummaryType.DETAILED),
                "You are a helpful assistant that creates detailed summaries of search results. " +
                "Provide a comprehensive overview of the information related to the search query, " +
                "including different perspectives, key data points, and relevant context. " +
                "Organize the information logically, grouping related points together. " +
                "Include important details while maintaining focus on the search query."),

            Pair(Pair(CardType.SEARCH.name, SummaryType.BULLET_POINTS),
                "You are a helpful assistant that creates bullet point summaries of search results. " +
                "Extract the key points related to the search query and present them as a clear, " +
                "organized list. Each bullet point should represent a distinct piece of information. " +
                "Use hierarchical structure if appropriate, with main points and sub-points. " +
                "Prioritize information that directly addresses the search query."),

            Pair(Pair(CardType.SEARCH.name, SummaryType.QUESTION_ANSWER),
                "You are a helpful assistant that creates question and answer summaries of search results. " +
                "Start with the main search query as the primary question. Then identify important " +
                "sub-questions related to the topic, and provide clear, concise answers based on " +
                "the search results. Format as Q&A pairs. Cover different aspects of the topic " +
                "and include different perspectives where relevant."),

            Pair(Pair(CardType.SEARCH.name, SummaryType.KEY_FACTS),
                "You are a helpful assistant that extracts key facts from search results. " +
                "Identify and list the most important factual information related to the search query. " +
                "Focus on verifiable data, statistics, dates, names, and concrete information. " +
                "Avoid opinions or interpretations unless they represent significant viewpoints " +
                "on the topic. Present facts in a clear, organized manner.")
        )
    }

    /**
     * Get a system prompt for a specific content type and summary type
     */
    fun getSystemPrompt(contentType: CardType, summaryType: SummaryType): Flow<String> {
        // Handle special case for YouTube
        val contentTypeStr = if (contentType.toString() == "YOUTUBE") "YOUTUBE" else contentType.name
        val key = stringPreferencesKey(getPromptKey(contentTypeStr, summaryType))

        return dataStore.data.map { preferences ->
            val customPrompt = preferences[key]
            if (customPrompt != null) {
                customPrompt
            } else {
                // Try to get the default prompt
                val defaultKey = Pair(contentTypeStr, summaryType)
                DEFAULT_PROMPTS[defaultKey] ?: DEFAULT_PROMPTS[Pair(contentType.name, summaryType)] ?: getDefaultPrompt(summaryType)
            }
        }
    }

    /**
     * Save a custom system prompt for a specific content type and summary type
     */
    suspend fun saveSystemPrompt(contentType: CardType, summaryType: SummaryType, prompt: String) {
        val key = stringPreferencesKey(getPromptKey(contentType.name, summaryType))
        dataStore.edit { preferences ->
            preferences[key] = prompt
        }
    }

    /**
     * Reset a system prompt to its default value
     */
    suspend fun resetSystemPrompt(contentType: CardType, summaryType: SummaryType) {
        val key = stringPreferencesKey(getPromptKey(contentType.name, summaryType))
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    /**
     * Reset all system prompts to their default values
     */
    suspend fun resetAllSystemPrompts() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Get a default generic prompt for a summary type (used as fallback)
     */
    private fun getDefaultPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "You are a helpful assistant that creates concise summaries. Keep the summary brief and to the point, focusing only on the most important information."
            SummaryType.DETAILED -> "You are a helpful assistant that creates detailed summaries. Include all important details, explanations, and context in your summary."
            SummaryType.BULLET_POINTS -> "You are a helpful assistant that creates bullet point summaries. Format your summary as a list of bullet points, each covering a key point from the content."
            SummaryType.QUESTION_ANSWER -> "You are a helpful assistant that creates question and answer summaries. Identify the main questions addressed in the content and provide clear answers based on the information provided."
            SummaryType.KEY_FACTS -> "You are a helpful assistant that extracts key facts from content. Identify and list the most important factual information, focusing on verifiable data, statistics, and concrete information."
        }
    }
}
