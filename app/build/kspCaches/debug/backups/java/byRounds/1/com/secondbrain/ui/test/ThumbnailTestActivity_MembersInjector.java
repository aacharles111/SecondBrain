package com.secondbrain.ui.test;

import com.secondbrain.data.service.ThumbnailService;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ThumbnailTestActivity_MembersInjector implements MembersInjector<ThumbnailTestActivity> {
  private final Provider<ThumbnailService> thumbnailServiceProvider;

  public ThumbnailTestActivity_MembersInjector(
      Provider<ThumbnailService> thumbnailServiceProvider) {
    this.thumbnailServiceProvider = thumbnailServiceProvider;
  }

  public static MembersInjector<ThumbnailTestActivity> create(
      Provider<ThumbnailService> thumbnailServiceProvider) {
    return new ThumbnailTestActivity_MembersInjector(thumbnailServiceProvider);
  }

  @Override
  public void injectMembers(ThumbnailTestActivity instance) {
    injectThumbnailService(instance, thumbnailServiceProvider.get());
  }

  @InjectedFieldSignature("com.secondbrain.ui.test.ThumbnailTestActivity.thumbnailService")
  public static void injectThumbnailService(ThumbnailTestActivity instance,
      ThumbnailService thumbnailService) {
    instance.thumbnailService = thumbnailService;
  }
}
