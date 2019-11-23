package com.diogomenezes.jetpackarchitcture.persistance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diogomenezes.jetpackarchitcture.model.AuthToken

@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(authToken: AuthToken)

    @Query("UPDATE auth_token SET token = null WHERE account_pk = :pk")
    fun nullifyToken (pk:Int): Int


}