package com.secondbrain.data.service.youtube;

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
public final class YouTubeTranscriptScraper_Factory implements Factory<YouTubeTranscriptScraper> {
  @Override
  public YouTubeTranscriptScraper get() {
    return newInstance();
  }

  public static YouTubeTranscriptScraper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static YouTubeTranscriptScraper newInstance() {
    return new YouTubeTranscriptScraper();
  }

  private static final class InstanceHolder {
    private static final YouTubeTranscriptScraper_Factory INSTANCE = new YouTubeTranscriptScraper_Factory();
  }
}
