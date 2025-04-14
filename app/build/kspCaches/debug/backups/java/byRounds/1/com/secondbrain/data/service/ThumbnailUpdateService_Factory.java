package com.secondbrain.data.service;

import com.secondbrain.data.repository.CardRepository;
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
public final class ThumbnailUpdateService_Factory implements Factory<ThumbnailUpdateService> {
  private final Provider<CardRepository> cardRepositoryProvider;

  private final Provider<ThumbnailService> thumbnailServiceProvider;

  public ThumbnailUpdateService_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<ThumbnailService> thumbnailServiceProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.thumbnailServiceProvider = thumbnailServiceProvider;
  }

  @Override
  public ThumbnailUpdateService get() {
    return newInstance(cardRepositoryProvider.get(), thumbnailServiceProvider.get());
  }

  public static ThumbnailUpdateService_Factory create(
      Provider<CardRepository> cardRepositoryProvider,
      Provider<ThumbnailService> thumbnailServiceProvider) {
    return new ThumbnailUpdateService_Factory(cardRepositoryProvider, thumbnailServiceProvider);
  }

  public static ThumbnailUpdateService newInstance(CardRepository cardRepository,
      ThumbnailService thumbnailService) {
    return new ThumbnailUpdateService(cardRepository, thumbnailService);
  }
}
