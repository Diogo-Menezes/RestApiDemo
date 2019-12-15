package com.diogomenezes.jetpackarchitcture.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diogomenezes.jetpackarchitcture.models.AccountProperties
import com.diogomenezes.jetpackarchitcture.models.AuthToken
import com.diogomenezes.jetpackarchitcture.models.BlogPost

@Database(entities = [AuthToken::class, AccountProperties::class, BlogPost::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}