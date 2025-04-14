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
public final class CardEditViewModel_Factory implements Factory<CardEditViewModel> {
  private final Provider<CardRepository> cardRepositoryProvider;

  public CardEditViewModel_Factory(Provider<CardRepository> cardRepositoryProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
  }

  @Override
  public CardEditViewModel get() {
    return newInstance(cardRepositoryProvider.get());
  }

  public static CardEditViewModel_Factory create(Provider<CardRepository> cardRepositoryProvider) {
    return new CardEditViewModel_Factory(cardRepositoryProvider);
  }

  public static CardEditViewModel newInstance(CardRepository cardRepository) {
    return new CardEditViewModel(cardRepository);
  }
}
