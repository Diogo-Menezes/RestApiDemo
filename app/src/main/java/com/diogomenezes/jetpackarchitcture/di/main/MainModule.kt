package com.diogomenezes.jetpackarchitcture.di.main

import com.diogomenezes.jetpackarchitcture.api.main.OpenApiMainService
import com.diogomenezes.jetpackarchitcture.persistance.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.repository.main.AccountRepository
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
            .build()
            .create(OpenApiMainService::class.java)
    }


    @MainScope
    @Provides
    fun provideAccountRepository(
        openApiMainService: OpenApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            openApiMainService,
            accountPropertiesDao,
            sessionManager
        )
    }
}