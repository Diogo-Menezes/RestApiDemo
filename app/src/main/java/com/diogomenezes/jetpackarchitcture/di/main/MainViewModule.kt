package com.diogomenezes.jetpackarchitcture.di.main

import androidx.lifecycle.ViewModel
import com.diogomenezes.jetpackarchitcture.di.ViewModelKey
import com.diogomenezes.jetpackarchitcture.ui.main.account.AccountViewModel
import com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel.BlogViewModel
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.CreateBlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAuthViewModel(accountViewModel: AccountViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel
}
