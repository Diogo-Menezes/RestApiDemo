package com.diogomenezes.jetpackarchitcture.network.api.main

import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.network.GenericResponse
import com.diogomenezes.jetpackarchitcture.network.api.main.responses.BlogListSearchResponse
import com.diogomenezes.jetpackarchitcture.network.api.main.responses.BlogCreateUpdateResponse
import com.diogomenezes.jetpackarchitcture.models.AccountProperties
import com.diogomenezes.jetpackarchitcture.util.GenericApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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


    @PUT("account/change_password/")
    @FormUrlEncoded
    fun updatePassword(
        @Header("Authorization") authorization: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmNewPassword: String
    ): LiveData<GenericApiResponse<GenericResponse>>


    @GET("blog/list")
    fun searchListBlogPosts(
        @Header("Authorization") token: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int


    ): LiveData<GenericApiResponse<BlogListSearchResponse>>


    @GET("blog/{slug}/is_author")
    fun isAuthorOfBlogPost(
        @Header("Authorization") token: String,
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @DELETE("blog/{slug}/delete")
    fun deleteBlogPost(
        @Header("Authorization") token: String,
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @Multipart
    @PUT("blog/{slug}/update")
    fun updateBlogPost(
        @Header("Authorization") token: String,
        @Path("slug") slug: String,
        @Part("title") title: RequestBody,
        @Part("title") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<BlogCreateUpdateResponse>>

    @Multipart
    @PUT("blog/create")
    fun createBlog(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("title") body: RequestBody,
        @Part image: MultipartBody.Part?
    ):LiveData<GenericApiResponse<BlogCreateUpdateResponse>>
}