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
public final class ContentAwareSummarizer_Factory implements Factory<ContentAwareSummarizer> {
  private final Provider<ContentTypeDetector> contentTypeDetectorProvider;

  private final Provider<SpecializedPromptGenerator> promptGeneratorProvider;

  private final Provider<AiServiceManager> aiServiceManagerProvider;

  private final Provider<EntityExtractor> entityExtractorProvider;

  public ContentAwareSummarizer_Factory(Provider<ContentTypeDetector> contentTypeDetectorProvider,
      Provider<SpecializedPromptGenerator> promptGeneratorProvider,
      Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<EntityExtractor> entityExtractorProvider) {
    this.contentTypeDetectorProvider = contentTypeDetectorProvider;
    this.promptGeneratorProvider = promptGeneratorProvider;
    this.aiServiceManagerProvider = aiServiceManagerProvider;
    this.entityExtractorProvider = entityExtractorProvider;
  }

  @Override
  public ContentAwareSummarizer get() {
    return newInstance(contentTypeDetectorProvider.get(), promptGeneratorProvider.get(), aiServiceManagerProvider.get(), entityExtractorProvider.get());
  }

  public static ContentAwareSummarizer_Factory create(
      Provider<ContentTypeDetector> contentTypeDetectorProvider,
      Provider<SpecializedPromptGenerator> promptGeneratorProvider,
      Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<EntityExtractor> entityExtractorProvider) {
    return new ContentAwareSummarizer_Factory(contentTypeDetectorProvider, promptGeneratorProvider, aiServiceManagerProvider, entityExtractorProvider);
  }

  public static ContentAwareSummarizer newInstance(ContentTypeDetector contentTypeDetector,
      SpecializedPromptGenerator promptGenerator, AiServiceManager aiServiceManager,
      EntityExtractor entityExtractor) {
    return new ContentAwareSummarizer(contentTypeDetector, promptGenerator, aiServiceManager, entityExtractor);
  }
}
