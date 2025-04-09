package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.AiServiceManager;
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
public final class AiSettingsViewModel_Factory implements Factory<AiSettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<AiServiceManager> aiServiceManagerProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public AiSettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.aiServiceManagerProvider = aiServiceManagerProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public AiSettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), aiServiceManagerProvider.get(), secureStorageProvider.get());
  }

  public static AiSettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AiServiceManager> aiServiceManagerProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new AiSettingsViewModel_Factory(settingsRepositoryProvider, aiServiceManagerProvider, secureStorageProvider);
  }

  public static AiSettingsViewModel newInstance(SettingsRepository settingsRepository,
      AiServiceManager aiServiceManager, SecureStorage secureStorage) {
    return new AiSettingsViewModel(settingsRepository, aiServiceManager, secureStorage);
  }
}
