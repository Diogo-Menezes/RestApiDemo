package com.diogomenezes.jetpackarchitcture.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.diogomenezes.jetpackarchitcture.network.GenericResponse
import com.diogomenezes.jetpackarchitcture.network.api.main.OpenApiMainService
import com.diogomenezes.jetpackarchitcture.network.api.main.responses.BlogCreateUpdateResponse
import com.diogomenezes.jetpackarchitcture.network.api.main.responses.BlogListSearchResponse
import com.diogomenezes.jetpackarchitcture.models.AuthToken
import com.diogomenezes.jetpackarchitcture.models.BlogPost
import com.diogomenezes.jetpackarchitcture.database.BlogPostDao
import com.diogomenezes.jetpackarchitcture.database.returnOrderedBlogQuery
import com.diogomenezes.jetpackarchitcture.repository.JobManager
import com.diogomenezes.jetpackarchitcture.repository.NetworkBoundResource
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState.BlogFields
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState.ViewBlogFields
import com.diogomenezes.jetpackarchitcture.util.*
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("BlogRepository") {


    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override suspend fun createCacheRequestAndReturn() {

                withContext(Main) {
                    result.addSource(loadFromCache()) {
                        it.blogFields.isQueryInProgress = false
                        if (page * Constants.PAGINATION_PAGE_SIZE > it.blogFields.blogList.size) {
                            it.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(it, null))
                    }
                }

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {

                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for (blogPostResponse in response.body.result) {
                    blogPostList.add(
                        BlogPost(
                            blogPostResponse.pk,
                            blogPostResponse.title,
                            blogPostResponse.slug,
                            blogPostResponse.body,
                            blogPostResponse.image,
                            DateUtil.convertServerStringDateToLong(blogPostResponse.date_updated),
                            blogPostResponse.username
                        )
                    )
                }
                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return openApiMainService.searchListBlogPosts(
                    "Token ${authToken.token}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao
                    .returnOrderedBlogQuery(
                        query = query,
                        filterAndOrder = filterAndOrder,
                        page = page
                    )
                    .switchMap {
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogFields(
                                        blogList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }

            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if (cacheObject != null) {
                    withContext(Dispatchers.IO) {
                        for (blogPost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                launch {
                                    Log.d(
                                        "BlogRepository",
                                        "updateLocalDb inserting blog: ${blogPost.title} "
                                    )
                                    blogPostDao.insert(blogPost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "BlogRepository",
                                    "updateLocalDb (line 69): error updating ${blogPost.pk} "
                                )
                            }
                        }
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }


    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {}


            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                withContext(Main) {
                    Log.d("BlogRepository", "handleApiSuccessResponse :${response.body.response} ")

                    var isAuthor = false

                    if (response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)) isAuthor =
                        true

                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = ViewBlogFields(
                                    isAuthorOfBlogPost = isAuthor
                                )
                            ), response = null
                        )
                    )

                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.isAuthorOfBlogPost(
                    "Token ${authToken.token}",
                    slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> = AbsentLiveData.create()

            override suspend fun updateLocalDb(cacheObject: Any?) {}

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }

        }.asLiveData()
    }


    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    updateLocalDb(blogPost)
                } else {

                }

                onCompleteJob(
                    DataState.error(
                        Response(
                            ERROR_UNKNOWN,
                            ResponseType.Dialog()
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.deleteBlogPost(
                    "Token ${authToken.token}",
                    blogPost.slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> = AbsentLiveData.create()

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                if (cacheObject != null) {
                    blogPostDao.deleteBlogPost(cacheObject)
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(SUCCESS_BLOG_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }
        }.asLiveData()
    }

    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                val updatedBLogPost = BlogPost(
                    response.body.pk,
                    response.body.title,
                    response.body.slug,
                    response.body.body,
                    response.body.image,
                    DateUtil.convertServerStringDateToLong(
                        response.body.date_updated
                    ),
                    response.body.username
                )
                updateLocalDb(updatedBLogPost)

                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = ViewBlogFields(blogPost = updatedBLogPost)
                            ),
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.updateBlogPost(
                    "Token ${authToken.token}",
                    slug,
                    title,
                    body,
                    image
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> = AbsentLiveData.create()

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.updateBlogPost(
                        blogPost.pk,
                        blogPost.title,
                        blogPost.body,
                        blogPost.image
                    )
                }

            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }
        }.asLiveData()
    }

}


