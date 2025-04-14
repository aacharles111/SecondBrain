package com.secondbrain.data.service.ai;

import android.content.Context;
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
public final class DefaultAiProvider_Factory implements Factory<DefaultAiProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<OpenAiProvider> openAiProvider;

  private final Provider<GeminiProvider> geminiProvider;

  private final Provider<ClaudeProvider> claudeProvider;

  private final Provider<DeepSeekProvider> deepSeekProvider;

  private final Provider<OpenRouterProvider> openRouterProvider;

  public DefaultAiProvider_Factory(Provider<Context> contextProvider,
      Provider<OpenAiProvider> openAiProvider, Provider<GeminiProvider> geminiProvider,
      Provider<ClaudeProvider> claudeProvider, Provider<DeepSeekProvider> deepSeekProvider,
      Provider<OpenRouterProvider> openRouterProvider) {
    this.contextProvider = contextProvider;
    this.openAiProvider = openAiProvider;
    this.geminiProvider = geminiProvider;
    this.claudeProvider = claudeProvider;
    this.deepSeekProvider = deepSeekProvider;
    this.openRouterProvider = openRouterProvider;
  }

  @Override
  public DefaultAiProvider get() {
    return newInstance(contextProvider.get(), openAiProvider.get(), geminiProvider.get(), claudeProvider.get(), deepSeekProvider.get(), openRouterProvider.get());
  }

  public static DefaultAiProvider_Factory create(Provider<Context> contextProvider,
      Provider<OpenAiProvider> openAiProvider, Provider<GeminiProvider> geminiProvider,
      Provider<ClaudeProvider> claudeProvider, Provider<DeepSeekProvider> deepSeekProvider,
      Provider<OpenRouterProvider> openRouterProvider) {
    return new DefaultAiProvider_Factory(contextProvider, openAiProvider, geminiProvider, claudeProvider, deepSeekProvider, openRouterProvider);
  }

  public static DefaultAiProvider newInstance(Context context, OpenAiProvider openAiProvider,
      GeminiProvider geminiProvider, ClaudeProvider claudeProvider,
      DeepSeekProvider deepSeekProvider, OpenRouterProvider openRouterProvider) {
    return new DefaultAiProvider(context, openAiProvider, geminiProvider, claudeProvider, deepSeekProvider, openRouterProvider);
  }
}
