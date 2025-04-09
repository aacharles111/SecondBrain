package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.secondbrain.data.service.ai.worker.AiProcessingWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing AI processing tasks
 */
@Singleton
class AiProcessingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AiProcessingService"
        
        // Work names
        private const val WORK_SUMMARIZE_PREFIX = "summarize_"
        private const val WORK_TRANSCRIBE_PREFIX = "transcribe_"
        private const val WORK_EXTRACT_TEXT_PREFIX = "extract_text_"
        private const val WORK_GENERATE_TAGS_PREFIX = "generate_tags_"
        private const val WORK_GENERATE_TITLE_PREFIX = "generate_title_"
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    // Processing state
    private val _processingState = MutableStateFlow<Map<String, ProcessingState>>(emptyMap())
    val processingState: StateFlow<Map<String, ProcessingState>> = _processingState.asStateFlow()
    
    /**
     * Summarize content in the background
     */
    fun summarizeContent(
        content: String,
        summaryType: String,
        language: String,
        maxLength: Int? = null,
        customInstructions: String? = null,
        aiModel: String? = null,
        taskId: String = UUID.randomUUID().toString()
    ): String {
        Log.d(TAG, "Scheduling summarize task: $taskId")
        
        // Create input data
        val inputData = Data.Builder()
            .putString(AiProcessingWorker.KEY_TASK_TYPE, AiProcessingWorker.TASK_SUMMARIZE)
            .putString(AiProcessingWorker.KEY_CONTENT, content)
            .putString(AiProcessingWorker.KEY_LANGUAGE, language)
            .putString(AiProcessingWorker.KEY_SUMMARY_TYPE, summaryType)
            .apply {
                maxLength?.let { putInt(AiProcessingWorker.KEY_MAX_LENGTH, it) }
                customInstructions?.let { putString(AiProcessingWorker.KEY_CUSTOM_INSTRUCTIONS, it) }
                aiModel?.let { putString(AiProcessingWorker.KEY_AI_MODEL, it) }
            }
            .build()
        
        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<AiProcessingWorker>()
            .setInputData(inputData)
            .setConstraints(createNetworkConstraints())
            .build()
        
        // Enqueue work
        workManager.enqueueUniqueWork(
            "$WORK_SUMMARIZE_PREFIX$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.PROCESSING)
        
        // Observe work
        observeWork(workRequest.id, taskId)
        
        return taskId
    }
    
    /**
     * Transcribe audio in the background
     */
    fun transcribeAudio(
        audioUri: Uri,
        language: String,
        aiModel: String? = null,
        taskId: String = UUID.randomUUID().toString()
    ): String {
        Log.d(TAG, "Scheduling transcribe task: $taskId")
        
        // Create input data
        val inputData = Data.Builder()
            .putString(AiProcessingWorker.KEY_TASK_TYPE, AiProcessingWorker.TASK_TRANSCRIBE)
            .putString(AiProcessingWorker.KEY_URI, audioUri.toString())
            .putString(AiProcessingWorker.KEY_LANGUAGE, language)
            .apply {
                aiModel?.let { putString(AiProcessingWorker.KEY_AI_MODEL, it) }
            }
            .build()
        
        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<AiProcessingWorker>()
            .setInputData(inputData)
            .setConstraints(createNetworkConstraints())
            .build()
        
        // Enqueue work
        workManager.enqueueUniqueWork(
            "$WORK_TRANSCRIBE_PREFIX$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.PROCESSING)
        
        // Observe work
        observeWork(workRequest.id, taskId)
        
        return taskId
    }
    
    /**
     * Extract text from image in the background
     */
    fun extractTextFromImage(
        imageUri: Uri,
        language: String,
        aiModel: String? = null,
        taskId: String = UUID.randomUUID().toString()
    ): String {
        Log.d(TAG, "Scheduling extract text task: $taskId")
        
        // Create input data
        val inputData = Data.Builder()
            .putString(AiProcessingWorker.KEY_TASK_TYPE, AiProcessingWorker.TASK_EXTRACT_TEXT)
            .putString(AiProcessingWorker.KEY_URI, imageUri.toString())
            .putString(AiProcessingWorker.KEY_LANGUAGE, language)
            .apply {
                aiModel?.let { putString(AiProcessingWorker.KEY_AI_MODEL, it) }
            }
            .build()
        
        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<AiProcessingWorker>()
            .setInputData(inputData)
            .setConstraints(createNetworkConstraints())
            .build()
        
        // Enqueue work
        workManager.enqueueUniqueWork(
            "$WORK_EXTRACT_TEXT_PREFIX$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.PROCESSING)
        
        // Observe work
        observeWork(workRequest.id, taskId)
        
        return taskId
    }
    
    /**
     * Generate tags in the background
     */
    fun generateTags(
        content: String,
        language: String,
        maxTags: Int = 5,
        aiModel: String? = null,
        taskId: String = UUID.randomUUID().toString()
    ): String {
        Log.d(TAG, "Scheduling generate tags task: $taskId")
        
        // Create input data
        val inputData = Data.Builder()
            .putString(AiProcessingWorker.KEY_TASK_TYPE, AiProcessingWorker.TASK_GENERATE_TAGS)
            .putString(AiProcessingWorker.KEY_CONTENT, content)
            .putString(AiProcessingWorker.KEY_LANGUAGE, language)
            .putInt(AiProcessingWorker.KEY_MAX_TAGS, maxTags)
            .apply {
                aiModel?.let { putString(AiProcessingWorker.KEY_AI_MODEL, it) }
            }
            .build()
        
        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<AiProcessingWorker>()
            .setInputData(inputData)
            .setConstraints(createNetworkConstraints())
            .build()
        
        // Enqueue work
        workManager.enqueueUniqueWork(
            "$WORK_GENERATE_TAGS_PREFIX$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.PROCESSING)
        
        // Observe work
        observeWork(workRequest.id, taskId)
        
        return taskId
    }
    
    /**
     * Generate title in the background
     */
    fun generateTitle(
        content: String,
        language: String,
        aiModel: String? = null,
        taskId: String = UUID.randomUUID().toString()
    ): String {
        Log.d(TAG, "Scheduling generate title task: $taskId")
        
        // Create input data
        val inputData = Data.Builder()
            .putString(AiProcessingWorker.KEY_TASK_TYPE, AiProcessingWorker.TASK_GENERATE_TITLE)
            .putString(AiProcessingWorker.KEY_CONTENT, content)
            .putString(AiProcessingWorker.KEY_LANGUAGE, language)
            .apply {
                aiModel?.let { putString(AiProcessingWorker.KEY_AI_MODEL, it) }
            }
            .build()
        
        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<AiProcessingWorker>()
            .setInputData(inputData)
            .setConstraints(createNetworkConstraints())
            .build()
        
        // Enqueue work
        workManager.enqueueUniqueWork(
            "$WORK_GENERATE_TITLE_PREFIX$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.PROCESSING)
        
        // Observe work
        observeWork(workRequest.id, taskId)
        
        return taskId
    }
    
    /**
     * Get the result of a task
     */
    fun getTaskResult(taskId: String): ProcessingState? {
        return _processingState.value[taskId]
    }
    
    /**
     * Cancel a task
     */
    fun cancelTask(taskId: String) {
        Log.d(TAG, "Cancelling task: $taskId")
        
        // Cancel work
        workManager.cancelUniqueWork("$WORK_SUMMARIZE_PREFIX$taskId")
        workManager.cancelUniqueWork("$WORK_TRANSCRIBE_PREFIX$taskId")
        workManager.cancelUniqueWork("$WORK_EXTRACT_TEXT_PREFIX$taskId")
        workManager.cancelUniqueWork("$WORK_GENERATE_TAGS_PREFIX$taskId")
        workManager.cancelUniqueWork("$WORK_GENERATE_TITLE_PREFIX$taskId")
        
        // Update processing state
        updateProcessingState(taskId, ProcessingState.CANCELLED)
    }
    
    /**
     * Create network constraints for work requests
     */
    private fun createNetworkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
    
    /**
     * Observe work and update processing state
     */
    private fun observeWork(workId: UUID, taskId: String) {
        val workInfoLiveData: LiveData<WorkInfo> = workManager.getWorkInfoByIdLiveData(workId)
        
        workInfoLiveData.observeForever { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val result = workInfo.outputData.getString(AiProcessingWorker.KEY_RESULT)
                    updateProcessingState(taskId, ProcessingState.Completed(result ?: ""))
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString(AiProcessingWorker.KEY_ERROR)
                    updateProcessingState(taskId, ProcessingState.Error(error ?: "Unknown error"))
                }
                WorkInfo.State.CANCELLED -> {
                    updateProcessingState(taskId, ProcessingState.CANCELLED)
                }
                else -> {
                    // Do nothing for other states
                }
            }
        }
    }
    
    /**
     * Update processing state
     */
    private fun updateProcessingState(taskId: String, state: ProcessingState) {
        val currentState = _processingState.value.toMutableMap()
        currentState[taskId] = state
        _processingState.value = currentState
    }
}

/**
 * Processing state for AI tasks
 */
sealed class ProcessingState {
    object PROCESSING : ProcessingState()
    object CANCELLED : ProcessingState()
    data class Completed(val result: String) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
