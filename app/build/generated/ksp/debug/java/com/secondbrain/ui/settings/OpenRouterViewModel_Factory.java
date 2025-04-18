package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.OpenRouterProvider;
import com.secondbrain.util.SecureStorage;
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
public final class OpenRouterViewModel_Factory implements Factory<OpenRouterViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<OpenRouterProvider> openRouterProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public OpenRouterViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<OpenRouterProvider> openRouterProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.openRouterProvider = openRouterProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public OpenRouterViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), openRouterProvider.get(), secureStorageProvider.get());
  }

  public static OpenRouterViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<OpenRouterProvider> openRouterProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new OpenRouterViewModel_Factory(settingsRepositoryProvider, openRouterProvider, secureStorageProvider);
  }

  public static OpenRouterViewModel newInstance(SettingsRepository settingsRepository,
      OpenRouterProvider openRouterProvider, SecureStorage secureStorage) {
    return new OpenRouterViewModel(settingsRepository, openRouterProvider, secureStorage);
  }
}
