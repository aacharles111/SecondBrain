package com.secondbrain.data.repository;

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
public final class SystemPromptRepository_Factory implements Factory<SystemPromptRepository> {
  private final Provider<Context> contextProvider;

  public SystemPromptRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SystemPromptRepository get() {
    return newInstance(contextProvider.get());
  }

  public static SystemPromptRepository_Factory create(Provider<Context> contextProvider) {
    return new SystemPromptRepository_Factory(contextProvider);
  }

  public static SystemPromptRepository newInstance(Context context) {
    return new SystemPromptRepository(context);
  }
}
