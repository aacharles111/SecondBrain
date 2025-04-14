package com.secondbrain.di;

import com.secondbrain.data.service.ai.AiProvider;
import com.secondbrain.data.service.ai.DefaultAiProvider;
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
public final class AiModule_ProvideAiProviderFactory implements Factory<AiProvider> {
  private final AiModule module;

  private final Provider<DefaultAiProvider> defaultAiProvider;

  public AiModule_ProvideAiProviderFactory(AiModule module,
      Provider<DefaultAiProvider> defaultAiProvider) {
    this.module = module;
    this.defaultAiProvider = defaultAiProvider;
  }

  @Override
  public AiProvider get() {
    return provideAiProvider(module, defaultAiProvider.get());
  }

  public static AiModule_ProvideAiProviderFactory create(AiModule module,
      Provider<DefaultAiProvider> defaultAiProvider) {
    return new AiModule_ProvideAiProviderFactory(module, defaultAiProvider);
  }

  public static AiProvider provideAiProvider(AiModule instance,
      DefaultAiProvider defaultAiProvider) {
    return Preconditions.checkNotNullFromProvides(instance.provideAiProvider(defaultAiProvider));
  }
}
