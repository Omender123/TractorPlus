// Generated by Dagger (https://dagger.dev).
package com.panaceasoft.psbuyandsell.ui.common;

import androidx.lifecycle.ViewModelProvider;
import dagger.MembersInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.internal.InjectedFieldSignature;
import javax.inject.Provider;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class PSAppCompactActivity_MembersInjector implements MembersInjector<PSAppCompactActivity> {
  private final Provider<DispatchingAndroidInjector<Object>> dispatchingAndroidInjectorProvider;

  private final Provider<ViewModelProvider.Factory> viewModelFactoryProvider;

  private final Provider<NavigationController> navigationControllerProvider;

  public PSAppCompactActivity_MembersInjector(
      Provider<DispatchingAndroidInjector<Object>> dispatchingAndroidInjectorProvider,
      Provider<ViewModelProvider.Factory> viewModelFactoryProvider,
      Provider<NavigationController> navigationControllerProvider) {
    this.dispatchingAndroidInjectorProvider = dispatchingAndroidInjectorProvider;
    this.viewModelFactoryProvider = viewModelFactoryProvider;
    this.navigationControllerProvider = navigationControllerProvider;
  }

  public static MembersInjector<PSAppCompactActivity> create(
      Provider<DispatchingAndroidInjector<Object>> dispatchingAndroidInjectorProvider,
      Provider<ViewModelProvider.Factory> viewModelFactoryProvider,
      Provider<NavigationController> navigationControllerProvider) {
    return new PSAppCompactActivity_MembersInjector(dispatchingAndroidInjectorProvider, viewModelFactoryProvider, navigationControllerProvider);
  }

  @Override
  public void injectMembers(PSAppCompactActivity instance) {
    injectDispatchingAndroidInjector(instance, dispatchingAndroidInjectorProvider.get());
    injectViewModelFactory(instance, viewModelFactoryProvider.get());
    injectNavigationController(instance, navigationControllerProvider.get());
  }

  @InjectedFieldSignature("com.panaceasoft.psbuyandsell.ui.common.PSAppCompactActivity.dispatchingAndroidInjector")
  public static void injectDispatchingAndroidInjector(PSAppCompactActivity instance,
      DispatchingAndroidInjector<Object> dispatchingAndroidInjector) {
    instance.dispatchingAndroidInjector = dispatchingAndroidInjector;
  }

  @InjectedFieldSignature("com.panaceasoft.psbuyandsell.ui.common.PSAppCompactActivity.viewModelFactory")
  public static void injectViewModelFactory(PSAppCompactActivity instance,
      ViewModelProvider.Factory viewModelFactory) {
    instance.viewModelFactory = viewModelFactory;
  }

  @InjectedFieldSignature("com.panaceasoft.psbuyandsell.ui.common.PSAppCompactActivity.navigationController")
  public static void injectNavigationController(PSAppCompactActivity instance,
      NavigationController navigationController) {
    instance.navigationController = navigationController;
  }
}