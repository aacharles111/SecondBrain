package com.secondbrain.ui.settings;

import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.ai.FreeFirstModelSelector;
import com.secondbrain.data.service.ai.OpenRouterModelRepository;
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
public final class CostAwareModelSelectorViewModel_Factory implements Factory<CostAwareModelSelectorViewModel> {
  private final Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider;

  private final Provider<FreeFirstModelSelector> freeFirstModelSelectorProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public CostAwareModelSelectorViewModel_Factory(
      Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider,
      Provider<FreeFirstModelSelector> freeFirstModelSelectorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.openRouterModelRepositoryProvider = openRouterModelRepositoryProvider;
    this.freeFirstModelSelectorProvider = freeFirstModelSelectorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public CostAwareModelSelectorViewModel get() {
    return newInstance(openRouterModelRepositoryProvider.get(), freeFirstModelSelectorProvider.get(), settingsRepositoryProvider.get());
  }

  public static CostAwareModelSelectorViewModel_Factory create(
      Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider,
      Provider<FreeFirstModelSelector> freeFirstModelSelectorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new CostAwareModelSelectorViewModel_Factory(openRouterModelRepositoryProvider, freeFirstModelSelectorProvider, settingsRepositoryProvider);
  }

  public static CostAwareModelSelectorViewModel newInstance(
      OpenRouterModelRepository openRouterModelRepository,
      FreeFirstModelSelector freeFirstModelSelector, SettingsRepository settingsRepository) {
    return new CostAwareModelSelectorViewModel(openRouterModelRepository, freeFirstModelSelector, settingsRepository);
  }
}
