package com.secondbrain.data.service.youtube;

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
public final class YouTubeThumbnailService_Factory implements Factory<YouTubeThumbnailService> {
  private final Provider<Context> contextProvider;

  public YouTubeThumbnailService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public YouTubeThumbnailService get() {
    return newInstance(contextProvider.get());
  }

  public static YouTubeThumbnailService_Factory create(Provider<Context> contextProvider) {
    return new YouTubeThumbnailService_Factory(contextProvider);
  }

  public static YouTubeThumbnailService newInstance(Context context) {
    return new YouTubeThumbnailService(context);
  }
}
