package com.diogomenezes.jetpackarchitcture.di.main

import com.diogomenezes.jetpackarchitcture.network.api.main.OpenApiMainService
import com.diogomenezes.jetpackarchitcture.database.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.database.AppDatabase
import com.diogomenezes.jetpackarchitcture.database.BlogPostDao
import com.diogomenezes.jetpackarchitcture.repository.main.AccountRepository
import com.diogomenezes.jetpackarchitcture.repository.main.BlogRepository
import com.diogomenezes.jetpackarchitcture.repository.main.CreateBlogRepository
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

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(
            openApiMainService,
            blogPostDao,
            sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(
            openApiMainService,
            blogPostDao,
            sessionManager
        )
    }
}