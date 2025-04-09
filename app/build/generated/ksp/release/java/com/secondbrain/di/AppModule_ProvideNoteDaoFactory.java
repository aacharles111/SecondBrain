package com.secondbrain.di;

import com.secondbrain.data.db.NoteDao;
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
public final class AppModule_ProvideNoteDaoFactory implements Factory<NoteDao> {
  private final Provider<NoteDatabase> databaseProvider;

  public AppModule_ProvideNoteDaoFactory(Provider<NoteDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public NoteDao get() {
    return provideNoteDao(databaseProvider.get());
  }

  public static AppModule_ProvideNoteDaoFactory create(Provider<NoteDatabase> databaseProvider) {
    return new AppModule_ProvideNoteDaoFactory(databaseProvider);
  }

  public static NoteDao provideNoteDao(NoteDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNoteDao(database));
  }
}
