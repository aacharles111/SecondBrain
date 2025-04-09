package com.secondbrain.ui.card;

import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.service.AiService;
import com.secondbrain.data.service.ai.AiServiceManager;
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

  public CreateCardViewModel_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<AiService> aiServiceProvider, Provider<AiServiceManager> aiServiceManagerProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.aiServiceProvider = aiServiceProvider;
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  @Override
  public CreateCardViewModel get() {
    return newInstance(cardRepositoryProvider.get(), aiServiceProvider.get(), aiServiceManagerProvider.get());
  }

  public static CreateCardViewModel_Factory create(Provider<CardRepository> cardRepositoryProvider,
      Provider<AiService> aiServiceProvider, Provider<AiServiceManager> aiServiceManagerProvider) {
    return new CreateCardViewModel_Factory(cardRepositoryProvider, aiServiceProvider, aiServiceManagerProvider);
  }

  public static CreateCardViewModel newInstance(CardRepository cardRepository, AiService aiService,
      AiServiceManager aiServiceManager) {
    return new CreateCardViewModel(cardRepository, aiService, aiServiceManager);
  }
}
