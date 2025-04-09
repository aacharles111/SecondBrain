package com.secondbrain.data.service.ai;

import android.content.Context;
import com.secondbrain.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AiServiceManager_Factory implements Factory<AiServiceManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<GeminiProvider> geminiProvider;

  private final Provider<OpenAiProvider> openAiProvider;

  private final Provider<ClaudeProvider> claudeProvider;

  private final Provider<DeepSeekProvider> deepSeekProvider;

  private final Provider<OpenRouterProvider> openRouterProvider;

  public AiServiceManager_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GeminiProvider> geminiProvider, Provider<OpenAiProvider> openAiProvider,
      Provider<ClaudeProvider> claudeProvider, Provider<DeepSeekProvider> deepSeekProvider,
      Provider<OpenRouterProvider> openRouterProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.geminiProvider = geminiProvider;
    this.openAiProvider = openAiProvider;
    this.claudeProvider = claudeProvider;
    this.deepSeekProvider = deepSeekProvider;
    this.openRouterProvider = openRouterProvider;
  }

  @Override
  public AiServiceManager get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get(), geminiProvider.get(), openAiProvider.get(), claudeProvider.get(), deepSeekProvider.get(), openRouterProvider.get());
  }

  public static AiServiceManager_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GeminiProvider> geminiProvider, Provider<OpenAiProvider> openAiProvider,
      Provider<ClaudeProvider> claudeProvider, Provider<DeepSeekProvider> deepSeekProvider,
      Provider<OpenRouterProvider> openRouterProvider) {
    return new AiServiceManager_Factory(contextProvider, settingsRepositoryProvider, geminiProvider, openAiProvider, claudeProvider, deepSeekProvider, openRouterProvider);
  }

  public static AiServiceManager newInstance(Context context, SettingsRepository settingsRepository,
      GeminiProvider geminiProvider, OpenAiProvider openAiProvider, ClaudeProvider claudeProvider,
      DeepSeekProvider deepSeekProvider, OpenRouterProvider openRouterProvider) {
    return new AiServiceManager(context, settingsRepository, geminiProvider, openAiProvider, claudeProvider, deepSeekProvider, openRouterProvider);
  }
}
