package com.diogomenezes.jetpackarchitcture.di

import com.diogomenezes.jetpackarchitcture.api.auth.OpenApiAuthService
import com.diogomenezes.jetpackarchitcture.di.auth.AuthScope
import com.diogomenezes.jetpackarchitcture.persistance.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.persistance.AuthTokenDao
import com.diogomenezes.jetpackarchitcture.repository.auth.AuthRepository
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {

    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager
        )
    }

}