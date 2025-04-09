package com.secondbrain.di

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.worker.AiProcessingWorker
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module for providing WorkManager workers
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    /**
     * Provides a custom WorkerFactory for creating workers with Hilt dependencies
     */
    @Provides
    @Singleton
    fun provideWorkerFactory(
        aiProcessingWorkerFactory: AiProcessingWorkerFactory
    ): WorkerFactory {
        return HiltWorkerFactory(aiProcessingWorkerFactory)
    }
}

/**
 * Factory for creating AiProcessingWorker with Hilt dependencies
 */
@AssistedFactory
interface AiProcessingWorkerFactory {
    fun create(
        context: Context,
        params: WorkerParameters
    ): AiProcessingWorker
}

/**
 * Custom WorkerFactory for creating workers with Hilt dependencies
 */
class HiltWorkerFactory @Inject constructor(
    private val aiProcessingWorkerFactory: AiProcessingWorkerFactory
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): androidx.work.ListenableWorker? {
        return when (workerClassName) {
            AiProcessingWorker::class.java.name -> {
                aiProcessingWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}
