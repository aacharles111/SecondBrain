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
public final class ContentProcessor_Factory implements Factory<ContentProcessor> {
  private final Provider<OpenRouterApiClient> openRouterApiClientProvider;

  public ContentProcessor_Factory(Provider<OpenRouterApiClient> openRouterApiClientProvider) {
    this.openRouterApiClientProvider = openRouterApiClientProvider;
  }

  @Override
  public ContentProcessor get() {
    return newInstance(openRouterApiClientProvider.get());
  }

  public static ContentProcessor_Factory create(
      Provider<OpenRouterApiClient> openRouterApiClientProvider) {
    return new ContentProcessor_Factory(openRouterApiClientProvider);
  }

  public static ContentProcessor newInstance(OpenRouterApiClient openRouterApiClient) {
    return new ContentProcessor(openRouterApiClient);
  }
}
