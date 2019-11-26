package com.diogomenezes.jetpackarchitcture.di.main

import androidx.lifecycle.ViewModel
import com.diogomenezes.jetpackarchitcture.di.ViewModelKey
import com.diogomenezes.jetpackarchitcture.ui.main.account.AccountViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAuthViewModel(accountViewModel: AccountViewModel): ViewModel

}
