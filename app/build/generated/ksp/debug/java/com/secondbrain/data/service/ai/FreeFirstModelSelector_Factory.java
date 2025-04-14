package com.secondbrain.data.service.ai;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FreeFirstModelSelector_Factory implements Factory<FreeFirstModelSelector> {
  private final Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider;

  public FreeFirstModelSelector_Factory(
      Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider) {
    this.openRouterModelRepositoryProvider = openRouterModelRepositoryProvider;
  }

  @Override
  public FreeFirstModelSelector get() {
    return newInstance(openRouterModelRepositoryProvider.get());
  }

  public static FreeFirstModelSelector_Factory create(
      Provider<OpenRouterModelRepository> openRouterModelRepositoryProvider) {
    return new FreeFirstModelSelector_Factory(openRouterModelRepositoryProvider);
  }

  public static FreeFirstModelSelector newInstance(
      OpenRouterModelRepository openRouterModelRepository) {
    return new FreeFirstModelSelector(openRouterModelRepository);
  }
}
