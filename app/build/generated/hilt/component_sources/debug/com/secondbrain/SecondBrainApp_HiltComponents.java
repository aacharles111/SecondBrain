package com.secondbrain;

import com.secondbrain.di.AiModule;
import com.secondbrain.di.AppModule;
import com.secondbrain.di.WorkerModule;
import com.secondbrain.ui.MainActivity_GeneratedInjector;
import com.secondbrain.ui.card.CardDetailViewModel_HiltModules;
import com.secondbrain.ui.card.CardEditViewModel_HiltModules;
import com.secondbrain.ui.card.CreateCardActivity_GeneratedInjector;
import com.secondbrain.ui.card.CreateCardViewModel_HiltModules;
import com.secondbrain.ui.card.SummaryReviewActivity_GeneratedInjector;
import com.secondbrain.ui.card.SummaryReviewViewModel_HiltModules;
import com.secondbrain.ui.home.HomeViewModel_HiltModules;
import com.secondbrain.ui.knowledge.KnowledgeGraphViewModel_HiltModules;
import com.secondbrain.ui.notes.NoteDetailViewModel_HiltModules;
import com.secondbrain.ui.notes.NoteEditViewModel_HiltModules;
import com.secondbrain.ui.notes.NoteListViewModel_HiltModules;
import com.secondbrain.ui.search.SearchViewModel_HiltModules;
import com.secondbrain.ui.settings.AiSettingsViewModel_HiltModules;
import com.secondbrain.ui.settings.ClaudeModelViewModel_HiltModules;
import com.secondbrain.ui.settings.CostAwareModelSelectorViewModel_HiltModules;
import com.secondbrain.ui.settings.DeepSeekModelViewModel_HiltModules;
import com.secondbrain.ui.settings.GeminiModelViewModel_HiltModules;
import com.secondbrain.ui.settings.OpenAiModelViewModel_HiltModules;
import com.secondbrain.ui.settings.OpenRouterViewModel_HiltModules;
import com.secondbrain.ui.settings.SettingsActivity_GeneratedInjector;
import com.secondbrain.ui.settings.SettingsViewModel_HiltModules;
import com.secondbrain.ui.settings.SystemPromptSettingsViewModel_HiltModules;
import com.secondbrain.ui.test.ThumbnailTestActivity_GeneratedInjector;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.components.ViewComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;
import dagger.hilt.android.flags.FragmentGetContextFix;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule;
import dagger.hilt.android.internal.managers.ActivityComponentManager;
import dagger.hilt.android.internal.managers.FragmentComponentManager;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule;
import dagger.hilt.android.internal.managers.HiltWrapper_SavedStateHandleModule;
import dagger.hilt.android.internal.managers.ServiceComponentManager;
import dagger.hilt.android.internal.managers.ViewComponentManager;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.android.scopes.FragmentScoped;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.android.scopes.ViewModelScoped;
import dagger.hilt.android.scopes.ViewScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import javax.annotation.processing.Generated;
import javax.inject.Singleton;

@Generated("dagger.hilt.processor.internal.root.RootProcessor")
public final class SecondBrainApp_HiltComponents {
  private SecondBrainApp_HiltComponents() {
  }

  @Module(
      subcomponents = ServiceC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ServiceCBuilderModule {
    @Binds
    ServiceComponentBuilder bind(ServiceC.Builder builder);
  }

  @Module(
      subcomponents = ActivityRetainedC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityRetainedCBuilderModule {
    @Binds
    ActivityRetainedComponentBuilder bind(ActivityRetainedC.Builder builder);
  }

  @Module(
      subcomponents = ActivityC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityCBuilderModule {
    @Binds
    ActivityComponentBuilder bind(ActivityC.Builder builder);
  }

  @Module(
      subcomponents = ViewModelC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewModelCBuilderModule {
    @Binds
    ViewModelComponentBuilder bind(ViewModelC.Builder builder);
  }

  @Module(
      subcomponents = ViewC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewCBuilderModule {
    @Binds
    ViewComponentBuilder bind(ViewC.Builder builder);
  }

  @Module(
      subcomponents = FragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface FragmentCBuilderModule {
    @Binds
    FragmentComponentBuilder bind(FragmentC.Builder builder);
  }

  @Module(
      subcomponents = ViewWithFragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewWithFragmentCBuilderModule {
    @Binds
    ViewWithFragmentComponentBuilder bind(ViewWithFragmentC.Builder builder);
  }

  @Component(
      modules = {
          AiModule.class,
          AppModule.class,
          ApplicationContextModule.class,
          HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule.class,
          ActivityRetainedCBuilderModule.class,
          ServiceCBuilderModule.class,
          WorkerModule.class
      }
  )
  @Singleton
  public abstract static class SingletonC implements SecondBrainApp_GeneratedInjector,
      FragmentGetContextFix.FragmentGetContextFixEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint,
      ServiceComponentManager.ServiceComponentBuilderEntryPoint,
      SingletonComponent,
      GeneratedComponent {
  }

  @Subcomponent
  @ServiceScoped
  public abstract static class ServiceC implements ServiceComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ServiceComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          AiSettingsViewModel_HiltModules.KeyModule.class,
          CardDetailViewModel_HiltModules.KeyModule.class,
          CardEditViewModel_HiltModules.KeyModule.class,
          ClaudeModelViewModel_HiltModules.KeyModule.class,
          CostAwareModelSelectorViewModel_HiltModules.KeyModule.class,
          CreateCardViewModel_HiltModules.KeyModule.class,
          DeepSeekModelViewModel_HiltModules.KeyModule.class,
          GeminiModelViewModel_HiltModules.KeyModule.class,
          HiltWrapper_ActivityRetainedComponentManager_LifecycleModule.class,
          HiltWrapper_SavedStateHandleModule.class,
          HomeViewModel_HiltModules.KeyModule.class,
          KnowledgeGraphViewModel_HiltModules.KeyModule.class,
          NoteDetailViewModel_HiltModules.KeyModule.class,
          NoteEditViewModel_HiltModules.KeyModule.class,
          NoteListViewModel_HiltModules.KeyModule.class,
          OpenAiModelViewModel_HiltModules.KeyModule.class,
          OpenRouterViewModel_HiltModules.KeyModule.class,
          SearchViewModel_HiltModules.KeyModule.class,
          ActivityCBuilderModule.class,
          ViewModelCBuilderModule.class,
          SettingsViewModel_HiltModules.KeyModule.class,
          SummaryReviewViewModel_HiltModules.KeyModule.class,
          SystemPromptSettingsViewModel_HiltModules.KeyModule.class
      }
  )
  @ActivityRetainedScoped
  public abstract static class ActivityRetainedC implements ActivityRetainedComponent,
      ActivityComponentManager.ActivityComponentBuilderEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityRetainedComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          HiltWrapper_ActivityModule.class,
          HiltWrapper_DefaultViewModelFactories_ActivityModule.class,
          FragmentCBuilderModule.class,
          ViewCBuilderModule.class
      }
  )
  @ActivityScoped
  public abstract static class ActivityC implements MainActivity_GeneratedInjector,
      CreateCardActivity_GeneratedInjector,
      SummaryReviewActivity_GeneratedInjector,
      SettingsActivity_GeneratedInjector,
      ThumbnailTestActivity_GeneratedInjector,
      ActivityComponent,
      DefaultViewModelFactories.ActivityEntryPoint,
      HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint,
      FragmentComponentManager.FragmentComponentBuilderEntryPoint,
      ViewComponentManager.ViewComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          AiSettingsViewModel_HiltModules.BindsModule.class,
          CardDetailViewModel_HiltModules.BindsModule.class,
          CardEditViewModel_HiltModules.BindsModule.class,
          ClaudeModelViewModel_HiltModules.BindsModule.class,
          CostAwareModelSelectorViewModel_HiltModules.BindsModule.class,
          CreateCardViewModel_HiltModules.BindsModule.class,
          DeepSeekModelViewModel_HiltModules.BindsModule.class,
          GeminiModelViewModel_HiltModules.BindsModule.class,
          HiltWrapper_HiltViewModelFactory_ViewModelModule.class,
          HomeViewModel_HiltModules.BindsModule.class,
          KnowledgeGraphViewModel_HiltModules.BindsModule.class,
          NoteDetailViewModel_HiltModules.BindsModule.class,
          NoteEditViewModel_HiltModules.BindsModule.class,
          NoteListViewModel_HiltModules.BindsModule.class,
          OpenAiModelViewModel_HiltModules.BindsModule.class,
          OpenRouterViewModel_HiltModules.BindsModule.class,
          SearchViewModel_HiltModules.BindsModule.class,
          SettingsViewModel_HiltModules.BindsModule.class,
          SummaryReviewViewModel_HiltModules.BindsModule.class,
          SystemPromptSettingsViewModel_HiltModules.BindsModule.class
      }
  )
  @ViewModelScoped
  public abstract static class ViewModelC implements ViewModelComponent,
      HiltViewModelFactory.ViewModelFactoriesEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewModelComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewC implements ViewComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewComponentBuilder {
    }
  }

  @Subcomponent(
      modules = ViewWithFragmentCBuilderModule.class
  )
  @FragmentScoped
  public abstract static class FragmentC implements FragmentComponent,
      DefaultViewModelFactories.FragmentEntryPoint,
      ViewComponentManager.ViewWithFragmentComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends FragmentComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewWithFragmentC implements ViewWithFragmentComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewWithFragmentComponentBuilder {
    }
  }
}
