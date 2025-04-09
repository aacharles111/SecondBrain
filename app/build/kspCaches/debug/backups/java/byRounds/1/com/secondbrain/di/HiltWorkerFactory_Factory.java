package com.secondbrain.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class HiltWorkerFactory_Factory implements Factory<HiltWorkerFactory> {
  private final Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider;

  public HiltWorkerFactory_Factory(
      Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider) {
    this.aiProcessingWorkerFactoryProvider = aiProcessingWorkerFactoryProvider;
  }

  @Override
  public HiltWorkerFactory get() {
    return newInstance(aiProcessingWorkerFactoryProvider.get());
  }

  public static HiltWorkerFactory_Factory create(
      Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider) {
    return new HiltWorkerFactory_Factory(aiProcessingWorkerFactoryProvider);
  }

  public static HiltWorkerFactory newInstance(AiProcessingWorkerFactory aiProcessingWorkerFactory) {
    return new HiltWorkerFactory(aiProcessingWorkerFactory);
  }
}
