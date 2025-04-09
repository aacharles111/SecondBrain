package com.secondbrain.di;

import androidx.work.WorkerFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class WorkerModule_ProvideWorkerFactoryFactory implements Factory<WorkerFactory> {
  private final Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider;

  public WorkerModule_ProvideWorkerFactoryFactory(
      Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider) {
    this.aiProcessingWorkerFactoryProvider = aiProcessingWorkerFactoryProvider;
  }

  @Override
  public WorkerFactory get() {
    return provideWorkerFactory(aiProcessingWorkerFactoryProvider.get());
  }

  public static WorkerModule_ProvideWorkerFactoryFactory create(
      Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider) {
    return new WorkerModule_ProvideWorkerFactoryFactory(aiProcessingWorkerFactoryProvider);
  }

  public static WorkerFactory provideWorkerFactory(
      AiProcessingWorkerFactory aiProcessingWorkerFactory) {
    return Preconditions.checkNotNullFromProvides(WorkerModule.INSTANCE.provideWorkerFactory(aiProcessingWorkerFactory));
  }
}
