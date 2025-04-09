package com.secondbrain.data.service.ai.content

import com.secondbrain.data.service.ai.SummaryType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating specialized prompts based on content type
 */
@Singleton
class SpecializedPromptGenerator @Inject constructor() {
    
    /**
     * Generate a specialized system prompt for summarization based on content type
     */
    fun generateSystemPrompt(contentType: ContentType, summaryType: SummaryType): String {
        val basePrompt = when (summaryType) {
            SummaryType.CONCISE -> "You are a helpful assistant that creates concise summaries. Keep the summary brief and to the point, focusing only on the most important information."
            SummaryType.DETAILED -> "You are a helpful assistant that creates detailed summaries. Include all important details, explanations, and context in your summary."
            SummaryType.BULLET_POINTS -> "You are a helpful assistant that creates bullet point summaries. Format your summary as a list of bullet points, each covering a key point from the content."
            SummaryType.QUESTION_ANSWER -> "You are a helpful assistant that creates Q&A summaries. Format your summary as a series of questions and answers that cover the key points from the content."
            SummaryType.KEY_FACTS -> "You are a helpful assistant that extracts key facts. Identify and list the most important facts from the content."
        }
        
        val specializedInstructions = when (contentType) {
            ContentType.ACADEMIC -> generateAcademicInstructions(summaryType)
            ContentType.NEWS -> generateNewsInstructions(summaryType)
            ContentType.TECHNICAL -> generateTechnicalInstructions(summaryType)
            ContentType.CREATIVE -> generateCreativeInstructions(summaryType)
            ContentType.BUSINESS -> generateBusinessInstructions(summaryType)
            ContentType.PERSONAL -> generatePersonalInstructions(summaryType)
            ContentType.UNKNOWN -> ""
        }
        
        return if (specializedInstructions.isNotEmpty()) {
            "$basePrompt\n\n$specializedInstructions"
        } else {
            basePrompt
        }
    }
    
    /**
     * Generate a specialized user prompt for summarization based on content type
     */
    fun generateUserPrompt(
        contentType: ContentType,
        summaryType: SummaryType,
        language: String,
        customInstructions: String? = null
    ): String {
        val basePrompt = when (summaryType) {
            SummaryType.CONCISE -> "Create a concise summary of the following content in $language:"
            SummaryType.DETAILED -> "Create a detailed summary of the following content in $language:"
            SummaryType.BULLET_POINTS -> "Summarize the following content as bullet points in $language:"
            SummaryType.QUESTION_ANSWER -> "Create a Q&A summary of the following content in $language:"
            SummaryType.KEY_FACTS -> "Extract the key facts from the following content in $language:"
        }
        
        val specializedInstructions = when (contentType) {
            ContentType.ACADEMIC -> generateAcademicUserPrompt(summaryType)
            ContentType.NEWS -> generateNewsUserPrompt(summaryType)
            ContentType.TECHNICAL -> generateTechnicalUserPrompt(summaryType)
            ContentType.CREATIVE -> generateCreativeUserPrompt(summaryType)
            ContentType.BUSINESS -> generateBusinessUserPrompt(summaryType)
            ContentType.PERSONAL -> generatePersonalUserPrompt(summaryType)
            ContentType.UNKNOWN -> ""
        }
        
        val fullPrompt = if (specializedInstructions.isNotEmpty()) {
            "$basePrompt\n\n$specializedInstructions"
        } else {
            basePrompt
        }
        
        return if (customInstructions.isNullOrEmpty()) {
            fullPrompt
        } else {
            "$fullPrompt\n\nAdditional instructions: $customInstructions"
        }
    }
    
    /**
     * Generate specialized instructions for academic content
     */
    private fun generateAcademicInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For academic content, focus on the research question, methodology, key findings, and implications. Use formal language and maintain academic rigor."
            SummaryType.DETAILED -> "For academic content, structure your summary to include the research question, theoretical framework, methodology, results, discussion, and implications. Maintain academic terminology and cite key references if mentioned."
            SummaryType.BULLET_POINTS -> "For academic content, organize bullet points by research components: research question, methodology, key findings, limitations, and implications. Use precise academic terminology."
            SummaryType.QUESTION_ANSWER -> "For academic content, structure questions around: What was the research question? What methodology was used? What were the key findings? What are the implications? What limitations were acknowledged?"
            SummaryType.KEY_FACTS -> "For academic content, extract facts related to the research question, methodology, sample size, key findings, statistical significance, limitations, and implications."
        }
    }
    
    /**
     * Generate specialized instructions for news content
     */
    private fun generateNewsInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For news content, focus on the 5 W's (Who, What, When, Where, Why) and How. Prioritize the most newsworthy information following the inverted pyramid structure."
            SummaryType.DETAILED -> "For news content, follow the inverted pyramid structure with the most important information first. Include relevant quotes, context, and background information. Maintain journalistic objectivity."
            SummaryType.BULLET_POINTS -> "For news content, organize bullet points by importance, starting with the most newsworthy information. Include the 5 W's (Who, What, When, Where, Why) and How."
            SummaryType.QUESTION_ANSWER -> "For news content, structure questions around: What happened? Who was involved? When and where did it occur? Why did it happen? What are the implications or next steps?"
            SummaryType.KEY_FACTS -> "For news content, extract facts related to the main event, key figures involved, location, timing, causes, and consequences."
        }
    }
    
    /**
     * Generate specialized instructions for technical content
     */
    private fun generateTechnicalInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For technical content, focus on the main concepts, technologies, or processes described. Use precise technical terminology and maintain accuracy."
            SummaryType.DETAILED -> "For technical content, structure your summary to include the problem being addressed, the technical approach or solution, implementation details, and results or outcomes. Maintain technical accuracy and use appropriate terminology."
            SummaryType.BULLET_POINTS -> "For technical content, organize bullet points by technical components: problem statement, approach, implementation details, technologies used, results, and limitations."
            SummaryType.QUESTION_ANSWER -> "For technical content, structure questions around: What problem is being addressed? What technical approach or solution is proposed? How is it implemented? What technologies are used? What are the results or outcomes?"
            SummaryType.KEY_FACTS -> "For technical content, extract facts related to the technical problem, approach, implementation details, technologies used, performance metrics, and limitations."
        }
    }
    
    /**
     * Generate specialized instructions for creative content
     */
    private fun generateCreativeInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For creative content, focus on the main themes, characters, plot points, or artistic elements. Avoid spoilers unless necessary for understanding."
            SummaryType.DETAILED -> "For creative content, structure your summary to include the setting, characters, plot, themes, and style. Preserve the tone of the original work and highlight notable creative elements."
            SummaryType.BULLET_POINTS -> "For creative content, organize bullet points by creative elements: setting, characters, plot points, themes, style, and notable quotes or passages."
            SummaryType.QUESTION_ANSWER -> "For creative content, structure questions around: What is the setting? Who are the main characters? What happens in the plot? What themes are explored? What creative techniques are used?"
            SummaryType.KEY_FACTS -> "For creative content, extract facts related to the setting, characters, plot points, themes, style, and notable creative elements."
        }
    }
    
    /**
     * Generate specialized instructions for business content
     */
    private fun generateBusinessInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For business content, focus on the key business insights, strategies, market trends, or financial information. Highlight actionable information and business implications."
            SummaryType.DETAILED -> "For business content, structure your summary to include the business context, challenges, strategies, market analysis, financial information, and recommendations. Use appropriate business terminology."
            SummaryType.BULLET_POINTS -> "For business content, organize bullet points by business components: market situation, challenges, strategies, financial data, competitive analysis, and recommendations."
            SummaryType.QUESTION_ANSWER -> "For business content, structure questions around: What is the business context? What challenges are addressed? What strategies are proposed? What market trends are relevant? What are the financial implications? What recommendations are made?"
            SummaryType.KEY_FACTS -> "For business content, extract facts related to the business context, market data, financial information, competitive analysis, strategies, and recommendations."
        }
    }
    
    /**
     * Generate specialized instructions for personal content
     */
    private fun generatePersonalInstructions(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "For personal content, focus on the main experiences, reflections, or emotions expressed. Maintain the personal voice and perspective."
            SummaryType.DETAILED -> "For personal content, structure your summary to include the personal context, experiences, reflections, emotions, and insights. Preserve the personal voice and respect the subjective nature of the content."
            SummaryType.BULLET_POINTS -> "For personal content, organize bullet points by personal elements: context, experiences, reflections, emotions, insights, and future intentions."
            SummaryType.QUESTION_ANSWER -> "For personal content, structure questions around: What is the personal context? What experiences are described? What reflections are shared? What emotions are expressed? What insights or lessons are gained?"
            SummaryType.KEY_FACTS -> "For personal content, extract facts related to the personal context, experiences, reflections, emotions, insights, and future intentions."
        }
    }
    
    /**
     * Generate specialized user prompt for academic content
     */
    private fun generateAcademicUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is academic content. Focus on the research question, methodology, key findings, and implications."
            SummaryType.DETAILED -> "This is academic content. Include the research question, theoretical framework, methodology, results, discussion, and implications."
            SummaryType.BULLET_POINTS -> "This is academic content. Organize bullet points by research components: research question, methodology, key findings, limitations, and implications."
            SummaryType.QUESTION_ANSWER -> "This is academic content. Include questions about the research question, methodology, key findings, implications, and limitations."
            SummaryType.KEY_FACTS -> "This is academic content. Extract facts about the research question, methodology, sample size, key findings, statistical significance, limitations, and implications."
        }
    }
    
    /**
     * Generate specialized user prompt for news content
     */
    private fun generateNewsUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is news content. Focus on the 5 W's (Who, What, When, Where, Why) and How."
            SummaryType.DETAILED -> "This is news content. Follow the inverted pyramid structure with the most important information first. Include relevant quotes, context, and background information."
            SummaryType.BULLET_POINTS -> "This is news content. Organize bullet points by importance, starting with the most newsworthy information. Include the 5 W's (Who, What, When, Where, Why) and How."
            SummaryType.QUESTION_ANSWER -> "This is news content. Include questions about what happened, who was involved, when and where it occurred, why it happened, and the implications or next steps."
            SummaryType.KEY_FACTS -> "This is news content. Extract facts about the main event, key figures involved, location, timing, causes, and consequences."
        }
    }
    
    /**
     * Generate specialized user prompt for technical content
     */
    private fun generateTechnicalUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is technical content. Focus on the main concepts, technologies, or processes described."
            SummaryType.DETAILED -> "This is technical content. Include the problem being addressed, the technical approach or solution, implementation details, and results or outcomes."
            SummaryType.BULLET_POINTS -> "This is technical content. Organize bullet points by technical components: problem statement, approach, implementation details, technologies used, results, and limitations."
            SummaryType.QUESTION_ANSWER -> "This is technical content. Include questions about the problem being addressed, the technical approach or solution, implementation details, technologies used, and results or outcomes."
            SummaryType.KEY_FACTS -> "This is technical content. Extract facts about the technical problem, approach, implementation details, technologies used, performance metrics, and limitations."
        }
    }
    
    /**
     * Generate specialized user prompt for creative content
     */
    private fun generateCreativeUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is creative content. Focus on the main themes, characters, plot points, or artistic elements. Avoid spoilers unless necessary for understanding."
            SummaryType.DETAILED -> "This is creative content. Include the setting, characters, plot, themes, and style. Preserve the tone of the original work."
            SummaryType.BULLET_POINTS -> "This is creative content. Organize bullet points by creative elements: setting, characters, plot points, themes, style, and notable quotes or passages."
            SummaryType.QUESTION_ANSWER -> "This is creative content. Include questions about the setting, main characters, plot, themes, and creative techniques used."
            SummaryType.KEY_FACTS -> "This is creative content. Extract facts about the setting, characters, plot points, themes, style, and notable creative elements."
        }
    }
    
    /**
     * Generate specialized user prompt for business content
     */
    private fun generateBusinessUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is business content. Focus on the key business insights, strategies, market trends, or financial information."
            SummaryType.DETAILED -> "This is business content. Include the business context, challenges, strategies, market analysis, financial information, and recommendations."
            SummaryType.BULLET_POINTS -> "This is business content. Organize bullet points by business components: market situation, challenges, strategies, financial data, competitive analysis, and recommendations."
            SummaryType.QUESTION_ANSWER -> "This is business content. Include questions about the business context, challenges, strategies, market trends, financial implications, and recommendations."
            SummaryType.KEY_FACTS -> "This is business content. Extract facts about the business context, market data, financial information, competitive analysis, strategies, and recommendations."
        }
    }
    
    /**
     * Generate specialized user prompt for personal content
     */
    private fun generatePersonalUserPrompt(summaryType: SummaryType): String {
        return when (summaryType) {
            SummaryType.CONCISE -> "This is personal content. Focus on the main experiences, reflections, or emotions expressed. Maintain the personal voice and perspective."
            SummaryType.DETAILED -> "This is personal content. Include the personal context, experiences, reflections, emotions, and insights. Preserve the personal voice."
            SummaryType.BULLET_POINTS -> "This is personal content. Organize bullet points by personal elements: context, experiences, reflections, emotions, insights, and future intentions."
            SummaryType.QUESTION_ANSWER -> "This is personal content. Include questions about the personal context, experiences, reflections, emotions, and insights or lessons gained."
            SummaryType.KEY_FACTS -> "This is personal content. Extract facts about the personal context, experiences, reflections, emotions, insights, and future intentions."
        }
    }
}
