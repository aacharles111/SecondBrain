package com.secondbrain.ui;

import com.secondbrain.data.service.ThumbnailUpdateService;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<ThumbnailUpdateService> thumbnailUpdateServiceProvider;

  public MainActivity_MembersInjector(
      Provider<ThumbnailUpdateService> thumbnailUpdateServiceProvider) {
    this.thumbnailUpdateServiceProvider = thumbnailUpdateServiceProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<ThumbnailUpdateService> thumbnailUpdateServiceProvider) {
    return new MainActivity_MembersInjector(thumbnailUpdateServiceProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectThumbnailUpdateService(instance, thumbnailUpdateServiceProvider.get());
  }

  @InjectedFieldSignature("com.secondbrain.ui.MainActivity.thumbnailUpdateService")
  public static void injectThumbnailUpdateService(MainActivity instance,
      ThumbnailUpdateService thumbnailUpdateService) {
    instance.thumbnailUpdateService = thumbnailUpdateService;
  }
}
