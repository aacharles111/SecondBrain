package com.secondbrain.util;

import android.content.Context;
import com.secondbrain.data.service.youtube.YouTubeContentProcessor;
import com.secondbrain.data.service.youtube.YouTubeService;
import com.secondbrain.data.service.youtube.YouTubeTranscriptScraper;
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
public final class ContentExtractor_Factory implements Factory<ContentExtractor> {
  private final Provider<Context> contextProvider;

  private final Provider<YouTubeService> youTubeServiceProvider;

  private final Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider;

  private final Provider<YouTubeContentProcessor> youTubeContentProcessorProvider;

  public ContentExtractor_Factory(Provider<Context> contextProvider,
      Provider<YouTubeService> youTubeServiceProvider,
      Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider,
      Provider<YouTubeContentProcessor> youTubeContentProcessorProvider) {
    this.contextProvider = contextProvider;
    this.youTubeServiceProvider = youTubeServiceProvider;
    this.youTubeTranscriptScraperProvider = youTubeTranscriptScraperProvider;
    this.youTubeContentProcessorProvider = youTubeContentProcessorProvider;
  }

  @Override
  public ContentExtractor get() {
    return newInstance(contextProvider.get(), youTubeServiceProvider.get(), youTubeTranscriptScraperProvider.get(), youTubeContentProcessorProvider.get());
  }

  public static ContentExtractor_Factory create(Provider<Context> contextProvider,
      Provider<YouTubeService> youTubeServiceProvider,
      Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider,
      Provider<YouTubeContentProcessor> youTubeContentProcessorProvider) {
    return new ContentExtractor_Factory(contextProvider, youTubeServiceProvider, youTubeTranscriptScraperProvider, youTubeContentProcessorProvider);
  }

  public static ContentExtractor newInstance(Context context, YouTubeService youTubeService,
      YouTubeTranscriptScraper youTubeTranscriptScraper,
      YouTubeContentProcessor youTubeContentProcessor) {
    return new ContentExtractor(context, youTubeService, youTubeTranscriptScraper, youTubeContentProcessor);
  }
}
