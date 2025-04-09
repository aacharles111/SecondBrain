package com.secondbrain.data.service;

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
public final class AiService_Factory implements Factory<AiService> {
  @Override
  public AiService get() {
    return newInstance();
  }

  public static AiService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AiService newInstance() {
    return new AiService();
  }

  private static final class InstanceHolder {
    private static final AiService_Factory INSTANCE = new AiService_Factory();
  }
}
