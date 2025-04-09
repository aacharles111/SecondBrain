package com.secondbrain.data.service.ai.content;

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
public final class EntityExtractor_Factory implements Factory<EntityExtractor> {
  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public EntityExtractor_Factory(Provider<AiServiceManager> aiServiceManagerProvider) {
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public EntityExtractor get() {
    return newInstance(aiServiceManagerProvider.get());
  }

  public static EntityExtractor_Factory create(
      Provider<AiServiceManager> aiServiceManagerProvider) {
    return new EntityExtractor_Factory(aiServiceManagerProvider);
  }

  public static EntityExtractor newInstance(AiServiceManager aiServiceManager) {
    return new EntityExtractor(aiServiceManager);
  }
}
