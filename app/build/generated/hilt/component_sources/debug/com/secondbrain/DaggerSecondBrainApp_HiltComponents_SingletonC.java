package com.secondbrain;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.secondbrain.data.db.CardDao;
import com.secondbrain.data.db.NoteDao;
import com.secondbrain.data.db.NoteDatabase;
import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.repository.NoteRepository;
import com.secondbrain.data.repository.SettingsRepository;
import com.secondbrain.data.service.AiService;
import com.secondbrain.data.service.ai.AiServiceManager;
import com.secondbrain.data.service.ai.ClaudeProvider;
import com.secondbrain.data.service.ai.DeepSeekProvider;
import com.secondbrain.data.service.ai.GeminiProvider;
import com.secondbrain.data.service.ai.OpenAiProvider;
import com.secondbrain.data.service.ai.OpenRouterProvider;
import com.secondbrain.data.service.ai.content.EntityExtractor;
import com.secondbrain.data.service.ai.worker.AiProcessingWorker;
import com.secondbrain.data.service.knowledge.KnowledgeGraphService;
import com.secondbrain.di.AiModule_ProvideAiServiceFactory;
import com.secondbrain.di.AiProcessingWorkerFactory;
import com.secondbrain.di.AppModule_ProvideCardDaoFactory;
import com.secondbrain.di.AppModule_ProvideNoteDaoFactory;
import com.secondbrain.di.AppModule_ProvideNoteDatabaseFactory;
import com.secondbrain.di.WorkerModule_ProvideWorkerFactoryFactory;
import com.secondbrain.ui.MainActivity;
import com.secondbrain.ui.card.CreateCardActivity;
import com.secondbrain.ui.card.CreateCardViewModel;
import com.secondbrain.ui.card.CreateCardViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.card.SummaryReviewActivity;
import com.secondbrain.ui.card.SummaryReviewViewModel;
import com.secondbrain.ui.card.SummaryReviewViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.home.HomeViewModel;
import com.secondbrain.ui.home.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.knowledge.KnowledgeGraphViewModel;
import com.secondbrain.ui.knowledge.KnowledgeGraphViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.notes.NoteDetailViewModel;
import com.secondbrain.ui.notes.NoteDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.notes.NoteEditViewModel;
import com.secondbrain.ui.notes.NoteEditViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.notes.NoteListViewModel;
import com.secondbrain.ui.notes.NoteListViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.search.SearchViewModel;
import com.secondbrain.ui.search.SearchViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.settings.AiSettingsViewModel;
import com.secondbrain.ui.settings.AiSettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.settings.OpenRouterViewModel;
import com.secondbrain.ui.settings.OpenRouterViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.ui.settings.SettingsViewModel;
import com.secondbrain.ui.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.secondbrain.util.SecureStorage;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerSecondBrainApp_HiltComponents_SingletonC {
  private DaggerSecondBrainApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public SecondBrainApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SecondBrainApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements SecondBrainApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SecondBrainApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SecondBrainApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SecondBrainApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SecondBrainApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SecondBrainApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SecondBrainApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SecondBrainApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SecondBrainApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SecondBrainApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SecondBrainApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectCreateCardActivity(CreateCardActivity createCardActivity) {
    }

    @Override
    public void injectSummaryReviewActivity(SummaryReviewActivity summaryReviewActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(AiSettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), CreateCardViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide(), KnowledgeGraphViewModel_HiltModules_KeyModule_ProvideFactory.provide(), NoteDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), NoteEditViewModel_HiltModules_KeyModule_ProvideFactory.provide(), NoteListViewModel_HiltModules_KeyModule_ProvideFactory.provide(), OpenRouterViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SearchViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SummaryReviewViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends SecondBrainApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AiSettingsViewModel> aiSettingsViewModelProvider;

    private Provider<CreateCardViewModel> createCardViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<KnowledgeGraphViewModel> knowledgeGraphViewModelProvider;

    private Provider<NoteDetailViewModel> noteDetailViewModelProvider;

    private Provider<NoteEditViewModel> noteEditViewModelProvider;

    private Provider<NoteListViewModel> noteListViewModelProvider;

    private Provider<OpenRouterViewModel> openRouterViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SummaryReviewViewModel> summaryReviewViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.aiSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.createCardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.knowledgeGraphViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.noteDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.noteEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.noteListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.openRouterViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.summaryReviewViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(11).put("com.secondbrain.ui.settings.AiSettingsViewModel", ((Provider) aiSettingsViewModelProvider)).put("com.secondbrain.ui.card.CreateCardViewModel", ((Provider) createCardViewModelProvider)).put("com.secondbrain.ui.home.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.secondbrain.ui.knowledge.KnowledgeGraphViewModel", ((Provider) knowledgeGraphViewModelProvider)).put("com.secondbrain.ui.notes.NoteDetailViewModel", ((Provider) noteDetailViewModelProvider)).put("com.secondbrain.ui.notes.NoteEditViewModel", ((Provider) noteEditViewModelProvider)).put("com.secondbrain.ui.notes.NoteListViewModel", ((Provider) noteListViewModelProvider)).put("com.secondbrain.ui.settings.OpenRouterViewModel", ((Provider) openRouterViewModelProvider)).put("com.secondbrain.ui.search.SearchViewModel", ((Provider) searchViewModelProvider)).put("com.secondbrain.ui.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.secondbrain.ui.card.SummaryReviewViewModel", ((Provider) summaryReviewViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<String, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.secondbrain.ui.settings.AiSettingsViewModel 
          return (T) new AiSettingsViewModel(singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.aiServiceManagerProvider.get(), singletonCImpl.secureStorageProvider.get());

          case 1: // com.secondbrain.ui.card.CreateCardViewModel 
          return (T) new CreateCardViewModel(singletonCImpl.cardRepositoryProvider.get(), singletonCImpl.provideAiServiceProvider.get(), singletonCImpl.aiServiceManagerProvider.get());

          case 2: // com.secondbrain.ui.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.cardRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 3: // com.secondbrain.ui.knowledge.KnowledgeGraphViewModel 
          return (T) new KnowledgeGraphViewModel(singletonCImpl.knowledgeGraphServiceProvider.get());

          case 4: // com.secondbrain.ui.notes.NoteDetailViewModel 
          return (T) new NoteDetailViewModel(singletonCImpl.noteRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 5: // com.secondbrain.ui.notes.NoteEditViewModel 
          return (T) new NoteEditViewModel(singletonCImpl.noteRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 6: // com.secondbrain.ui.notes.NoteListViewModel 
          return (T) new NoteListViewModel(singletonCImpl.noteRepositoryProvider.get());

          case 7: // com.secondbrain.ui.settings.OpenRouterViewModel 
          return (T) new OpenRouterViewModel(singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.openRouterProvider.get());

          case 8: // com.secondbrain.ui.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.noteRepositoryProvider.get());

          case 9: // com.secondbrain.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.settingsRepositoryProvider.get());

          case 10: // com.secondbrain.ui.card.SummaryReviewViewModel 
          return (T) new SummaryReviewViewModel(singletonCImpl.cardRepositoryProvider.get(), singletonCImpl.provideAiServiceProvider.get(), viewModelCImpl.savedStateHandle);

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SecondBrainApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SecondBrainApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends SecondBrainApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<SettingsRepository> settingsRepositoryProvider;

    private Provider<SecureStorage> secureStorageProvider;

    private Provider<GeminiProvider> geminiProvider;

    private Provider<OpenAiProvider> openAiProvider;

    private Provider<ClaudeProvider> claudeProvider;

    private Provider<DeepSeekProvider> deepSeekProvider;

    private Provider<OpenRouterProvider> openRouterProvider;

    private Provider<AiServiceManager> aiServiceManagerProvider;

    private Provider<AiProcessingWorkerFactory> aiProcessingWorkerFactoryProvider;

    private Provider<WorkerFactory> provideWorkerFactoryProvider;

    private Provider<NoteDatabase> provideNoteDatabaseProvider;

    private Provider<CardDao> provideCardDaoProvider;

    private Provider<CardRepository> cardRepositoryProvider;

    private Provider<AiService> provideAiServiceProvider;

    private Provider<EntityExtractor> entityExtractorProvider;

    private Provider<KnowledgeGraphService> knowledgeGraphServiceProvider;

    private Provider<NoteDao> provideNoteDaoProvider;

    private Provider<NoteRepository> noteRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.settingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 3));
      this.secureStorageProvider = DoubleCheck.provider(new SwitchingProvider<SecureStorage>(singletonCImpl, 5));
      this.geminiProvider = DoubleCheck.provider(new SwitchingProvider<GeminiProvider>(singletonCImpl, 4));
      this.openAiProvider = DoubleCheck.provider(new SwitchingProvider<OpenAiProvider>(singletonCImpl, 6));
      this.claudeProvider = DoubleCheck.provider(new SwitchingProvider<ClaudeProvider>(singletonCImpl, 7));
      this.deepSeekProvider = DoubleCheck.provider(new SwitchingProvider<DeepSeekProvider>(singletonCImpl, 8));
      this.openRouterProvider = DoubleCheck.provider(new SwitchingProvider<OpenRouterProvider>(singletonCImpl, 9));
      this.aiServiceManagerProvider = DoubleCheck.provider(new SwitchingProvider<AiServiceManager>(singletonCImpl, 2));
      this.aiProcessingWorkerFactoryProvider = SingleCheck.provider(new SwitchingProvider<AiProcessingWorkerFactory>(singletonCImpl, 1));
      this.provideWorkerFactoryProvider = DoubleCheck.provider(new SwitchingProvider<WorkerFactory>(singletonCImpl, 0));
      this.provideNoteDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<NoteDatabase>(singletonCImpl, 12));
      this.provideCardDaoProvider = DoubleCheck.provider(new SwitchingProvider<CardDao>(singletonCImpl, 11));
      this.cardRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CardRepository>(singletonCImpl, 10));
      this.provideAiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AiService>(singletonCImpl, 13));
      this.entityExtractorProvider = DoubleCheck.provider(new SwitchingProvider<EntityExtractor>(singletonCImpl, 15));
      this.knowledgeGraphServiceProvider = DoubleCheck.provider(new SwitchingProvider<KnowledgeGraphService>(singletonCImpl, 14));
      this.provideNoteDaoProvider = DoubleCheck.provider(new SwitchingProvider<NoteDao>(singletonCImpl, 17));
      this.noteRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NoteRepository>(singletonCImpl, 16));
    }

    @Override
    public void injectSecondBrainApp(SecondBrainApp secondBrainApp) {
      injectSecondBrainApp2(secondBrainApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private SecondBrainApp injectSecondBrainApp2(SecondBrainApp instance) {
      SecondBrainApp_MembersInjector.injectWorkerFactory(instance, provideWorkerFactoryProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // androidx.work.WorkerFactory 
          return (T) WorkerModule_ProvideWorkerFactoryFactory.provideWorkerFactory(singletonCImpl.aiProcessingWorkerFactoryProvider.get());

          case 1: // com.secondbrain.di.AiProcessingWorkerFactory 
          return (T) new AiProcessingWorkerFactory() {
            @Override
            public AiProcessingWorker create(Context context, WorkerParameters params) {
              return new AiProcessingWorker(context, params, singletonCImpl.aiServiceManagerProvider.get());
            }
          };

          case 2: // com.secondbrain.data.service.ai.AiServiceManager 
          return (T) new AiServiceManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.geminiProvider.get(), singletonCImpl.openAiProvider.get(), singletonCImpl.claudeProvider.get(), singletonCImpl.deepSeekProvider.get(), singletonCImpl.openRouterProvider.get());

          case 3: // com.secondbrain.data.repository.SettingsRepository 
          return (T) new SettingsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.secondbrain.data.service.ai.GeminiProvider 
          return (T) new GeminiProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.secureStorageProvider.get());

          case 5: // com.secondbrain.util.SecureStorage 
          return (T) new SecureStorage(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.secondbrain.data.service.ai.OpenAiProvider 
          return (T) new OpenAiProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.secureStorageProvider.get());

          case 7: // com.secondbrain.data.service.ai.ClaudeProvider 
          return (T) new ClaudeProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get());

          case 8: // com.secondbrain.data.service.ai.DeepSeekProvider 
          return (T) new DeepSeekProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get());

          case 9: // com.secondbrain.data.service.ai.OpenRouterProvider 
          return (T) new OpenRouterProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsRepositoryProvider.get());

          case 10: // com.secondbrain.data.repository.CardRepository 
          return (T) new CardRepository(singletonCImpl.provideCardDaoProvider.get());

          case 11: // com.secondbrain.data.db.CardDao 
          return (T) AppModule_ProvideCardDaoFactory.provideCardDao(singletonCImpl.provideNoteDatabaseProvider.get());

          case 12: // com.secondbrain.data.db.NoteDatabase 
          return (T) AppModule_ProvideNoteDatabaseFactory.provideNoteDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.secondbrain.data.service.AiService 
          return (T) AiModule_ProvideAiServiceFactory.provideAiService(singletonCImpl.aiServiceManagerProvider.get());

          case 14: // com.secondbrain.data.service.knowledge.KnowledgeGraphService 
          return (T) new KnowledgeGraphService(singletonCImpl.cardRepositoryProvider.get(), singletonCImpl.entityExtractorProvider.get(), singletonCImpl.aiServiceManagerProvider.get());

          case 15: // com.secondbrain.data.service.ai.content.EntityExtractor 
          return (T) new EntityExtractor(singletonCImpl.aiServiceManagerProvider.get());

          case 16: // com.secondbrain.data.repository.NoteRepository 
          return (T) new NoteRepository(singletonCImpl.provideNoteDaoProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // com.secondbrain.data.db.NoteDao 
          return (T) AppModule_ProvideNoteDaoFactory.provideNoteDao(singletonCImpl.provideNoteDatabaseProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
