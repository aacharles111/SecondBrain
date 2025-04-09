package com.secondbrain;

import androidx.work.WorkerFactory;
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
public final class SecondBrainApp_MembersInjector implements MembersInjector<SecondBrainApp> {
  private final Provider<WorkerFactory> workerFactoryProvider;

  public SecondBrainApp_MembersInjector(Provider<WorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<SecondBrainApp> create(
      Provider<WorkerFactory> workerFactoryProvider) {
    return new SecondBrainApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(SecondBrainApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.secondbrain.SecondBrainApp.workerFactory")
  public static void injectWorkerFactory(SecondBrainApp instance, WorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
