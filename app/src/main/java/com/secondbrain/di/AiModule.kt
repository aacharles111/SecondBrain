package com.secondbrain.di

import com.secondbrain.data.service.AiService
import com.secondbrain.data.service.ai.AiProvider
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.DefaultAiProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing AI-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
class AiModule {

    /**
     * Provide the legacy AiService for backward compatibility
     */
    @Provides
    @Singleton
    fun provideAiService(aiServiceManager: AiServiceManager): AiService {
        return AiService(aiServiceManager)
    }

    /**
     * Provide the AiProvider implementation
     */
    @Provides
    @Singleton
    fun provideAiProvider(defaultAiProvider: DefaultAiProvider): AiProvider {
        return defaultAiProvider
    }
}
