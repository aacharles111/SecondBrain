package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.DeepSeekProvider;
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
public final class DeepSeekModelViewModel_Factory implements Factory<DeepSeekModelViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<DeepSeekProvider> deepSeekProvider;

  public DeepSeekModelViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<DeepSeekProvider> deepSeekProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.deepSeekProvider = deepSeekProvider;
  }

  @Override
  public DeepSeekModelViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), deepSeekProvider.get());
  }

  public static DeepSeekModelViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<DeepSeekProvider> deepSeekProvider) {
    return new DeepSeekModelViewModel_Factory(settingsRepositoryProvider, deepSeekProvider);
  }

  public static DeepSeekModelViewModel newInstance(SettingsRepository settingsRepository,
      DeepSeekProvider deepSeekProvider) {
    return new DeepSeekModelViewModel(settingsRepository, deepSeekProvider);
  }
}
