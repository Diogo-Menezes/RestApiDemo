package com.diogomenezes.jetpackarchitcture.di.auth

import com.diogomenezes.jetpackarchitcture.ui.auth.ForgotPasswordFragment
import com.diogomenezes.jetpackarchitcture.ui.auth.LauncherFragment
import com.diogomenezes.jetpackarchitcture.ui.auth.LoginFragment
import com.diogomenezes.jetpackarchitcture.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}