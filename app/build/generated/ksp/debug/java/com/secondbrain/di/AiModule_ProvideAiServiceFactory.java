package com.secondbrain.di;

import com.secondbrain.data.service.AiService;
import com.secondbrain.data.service.ai.AiServiceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class AiModule_ProvideAiServiceFactory implements Factory<AiService> {
  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public AiModule_ProvideAiServiceFactory(Provider<AiServiceManager> aiServiceManagerProvider) {
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public AiService get() {
    return provideAiService(aiServiceManagerProvider.get());
  }

  public static AiModule_ProvideAiServiceFactory create(
      Provider<AiServiceManager> aiServiceManagerProvider) {
    return new AiModule_ProvideAiServiceFactory(aiServiceManagerProvider);
  }

  public static AiService provideAiService(AiServiceManager aiServiceManager) {
    return Preconditions.checkNotNullFromProvides(AiModule.INSTANCE.provideAiService(aiServiceManager));
  }
}
