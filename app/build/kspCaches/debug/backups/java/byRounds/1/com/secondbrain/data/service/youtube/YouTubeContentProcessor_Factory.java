package com.secondbrain.data.service.youtube;

import android.content.Context;
import com.secondbrain.data.service.ai.AiProvider;
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
public final class YouTubeContentProcessor_Factory implements Factory<YouTubeContentProcessor> {
  private final Provider<Context> contextProvider;

  private final Provider<YouTubeService> youTubeServiceProvider;

  private final Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider;

  private final Provider<YouTubeThumbnailService> youtubeThumbnailServiceProvider;

  private final Provider<YouTubePromptGenerator> youTubePromptGeneratorProvider;

  private final Provider<AiProvider> aiProvider;

  public YouTubeContentProcessor_Factory(Provider<Context> contextProvider,
      Provider<YouTubeService> youTubeServiceProvider,
      Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider,
      Provider<YouTubeThumbnailService> youtubeThumbnailServiceProvider,
      Provider<YouTubePromptGenerator> youTubePromptGeneratorProvider,
      Provider<AiProvider> aiProvider) {
    this.contextProvider = contextProvider;
    this.youTubeServiceProvider = youTubeServiceProvider;
    this.youTubeTranscriptScraperProvider = youTubeTranscriptScraperProvider;
    this.youtubeThumbnailServiceProvider = youtubeThumbnailServiceProvider;
    this.youTubePromptGeneratorProvider = youTubePromptGeneratorProvider;
    this.aiProvider = aiProvider;
  }

  @Override
  public YouTubeContentProcessor get() {
    return newInstance(contextProvider.get(), youTubeServiceProvider.get(), youTubeTranscriptScraperProvider.get(), youtubeThumbnailServiceProvider.get(), youTubePromptGeneratorProvider.get(), aiProvider.get());
  }

  public static YouTubeContentProcessor_Factory create(Provider<Context> contextProvider,
      Provider<YouTubeService> youTubeServiceProvider,
      Provider<YouTubeTranscriptScraper> youTubeTranscriptScraperProvider,
      Provider<YouTubeThumbnailService> youtubeThumbnailServiceProvider,
      Provider<YouTubePromptGenerator> youTubePromptGeneratorProvider,
      Provider<AiProvider> aiProvider) {
    return new YouTubeContentProcessor_Factory(contextProvider, youTubeServiceProvider, youTubeTranscriptScraperProvider, youtubeThumbnailServiceProvider, youTubePromptGeneratorProvider, aiProvider);
  }

  public static YouTubeContentProcessor newInstance(Context context, YouTubeService youTubeService,
      YouTubeTranscriptScraper youTubeTranscriptScraper,
      YouTubeThumbnailService youtubeThumbnailService,
      YouTubePromptGenerator youTubePromptGenerator, AiProvider aiProvider) {
    return new YouTubeContentProcessor(context, youTubeService, youTubeTranscriptScraper, youtubeThumbnailService, youTubePromptGenerator, aiProvider);
  }
}
