package com.secondbrain.data.service;

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
public final class WebSearchService_Factory implements Factory<WebSearchService> {
  @Override
  public WebSearchService get() {
    return newInstance();
  }

  public static WebSearchService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WebSearchService newInstance() {
    return new WebSearchService();
  }

  private static final class InstanceHolder {
    private static final WebSearchService_Factory INSTANCE = new WebSearchService_Factory();
  }
}
