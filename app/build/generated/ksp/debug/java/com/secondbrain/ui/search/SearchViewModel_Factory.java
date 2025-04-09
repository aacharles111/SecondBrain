package com.secondbrain.ui.search;

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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<NoteRepository> noteRepositoryProvider;

  public SearchViewModel_Factory(Provider<NoteRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<NoteRepository> noteRepositoryProvider) {
    return new SearchViewModel_Factory(noteRepositoryProvider);
  }

  public static SearchViewModel newInstance(NoteRepository noteRepository) {
    return new SearchViewModel(noteRepository);
  }
}
