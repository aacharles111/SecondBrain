package com.secondbrain.ui.notes;

import androidx.lifecycle.SavedStateHandle;
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
public final class NoteEditViewModel_Factory implements Factory<NoteEditViewModel> {
  private final Provider<NoteRepository> noteRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public NoteEditViewModel_Factory(Provider<NoteRepository> noteRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public NoteEditViewModel get() {
    return newInstance(noteRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static NoteEditViewModel_Factory create(Provider<NoteRepository> noteRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new NoteEditViewModel_Factory(noteRepositoryProvider, savedStateHandleProvider);
  }

  public static NoteEditViewModel newInstance(NoteRepository noteRepository,
      SavedStateHandle savedStateHandle) {
    return new NoteEditViewModel(noteRepository, savedStateHandle);
  }
}
