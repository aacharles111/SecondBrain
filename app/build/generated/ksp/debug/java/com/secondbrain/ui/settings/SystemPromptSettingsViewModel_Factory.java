package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SystemPromptRepository;
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
public final class SystemPromptSettingsViewModel_Factory implements Factory<SystemPromptSettingsViewModel> {
  private final Provider<SystemPromptRepository> systemPromptRepositoryProvider;

  public SystemPromptSettingsViewModel_Factory(
      Provider<SystemPromptRepository> systemPromptRepositoryProvider) {
    this.systemPromptRepositoryProvider = systemPromptRepositoryProvider;
  }

  @Override
  public SystemPromptSettingsViewModel get() {
    return newInstance(systemPromptRepositoryProvider.get());
  }

  public static SystemPromptSettingsViewModel_Factory create(
      Provider<SystemPromptRepository> systemPromptRepositoryProvider) {
    return new SystemPromptSettingsViewModel_Factory(systemPromptRepositoryProvider);
  }

  public static SystemPromptSettingsViewModel newInstance(
      SystemPromptRepository systemPromptRepository) {
    return new SystemPromptSettingsViewModel(systemPromptRepository);
  }
}
