package com.secondbrain.di;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.secondbrain.data.service.ai.worker.AiProcessingWorker;
import com.secondbrain.data.service.ai.worker.AiProcessingWorker_Factory;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AiProcessingWorkerFactory_Impl implements AiProcessingWorkerFactory {
  private final AiProcessingWorker_Factory delegateFactory;

  AiProcessingWorkerFactory_Impl(AiProcessingWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AiProcessingWorker create(Context context, WorkerParameters params) {
    return delegateFactory.get(context, params);
  }

  public static Provider<AiProcessingWorkerFactory> create(
      AiProcessingWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AiProcessingWorkerFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<AiProcessingWorkerFactory> createFactoryProvider(
      AiProcessingWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AiProcessingWorkerFactory_Impl(delegateFactory));
  }
}
