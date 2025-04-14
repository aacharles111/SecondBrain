package com.secondbrain.data.service.ai.api;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class OpenRouterApiClient_Factory implements Factory<OpenRouterApiClient> {
  @Override
  public OpenRouterApiClient get() {
    return newInstance();
  }

  public static OpenRouterApiClient_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OpenRouterApiClient newInstance() {
    return new OpenRouterApiClient();
  }

  private static final class InstanceHolder {
    private static final OpenRouterApiClient_Factory INSTANCE = new OpenRouterApiClient_Factory();
  }
}
