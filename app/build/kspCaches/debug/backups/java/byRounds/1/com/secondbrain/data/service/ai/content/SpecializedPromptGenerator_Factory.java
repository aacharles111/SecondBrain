package com.secondbrain.data.service.ai.content;

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
public final class SpecializedPromptGenerator_Factory implements Factory<SpecializedPromptGenerator> {
  @Override
  public SpecializedPromptGenerator get() {
    return newInstance();
  }

  public static SpecializedPromptGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SpecializedPromptGenerator newInstance() {
    return new SpecializedPromptGenerator();
  }

  private static final class InstanceHolder {
    private static final SpecializedPromptGenerator_Factory INSTANCE = new SpecializedPromptGenerator_Factory();
  }
}
