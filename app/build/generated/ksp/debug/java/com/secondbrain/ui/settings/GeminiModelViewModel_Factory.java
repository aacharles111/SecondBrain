package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.GeminiProvider;
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
public final class GeminiModelViewModel_Factory implements Factory<GeminiModelViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<GeminiProvider> geminiProvider;

  public GeminiModelViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GeminiProvider> geminiProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.geminiProvider = geminiProvider;
  }

  @Override
  public GeminiModelViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), geminiProvider.get());
  }

  public static GeminiModelViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GeminiProvider> geminiProvider) {
    return new GeminiModelViewModel_Factory(settingsRepositoryProvider, geminiProvider);
  }

  public static GeminiModelViewModel newInstance(SettingsRepository settingsRepository,
      GeminiProvider geminiProvider) {
    return new GeminiModelViewModel(settingsRepository, geminiProvider);
  }
}
