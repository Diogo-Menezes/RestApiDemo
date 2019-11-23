package com.diogomenezes.jetpackarchitcture.di.auth

import com.diogomenezes.jetpackarchitcture.di.AuthModule
import com.diogomenezes.jetpackarchitcture.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

}