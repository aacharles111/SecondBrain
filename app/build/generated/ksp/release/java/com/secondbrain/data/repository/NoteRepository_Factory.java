package com.secondbrain.data.repository;

import android.content.Context;
import com.secondbrain.data.db.NoteDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NoteRepository_Factory implements Factory<NoteRepository> {
  private final Provider<NoteDao> noteDaoProvider;

  private final Provider<Context> contextProvider;

  public NoteRepository_Factory(Provider<NoteDao> noteDaoProvider,
      Provider<Context> contextProvider) {
    this.noteDaoProvider = noteDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public NoteRepository get() {
    return newInstance(noteDaoProvider.get(), contextProvider.get());
  }

  public static NoteRepository_Factory create(Provider<NoteDao> noteDaoProvider,
      Provider<Context> contextProvider) {
    return new NoteRepository_Factory(noteDaoProvider, contextProvider);
  }

  public static NoteRepository newInstance(NoteDao noteDao, Context context) {
    return new NoteRepository(noteDao, context);
  }
}
