package com.secondbrain.di;

import android.content.Context;
import com.secondbrain.data.db.NoteDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideNoteDatabaseFactory implements Factory<NoteDatabase> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideNoteDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NoteDatabase get() {
    return provideNoteDatabase(contextProvider.get());
  }

  public static AppModule_ProvideNoteDatabaseFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideNoteDatabaseFactory(contextProvider);
  }

  public static NoteDatabase provideNoteDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNoteDatabase(context));
  }
}
