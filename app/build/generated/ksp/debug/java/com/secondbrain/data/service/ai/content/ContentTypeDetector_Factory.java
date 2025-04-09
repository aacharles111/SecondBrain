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
public final class ContentTypeDetector_Factory implements Factory<ContentTypeDetector> {
  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public ContentTypeDetector_Factory(Provider<AiServiceManager> aiServiceManagerProvider) {
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public ContentTypeDetector get() {
    return newInstance(aiServiceManagerProvider.get());
  }

  public static ContentTypeDetector_Factory create(
      Provider<AiServiceManager> aiServiceManagerProvider) {
    return new ContentTypeDetector_Factory(aiServiceManagerProvider);
  }

  public static ContentTypeDetector newInstance(AiServiceManager aiServiceManager) {
    return new ContentTypeDetector(aiServiceManager);
  }
}
