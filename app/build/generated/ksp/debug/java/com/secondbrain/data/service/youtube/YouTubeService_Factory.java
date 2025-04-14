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
public final class YouTubeService_Factory implements Factory<YouTubeService> {
  private final Provider<Context> contextProvider;

  public YouTubeService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public YouTubeService get() {
    return newInstance(contextProvider.get());
  }

  public static YouTubeService_Factory create(Provider<Context> contextProvider) {
    return new YouTubeService_Factory(contextProvider);
  }

  public static YouTubeService newInstance(Context context) {
    return new YouTubeService(context);
  }
}
