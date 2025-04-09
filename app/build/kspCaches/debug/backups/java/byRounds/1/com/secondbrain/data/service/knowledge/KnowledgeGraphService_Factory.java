package com.secondbrain.data.service.knowledge;

import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.service.ai.AiServiceManager;
import com.secondbrain.data.service.ai.content.EntityExtractor;
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
public final class KnowledgeGraphService_Factory implements Factory<KnowledgeGraphService> {
  private final Provider<CardRepository> cardRepositoryProvider;

  private final Provider<EntityExtractor> entityExtractorProvider;

  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public KnowledgeGraphService_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<EntityExtractor> entityExtractorProvider,
      Provider<AiServiceManager> aiServiceManagerProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.entityExtractorProvider = entityExtractorProvider;
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public KnowledgeGraphService get() {
    return newInstance(cardRepositoryProvider.get(), entityExtractorProvider.get(), aiServiceManagerProvider.get());
  }

  public static KnowledgeGraphService_Factory create(
      Provider<CardRepository> cardRepositoryProvider,
      Provider<EntityExtractor> entityExtractorProvider,
      Provider<AiServiceManager> aiServiceManagerProvider) {
    return new KnowledgeGraphService_Factory(cardRepositoryProvider, entityExtractorProvider, aiServiceManagerProvider);
  }

  public static KnowledgeGraphService newInstance(CardRepository cardRepository,
      EntityExtractor entityExtractor, AiServiceManager aiServiceManager) {
    return new KnowledgeGraphService(cardRepository, entityExtractor, aiServiceManager);
  }
}
