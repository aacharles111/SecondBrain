package com.secondbrain.data.repository;

import com.secondbrain.data.db.CardDao;
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
public final class CardRepository_Factory implements Factory<CardRepository> {
  private final Provider<CardDao> cardDaoProvider;

  public CardRepository_Factory(Provider<CardDao> cardDaoProvider) {
    this.cardDaoProvider = cardDaoProvider;
  }

  @Override
  public CardRepository get() {
    return newInstance(cardDaoProvider.get());
  }

  public static CardRepository_Factory create(Provider<CardDao> cardDaoProvider) {
    return new CardRepository_Factory(cardDaoProvider);
  }

  public static CardRepository newInstance(CardDao cardDao) {
    return new CardRepository(cardDao);
  }
}
