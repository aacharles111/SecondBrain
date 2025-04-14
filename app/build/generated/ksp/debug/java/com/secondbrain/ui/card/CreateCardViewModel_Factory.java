package com.secondbrain.ui.card;

import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.AiService;
import com.secondbrain.data.service.ThumbnailService;
import com.secondbrain.data.service.WebSearchService;
import com.secondbrain.data.service.ai.AiServiceManager;
import com.secondbrain.util.ContentExtractor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CreateCardViewModel_Factory implements Factory<CreateCardViewModel> {
  private final Provider<CardRepository> cardRepositoryProvider;

  private final Provider<AiService> aiServiceProvider;

  private final Provider<AiServiceManager> aiServiceManagerProvider;

  private final Provider<ContentExtractor> contentExtractorProvider;

  private final Provider<WebSearchService> webSearchServiceProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<ThumbnailService> thumbnailServiceProvider;

  public CreateCardViewModel_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<AiService> aiServiceProvider, Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<ContentExtractor> contentExtractorProvider,
      Provider<WebSearchService> webSearchServiceProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ThumbnailService> thumbnailServiceProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.aiServiceProvider = aiServiceProvider;
    this.aiServiceManagerProvider = aiServiceManagerProvider;
    this.contentExtractorProvider = contentExtractorProvider;
    this.webSearchServiceProvider = webSearchServiceProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.thumbnailServiceProvider = thumbnailServiceProvider;
  }

  @Override
  public CreateCardViewModel get() {
    return newInstance(cardRepositoryProvider.get(), aiServiceProvider.get(), aiServiceManagerProvider.get(), contentExtractorProvider.get(), webSearchServiceProvider.get(), settingsRepositoryProvider.get(), thumbnailServiceProvider.get());
  }

  public static CreateCardViewModel_Factory create(Provider<CardRepository> cardRepositoryProvider,
      Provider<AiService> aiServiceProvider, Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<ContentExtractor> contentExtractorProvider,
      Provider<WebSearchService> webSearchServiceProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ThumbnailService> thumbnailServiceProvider) {
    return new CreateCardViewModel_Factory(cardRepositoryProvider, aiServiceProvider, aiServiceManagerProvider, contentExtractorProvider, webSearchServiceProvider, settingsRepositoryProvider, thumbnailServiceProvider);
  }

  public static CreateCardViewModel newInstance(CardRepository cardRepository, AiService aiService,
      AiServiceManager aiServiceManager, ContentExtractor contentExtractor,
      WebSearchService webSearchService, SettingsRepository settingsRepository,
      ThumbnailService thumbnailService) {
    return new CreateCardViewModel(cardRepository, aiService, aiServiceManager, contentExtractor, webSearchService, settingsRepository, thumbnailService);
  }
}
