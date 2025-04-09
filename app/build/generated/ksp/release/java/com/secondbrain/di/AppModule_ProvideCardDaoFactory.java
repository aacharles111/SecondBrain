package com.secondbrain.di;

import com.secondbrain.data.db.CardDao;
import com.secondbrain.data.db.NoteDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideCardDaoFactory implements Factory<CardDao> {
  private final Provider<NoteDatabase> databaseProvider;

  public AppModule_ProvideCardDaoFactory(Provider<NoteDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CardDao get() {
    return provideCardDao(databaseProvider.get());
  }

  public static AppModule_ProvideCardDaoFactory create(Provider<NoteDatabase> databaseProvider) {
    return new AppModule_ProvideCardDaoFactory(databaseProvider);
  }

  public static CardDao provideCardDao(NoteDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCardDao(database));
  }
}
