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
public final class ClaudeProvider_Factory implements Factory<ClaudeProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public ClaudeProvider_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public ClaudeProvider get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get());
  }

  public static ClaudeProvider_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new ClaudeProvider_Factory(contextProvider, settingsRepositoryProvider);
  }

  public static ClaudeProvider newInstance(Context context, SettingsRepository settingsRepository) {
    return new ClaudeProvider(context, settingsRepository);
  }
}
