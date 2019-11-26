package com.diogomenezes.jetpackarchitcture.di

import com.diogomenezes.jetpackarchitcture.di.auth.AuthFragmentBuildersModule
import com.diogomenezes.jetpackarchitcture.di.auth.AuthModule
import com.diogomenezes.jetpackarchitcture.di.auth.AuthScope
import com.diogomenezes.jetpackarchitcture.di.auth.AuthViewModelModule
import com.diogomenezes.jetpackarchitcture.di.main.MainFragmentBuildersModule
import com.diogomenezes.jetpackarchitcture.di.main.MainModule
import com.diogomenezes.jetpackarchitcture.di.main.MainScope
import com.diogomenezes.jetpackarchitcture.di.main.MainViewModelModule
import com.diogomenezes.jetpackarchitcture.ui.auth.AuthActivity
import com.diogomenezes.jetpackarchitcture.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [
            AuthModule::class,
            AuthFragmentBuildersModule::class,
            AuthViewModelModule::class
        ]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [
            MainModule::class,
            MainFragmentBuildersModule::class,
            MainViewModelModule::class
        ]
    )
    abstract fun contributeMainActivity(): MainActivity

}