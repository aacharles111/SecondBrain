package com.secondbrain.ui.card;

import androidx.lifecycle.SavedStateHandle;
import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.service.AiService;
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
public final class SummaryReviewViewModel_Factory implements Factory<SummaryReviewViewModel> {
  private final Provider<CardRepository> cardRepositoryProvider;

  private final Provider<AiService> aiServiceProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public SummaryReviewViewModel_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<AiService> aiServiceProvider, Provider<SavedStateHandle> savedStateHandleProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.aiServiceProvider = aiServiceProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public SummaryReviewViewModel get() {
    return newInstance(cardRepositoryProvider.get(), aiServiceProvider.get(), savedStateHandleProvider.get());
  }

  public static SummaryReviewViewModel_Factory create(
      Provider<CardRepository> cardRepositoryProvider, Provider<AiService> aiServiceProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new SummaryReviewViewModel_Factory(cardRepositoryProvider, aiServiceProvider, savedStateHandleProvider);
  }

  public static SummaryReviewViewModel newInstance(CardRepository cardRepository,
      AiService aiService, SavedStateHandle savedStateHandle) {
    return new SummaryReviewViewModel(cardRepository, aiService, savedStateHandle);
  }
}
