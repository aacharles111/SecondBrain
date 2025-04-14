package com.secondbrain.data.service.ai;

import android.content.Context;
import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.util.SecureStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ClaudeProvider_Factory implements Factory<ClaudeProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public ClaudeProvider_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public ClaudeProvider get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get(), secureStorageProvider.get());
  }

  public static ClaudeProvider_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new ClaudeProvider_Factory(contextProvider, settingsRepositoryProvider, secureStorageProvider);
  }

  public static ClaudeProvider newInstance(Context context, SettingsRepository settingsRepository,
      SecureStorage secureStorage) {
    return new ClaudeProvider(context, settingsRepository, secureStorage);
  }
}
