package com.secondbrain.data.service.ai.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.secondbrain.data.service.ai.AiServiceManager;
import dagger.internal.DaggerGenerated;
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
public final class AiProcessingWorker_Factory {
  private final Provider<AiServiceManager> aiServiceManagerProvider;

  public AiProcessingWorker_Factory(Provider<AiServiceManager> aiServiceManagerProvider) {
    this.aiServiceManagerProvider = aiServiceManagerProvider;
  }

  public AiProcessingWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, aiServiceManagerProvider.get());
  }

  public static AiProcessingWorker_Factory create(
      Provider<AiServiceManager> aiServiceManagerProvider) {
    return new AiProcessingWorker_Factory(aiServiceManagerProvider);
  }

  public static AiProcessingWorker newInstance(Context context, WorkerParameters params,
      AiServiceManager aiServiceManager) {
    return new AiProcessingWorker(context, params, aiServiceManager);
  }
}
