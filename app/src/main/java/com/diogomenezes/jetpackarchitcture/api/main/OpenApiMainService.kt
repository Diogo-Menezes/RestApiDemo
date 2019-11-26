package com.diogomenezes.jetpackarchitcture.api.main

import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.api.GenericResponse
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.util.GenericApiResponse
import retrofit2.http.*

interface OpenApiMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>


    @PUT("account/properties/update")
    @FormUrlEncoded
    fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

}