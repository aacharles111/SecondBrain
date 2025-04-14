package com.secondbrain.data.service.ai.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.secondbrain.data.model.CardType
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import com.secondbrain.data.service.ai.TagGenerationOptions
import com.secondbrain.data.service.ai.TitleGenerationOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for processing AI tasks in the background
 */
class AiProcessingWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val aiServiceManager: AiServiceManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AiProcessingWorker"

        // Input keys
        const val KEY_TASK_TYPE = "task_type"
        const val KEY_CONTENT = "content"
        const val KEY_URI = "uri"
        const val KEY_LANGUAGE = "language"
        const val KEY_SUMMARY_TYPE = "summary_type"
        const val KEY_MAX_LENGTH = "max_length"
        const val KEY_MAX_TAGS = "max_tags"
        const val KEY_CUSTOM_INSTRUCTIONS = "custom_instructions"
        const val KEY_AI_MODEL = "ai_model"
        const val KEY_CONTENT_TYPE = "content_type"

        // Output keys
        const val KEY_RESULT = "result"
        const val KEY_ERROR = "error"

        // Task types
        const val TASK_SUMMARIZE = "summarize"
        const val TASK_TRANSCRIBE = "transcribe"
        const val TASK_EXTRACT_TEXT = "extract_text"
        const val TASK_GENERATE_TAGS = "generate_tags"
        const val TASK_GENERATE_TITLE = "generate_title"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskType = inputData.getString(KEY_TASK_TYPE)
                ?: return@withContext Result.failure(createErrorData("Task type not specified"))

            Log.d(TAG, "Processing AI task: $taskType")

            val result = when (taskType) {
                TASK_SUMMARIZE -> processSummarizeTask()
                TASK_TRANSCRIBE -> processTranscribeTask()
                TASK_EXTRACT_TEXT -> processExtractTextTask()
                TASK_GENERATE_TAGS -> processGenerateTagsTask()
                TASK_GENERATE_TITLE -> processGenerateTitleTask()
                else -> return@withContext Result.failure(createErrorData("Unknown task type: $taskType"))
            }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing AI task", e)
            Result.failure(createErrorData(e.message ?: "Unknown error"))
        }
    }

    /**
     * Process a summarize task
     */
    private suspend fun processSummarizeTask(): Data {
        val content = inputData.getString(KEY_CONTENT)
            ?: return createErrorData("Content not specified")

        val language = inputData.getString(KEY_LANGUAGE) ?: "en"
        val summaryTypeStr = inputData.getString(KEY_SUMMARY_TYPE) ?: "Concise summary"
        val maxLength = inputData.getInt(KEY_MAX_LENGTH, 1000)
        val customInstructions = inputData.getString(KEY_CUSTOM_INSTRUCTIONS)
        val aiModel = inputData.getString(KEY_AI_MODEL)
        val contentTypeStr = inputData.getString(KEY_CONTENT_TYPE)

        val summaryType = when (summaryTypeStr) {
            "Concise summary" -> SummaryType.CONCISE
            "Detailed summary" -> SummaryType.DETAILED
            "Bullet points" -> SummaryType.BULLET_POINTS
            "Question and answer" -> SummaryType.QUESTION_ANSWER
            "Key facts" -> SummaryType.KEY_FACTS
            else -> SummaryType.CONCISE
        }

        val options = SummarizationOptions(
            summaryType = summaryType,
            language = language,
            maxLength = maxLength,
            customInstructions = customInstructions
        )

        // Convert content type string to enum if provided
        val contentType = contentTypeStr?.let {
            try {
                CardType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        val result = aiServiceManager.summarize(content, options, aiModel, contentType)

        return if (result.isSuccess) {
            Data.Builder()
                .putString(KEY_RESULT, result.getOrNull())
                .build()
        } else {
            createErrorData(result.exceptionOrNull()?.message ?: "Error summarizing content")
        }
    }

    /**
     * Process a transcribe task
     */
    private suspend fun processTranscribeTask(): Data {
        val uriString = inputData.getString(KEY_URI)
            ?: return createErrorData("URI not specified")

        val uri = Uri.parse(uriString)
        val language = inputData.getString(KEY_LANGUAGE) ?: "en"
        val aiModel = inputData.getString(KEY_AI_MODEL)

        val result = aiServiceManager.transcribeAudio(uri, language, aiModel)

        return if (result.isSuccess) {
            Data.Builder()
                .putString(KEY_RESULT, result.getOrNull())
                .build()
        } else {
            createErrorData(result.exceptionOrNull()?.message ?: "Error transcribing audio")
        }
    }

    /**
     * Process an extract text task
     */
    private suspend fun processExtractTextTask(): Data {
        val uriString = inputData.getString(KEY_URI)
            ?: return createErrorData("URI not specified")

        val uri = Uri.parse(uriString)
        val language = inputData.getString(KEY_LANGUAGE) ?: "en"
        val aiModel = inputData.getString(KEY_AI_MODEL)

        val result = aiServiceManager.extractTextFromImage(uri, language, aiModel)

        return if (result.isSuccess) {
            Data.Builder()
                .putString(KEY_RESULT, result.getOrNull())
                .build()
        } else {
            createErrorData(result.exceptionOrNull()?.message ?: "Error extracting text from image")
        }
    }

    /**
     * Process a generate tags task
     */
    private suspend fun processGenerateTagsTask(): Data {
        val content = inputData.getString(KEY_CONTENT)
            ?: return createErrorData("Content not specified")

        val language = inputData.getString(KEY_LANGUAGE) ?: "en"
        val maxTags = inputData.getInt(KEY_MAX_TAGS, 5)
        val aiModel = inputData.getString(KEY_AI_MODEL)

        val options = TagGenerationOptions(
            language = language,
            maxTags = maxTags
        )

        val result = aiServiceManager.generateTags(content, language, maxTags, aiModel)

        return if (result.isSuccess) {
            val tags = result.getOrNull()?.joinToString(",") ?: ""
            Data.Builder()
                .putString(KEY_RESULT, tags)
                .build()
        } else {
            createErrorData(result.exceptionOrNull()?.message ?: "Error generating tags")
        }
    }

    /**
     * Process a generate title task
     */
    private suspend fun processGenerateTitleTask(): Data {
        val content = inputData.getString(KEY_CONTENT)
            ?: return createErrorData("Content not specified")

        val language = inputData.getString(KEY_LANGUAGE) ?: "en"
        val aiModel = inputData.getString(KEY_AI_MODEL)

        val options = TitleGenerationOptions(
            language = language,
            maxLength = 100
        )

        val result = aiServiceManager.generateTitle(content, language, aiModel)

        return if (result.isSuccess) {
            Data.Builder()
                .putString(KEY_RESULT, result.getOrNull())
                .build()
        } else {
            createErrorData(result.exceptionOrNull()?.message ?: "Error generating title")
        }
    }

    /**
     * Create error data
     */
    private fun createErrorData(errorMessage: String): Data {
        Log.e(TAG, "Error: $errorMessage")
        return Data.Builder()
            .putString(KEY_ERROR, errorMessage)
            .build()
    }
}
