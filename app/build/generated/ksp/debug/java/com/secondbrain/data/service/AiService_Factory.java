package com.secondbrain.data.service;

import com.secondbrain.data.service.ai.AiServiceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AiService_Factory implements Factory<AiService> {
  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public AiService_Factory(Provider<AiServiceManager> aiServiceManagerProvider) {
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public AiService get() {
    return newInstance(aiServiceManagerProvider.get());
  }

  public static AiService_Factory create(Provider<AiServiceManager> aiServiceManagerProvider) {
    return new AiService_Factory(aiServiceManagerProvider);
  }

  public static AiService newInstance(AiServiceManager aiServiceManager) {
    return new AiService(aiServiceManager);
  }
}
