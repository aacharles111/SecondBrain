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
public final class YouTubePromptGenerator_Factory implements Factory<YouTubePromptGenerator> {
  @Override
  public YouTubePromptGenerator get() {
    return newInstance();
  }

  public static YouTubePromptGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static YouTubePromptGenerator newInstance() {
    return new YouTubePromptGenerator();
  }

  private static final class InstanceHolder {
    private static final YouTubePromptGenerator_Factory INSTANCE = new YouTubePromptGenerator_Factory();
  }
}
