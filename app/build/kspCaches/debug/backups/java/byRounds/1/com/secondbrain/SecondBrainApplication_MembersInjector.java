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
public final class SecondBrainApplication_MembersInjector implements MembersInjector<SecondBrainApplication> {
  private final Provider<WorkerFactory> workerFactoryProvider;

  public SecondBrainApplication_MembersInjector(Provider<WorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<SecondBrainApplication> create(
      Provider<WorkerFactory> workerFactoryProvider) {
    return new SecondBrainApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(SecondBrainApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.secondbrain.SecondBrainApplication.workerFactory")
  public static void injectWorkerFactory(SecondBrainApplication instance,
      WorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
