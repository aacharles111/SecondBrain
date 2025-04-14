package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.OpenAiProvider;
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
public final class OpenAiModelViewModel_Factory implements Factory<OpenAiModelViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<OpenAiProvider> openAiProvider;

  public OpenAiModelViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<OpenAiProvider> openAiProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.openAiProvider = openAiProvider;
  }

  @Override
  public OpenAiModelViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), openAiProvider.get());
  }

  public static OpenAiModelViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<OpenAiProvider> openAiProvider) {
    return new OpenAiModelViewModel_Factory(settingsRepositoryProvider, openAiProvider);
  }

  public static OpenAiModelViewModel newInstance(SettingsRepository settingsRepository,
      OpenAiProvider openAiProvider) {
    return new OpenAiModelViewModel(settingsRepository, openAiProvider);
  }
}
