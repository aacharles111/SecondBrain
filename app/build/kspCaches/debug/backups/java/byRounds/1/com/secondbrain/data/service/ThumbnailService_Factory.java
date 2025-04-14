package com.secondbrain.data.service;

import android.content.Context;
import com.secondbrain.data.service.youtube.YouTubeThumbnailService;
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
public final class ThumbnailService_Factory implements Factory<ThumbnailService> {
  private final Provider<Context> contextProvider;

  private final Provider<YouTubeThumbnailService> youTubeThumbnailServiceProvider;

  private final Provider<LinkPreviewService> linkPreviewServiceProvider;

  public ThumbnailService_Factory(Provider<Context> contextProvider,
      Provider<YouTubeThumbnailService> youTubeThumbnailServiceProvider,
      Provider<LinkPreviewService> linkPreviewServiceProvider) {
    this.contextProvider = contextProvider;
    this.youTubeThumbnailServiceProvider = youTubeThumbnailServiceProvider;
    this.linkPreviewServiceProvider = linkPreviewServiceProvider;
  }

  @Override
  public ThumbnailService get() {
    return newInstance(contextProvider.get(), youTubeThumbnailServiceProvider.get(), linkPreviewServiceProvider.get());
  }

  public static ThumbnailService_Factory create(Provider<Context> contextProvider,
      Provider<YouTubeThumbnailService> youTubeThumbnailServiceProvider,
      Provider<LinkPreviewService> linkPreviewServiceProvider) {
    return new ThumbnailService_Factory(contextProvider, youTubeThumbnailServiceProvider, linkPreviewServiceProvider);
  }

  public static ThumbnailService newInstance(Context context,
      YouTubeThumbnailService youTubeThumbnailService, LinkPreviewService linkPreviewService) {
    return new ThumbnailService(context, youTubeThumbnailService, linkPreviewService);
  }
}
