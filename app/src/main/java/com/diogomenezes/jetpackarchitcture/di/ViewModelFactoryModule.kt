package com.diogomenezes.jetpackarchitcture.di

import androidx.lifecycle.ViewModelProvider
import com.diogomenezes.jetpackarchitcture.viewmodel.ViewModelProviderFactory

import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}