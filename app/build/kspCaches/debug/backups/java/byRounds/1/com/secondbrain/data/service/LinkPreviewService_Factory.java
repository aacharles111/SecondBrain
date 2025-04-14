package com.secondbrain.data.service;

import android.content.Context;
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
public final class LinkPreviewService_Factory implements Factory<LinkPreviewService> {
  private final Provider<Context> contextProvider;

  public LinkPreviewService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LinkPreviewService get() {
    return newInstance(contextProvider.get());
  }

  public static LinkPreviewService_Factory create(Provider<Context> contextProvider) {
    return new LinkPreviewService_Factory(contextProvider);
  }

  public static LinkPreviewService newInstance(Context context) {
    return new LinkPreviewService(context);
  }
}
