package com.secondbrain.ui.notes;

import com.secondbrain.data.repository.NoteRepository;
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
public final class NoteListViewModel_Factory implements Factory<NoteListViewModel> {
  private final Provider<NoteRepository> noteRepositoryProvider;

  public NoteListViewModel_Factory(Provider<NoteRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public NoteListViewModel get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static NoteListViewModel_Factory create(Provider<NoteRepository> noteRepositoryProvider) {
    return new NoteListViewModel_Factory(noteRepositoryProvider);
  }

  public static NoteListViewModel newInstance(NoteRepository noteRepository) {
    return new NoteListViewModel(noteRepository);
  }
}
