package com.secondbrain.ui.knowledge;

import com.secondbrain.data.service.knowledge.KnowledgeGraphService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class KnowledgeGraphViewModel_Factory implements Factory<KnowledgeGraphViewModel> {
  private final Provider<KnowledgeGraphService> knowledgeGraphServiceProvider;

  public KnowledgeGraphViewModel_Factory(
      Provider<KnowledgeGraphService> knowledgeGraphServiceProvider) {
    this.knowledgeGraphServiceProvider = knowledgeGraphServiceProvider;
  }

  @Override
  public KnowledgeGraphViewModel get() {
    return newInstance(knowledgeGraphServiceProvider.get());
  }

  public static KnowledgeGraphViewModel_Factory create(
      Provider<KnowledgeGraphService> knowledgeGraphServiceProvider) {
    return new KnowledgeGraphViewModel_Factory(knowledgeGraphServiceProvider);
  }

  public static KnowledgeGraphViewModel newInstance(KnowledgeGraphService knowledgeGraphService) {
    return new KnowledgeGraphViewModel(knowledgeGraphService);
  }
}
