package com.secondbrain.data.service.youtube

import com.secondbrain.data.service.ai.SummaryType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generator for YouTube-specific AI prompts
 */
@Singleton
class YouTubePromptGenerator @Inject constructor() {
    
    /**
     * Generate a system prompt for YouTube video summarization
     * 
     * @param videoDetails The YouTube video details
     * @param summaryType The type of summary to generate
     * @param hasTranscript Whether a transcript is available
     * @return A system prompt tailored for YouTube summarization
     */
    fun generateSystemPrompt(
        videoDetails: YouTubeVideoDetails,
        summaryType: SummaryType,
        hasTranscript: Boolean
    ): String {
        val basePrompt = "You are a helpful assistant that creates summaries of YouTube videos."
        
        val videoContext = "This is a ${videoDetails.durationText} video titled \"${videoDetails.title}\" " +
                "by ${videoDetails.channelTitle} with ${formatCount(videoDetails.viewCount)} views."
        
        val transcriptContext = if (hasTranscript) {
            "You have access to the video's transcript, which provides accurate information about the content."
        } else {
            "You don't have access to the video's transcript, so focus on summarizing the available description and metadata."
        }
        
        val summaryTypeInstructions = when (summaryType) {
            SummaryType.CONCISE -> 
                "Create a concise summary that captures the main point of the video in a few sentences. " +
                "Keep it brief and to the point, focusing only on the most important information."
                
            SummaryType.DETAILED -> 
                "Create a detailed summary that covers all the important points discussed in the video. " +
                "Include key explanations, examples, and context to provide a comprehensive overview."
                
            SummaryType.BULLET_POINTS -> 
                "Create a bullet-point summary that lists the main points and key takeaways from the video. " +
                "Organize the points in a logical order and keep each point concise and clear."
                
            SummaryType.QUESTION_ANSWER -> 
                "Create a Q&A summary that presents the video's content as questions and answers. " +
                "Identify the main questions the video addresses and provide clear answers based on the content."
                
            SummaryType.KEY_FACTS -> 
                "Extract the key facts and important data points mentioned in the video. " +
                "Focus on specific information, statistics, definitions, and factual statements."
        }
        
        val structureInstructions = "Structure your summary with clear sections and include timestamps where relevant. " +
                "If the video has distinct parts like introduction, main content, and conclusion, reflect that in your summary."
        
        return listOf(
            basePrompt,
            videoContext,
            transcriptContext,
            summaryTypeInstructions,
            structureInstructions
        ).joinToString("\n\n")
    }
    
    /**
     * Generate a user prompt for YouTube video summarization
     * 
     * @param videoDetails The YouTube video details
     * @param summaryType The type of summary to generate
     * @param hasTranscript Whether a transcript is available
     * @param language The language for the summary
     * @return A user prompt tailored for YouTube summarization
     */
    fun generateUserPrompt(
        videoDetails: YouTubeVideoDetails,
        summaryType: SummaryType,
        hasTranscript: Boolean,
        language: String
    ): String {
        val summaryTypeRequest = when (summaryType) {
            SummaryType.CONCISE -> "Please provide a concise summary"
            SummaryType.DETAILED -> "Please provide a detailed summary"
            SummaryType.BULLET_POINTS -> "Please provide a bullet-point summary"
            SummaryType.QUESTION_ANSWER -> "Please provide a Q&A summary"
            SummaryType.KEY_FACTS -> "Please extract the key facts"
        }
        
        val languageInstruction = if (language.equals("en", ignoreCase = true)) {
            ""
        } else {
            " in $language"
        }
        
        val transcriptNote = if (hasTranscript) {
            "Based on the transcript provided below,"
        } else {
            "Based on the description and metadata provided below,"
        }
        
        return "$transcriptNote $summaryTypeRequest of the YouTube video \"${videoDetails.title}\"$languageInstruction.\n\n" +
                "Include the most important information and key points from the video. " +
                "If there are timestamps in the transcript, include the most significant ones in your summary."
    }
    
    /**
     * Format large numbers for display (e.g., 1.2M, 4.5K)
     */
    private fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000_000 -> String.format("%.1fB", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
