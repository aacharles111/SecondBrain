package com.secondbrain.data.repository;

import com.secondbrain.data.db.CardDao;
import com.secondbrain.data.db.NoteDao;
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
public final class SearchRepository_Factory implements Factory<SearchRepository> {
  private final Provider<CardDao> cardDaoProvider;

  private final Provider<NoteDao> noteDaoProvider;

  public SearchRepository_Factory(Provider<CardDao> cardDaoProvider,
      Provider<NoteDao> noteDaoProvider) {
    this.cardDaoProvider = cardDaoProvider;
    this.noteDaoProvider = noteDaoProvider;
  }

  @Override
  public SearchRepository get() {
    return newInstance(cardDaoProvider.get(), noteDaoProvider.get());
  }

  public static SearchRepository_Factory create(Provider<CardDao> cardDaoProvider,
      Provider<NoteDao> noteDaoProvider) {
    return new SearchRepository_Factory(cardDaoProvider, noteDaoProvider);
  }

  public static SearchRepository newInstance(CardDao cardDao, NoteDao noteDao) {
    return new SearchRepository(cardDao, noteDao);
  }
}
