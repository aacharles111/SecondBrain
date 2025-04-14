package com.secondbrain.ui.home;

import android.app.Application;
import com.secondbrain.data.repository.CardRepository;
import com.secondbrain.data.repository.SettingsRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CardRepository> cardRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<Application> applicationProvider;

  public HomeViewModel_Factory(Provider<CardRepository> cardRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.cardRepositoryProvider = cardRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(cardRepositoryProvider.get(), settingsRepositoryProvider.get(), applicationProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<CardRepository> cardRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new HomeViewModel_Factory(cardRepositoryProvider, settingsRepositoryProvider, applicationProvider);
  }

  public static HomeViewModel newInstance(CardRepository cardRepository,
      SettingsRepository settingsRepository, Application application) {
    return new HomeViewModel(cardRepository, settingsRepository, application);
  }
}
