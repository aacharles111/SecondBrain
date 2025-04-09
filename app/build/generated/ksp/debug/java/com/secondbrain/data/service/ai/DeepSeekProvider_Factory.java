package com.secondbrain.data.service.ai;

import android.content.Context;
import com.secondbrain.data.repository.SettingsRepository;
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
public final class DeepSeekProvider_Factory implements Factory<DeepSeekProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public DeepSeekProvider_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DeepSeekProvider get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get());
  }

  public static DeepSeekProvider_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DeepSeekProvider_Factory(contextProvider, settingsRepositoryProvider);
  }

  public static DeepSeekProvider newInstance(Context context,
      SettingsRepository settingsRepository) {
    return new DeepSeekProvider(context, settingsRepository);
  }
}
