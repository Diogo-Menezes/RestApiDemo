package com.diogomenezes.jetpackarchitcture.di.main

import com.diogomenezes.jetpackarchitcture.ui.main.account.AccountFragment
import com.diogomenezes.jetpackarchitcture.ui.main.account.ChangePasswordFragment
import com.diogomenezes.jetpackarchitcture.ui.main.account.UpdateAccountFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.BlogListFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.UpdateBlogFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.ViewBlogFragment
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogListFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}