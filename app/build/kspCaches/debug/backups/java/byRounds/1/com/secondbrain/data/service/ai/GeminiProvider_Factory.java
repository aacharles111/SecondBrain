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
public final class GeminiProvider_Factory implements Factory<GeminiProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public GeminiProvider_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public GeminiProvider get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get(), secureStorageProvider.get());
  }

  public static GeminiProvider_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new GeminiProvider_Factory(contextProvider, settingsRepositoryProvider, secureStorageProvider);
  }

  public static GeminiProvider newInstance(Context context, SettingsRepository settingsRepository,
      SecureStorage secureStorage) {
    return new GeminiProvider(context, settingsRepository, secureStorage);
  }
}
