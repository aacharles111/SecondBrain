package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.ClaudeProvider;
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
public final class ClaudeModelViewModel_Factory implements Factory<ClaudeModelViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<ClaudeProvider> claudeProvider;

  public ClaudeModelViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ClaudeProvider> claudeProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.claudeProvider = claudeProvider;
  }

  @Override
  public ClaudeModelViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), claudeProvider.get());
  }

  public static ClaudeModelViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ClaudeProvider> claudeProvider) {
    return new ClaudeModelViewModel_Factory(settingsRepositoryProvider, claudeProvider);
  }

  public static ClaudeModelViewModel newInstance(SettingsRepository settingsRepository,
      ClaudeProvider claudeProvider) {
    return new ClaudeModelViewModel(settingsRepository, claudeProvider);
  }
}
