package com.secondbrain.data.service.ai;

import android.content.Context;
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
public final class AiProcessingService_Factory implements Factory<AiProcessingService> {
  private final Provider<Context> contextProvider;

  public AiProcessingService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AiProcessingService get() {
    return newInstance(contextProvider.get());
  }

  public static AiProcessingService_Factory create(Provider<Context> contextProvider) {
    return new AiProcessingService_Factory(contextProvider);
  }

  public static AiProcessingService newInstance(Context context) {
    return new AiProcessingService(context);
  }
}
