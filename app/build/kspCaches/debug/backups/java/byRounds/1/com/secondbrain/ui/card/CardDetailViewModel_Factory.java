package com.secondbrain.ui.card;

import com.secondbrain.data.repository.CardRepository;
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
public final class CardDetailViewModel_Factory implements Factory<CardDetailViewModel> {
  private final Provider<CardRepository> cardRepositoryProvider;

  public CardDetailViewModel_Factory(Provider<CardRepository> cardRepositoryProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
  }

  @Override
  public CardDetailViewModel get() {
    return newInstance(cardRepositoryProvider.get());
  }

  public static CardDetailViewModel_Factory create(
      Provider<CardRepository> cardRepositoryProvider) {
    return new CardDetailViewModel_Factory(cardRepositoryProvider);
  }

  public static CardDetailViewModel newInstance(CardRepository cardRepository) {
    return new CardDetailViewModel(cardRepository);
  }
}
