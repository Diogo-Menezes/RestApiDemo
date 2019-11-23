package com.diogomenezes.jetpackarchitcture.persistance

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.model.AuthToken

@Database(entities = [AuthToken::class, AccountProperties::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object{
        const val DATABASE_NAME = "app_database"
    }
}