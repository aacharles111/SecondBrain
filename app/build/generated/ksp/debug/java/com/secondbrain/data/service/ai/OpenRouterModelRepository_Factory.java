package com.secondbrain.data.service.ai;

import com.secondbrain.data.service.ai.api.OpenRouterApiClient;
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
public final class OpenRouterModelRepository_Factory implements Factory<OpenRouterModelRepository> {
  private final Provider<OpenRouterApiClient> openRouterApiClientProvider;

  public OpenRouterModelRepository_Factory(
      Provider<OpenRouterApiClient> openRouterApiClientProvider) {
    this.openRouterApiClientProvider = openRouterApiClientProvider;
  }

  @Override
  public OpenRouterModelRepository get() {
    return newInstance(openRouterApiClientProvider.get());
  }

  public static OpenRouterModelRepository_Factory create(
      Provider<OpenRouterApiClient> openRouterApiClientProvider) {
    return new OpenRouterModelRepository_Factory(openRouterApiClientProvider);
  }

  public static OpenRouterModelRepository newInstance(OpenRouterApiClient openRouterApiClient) {
    return new OpenRouterModelRepository(openRouterApiClient);
  }
}
