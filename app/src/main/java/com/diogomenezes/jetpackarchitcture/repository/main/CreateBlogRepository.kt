package com.diogomenezes.jetpackarchitcture.repository.main

import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.network.api.main.OpenApiMainService
import com.diogomenezes.jetpackarchitcture.network.api.main.responses.BlogCreateUpdateResponse
import com.diogomenezes.jetpackarchitcture.models.AuthToken
import com.diogomenezes.jetpackarchitcture.models.BlogPost
import com.diogomenezes.jetpackarchitcture.database.BlogPostDao
import com.diogomenezes.jetpackarchitcture.repository.JobManager
import com.diogomenezes.jetpackarchitcture.repository.NetworkBoundResource
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogViewState
import com.diogomenezes.jetpackarchitcture.util.AbsentLiveData
import com.diogomenezes.jetpackarchitcture.util.ApiSuccessResponse
import com.diogomenezes.jetpackarchitcture.util.DateUtil.Companion.convertServerStringDateToLong
import com.diogomenezes.jetpackarchitcture.util.GenericApiResponse
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("CreateBlogRepository") {

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                if (!response.body.response.equals(SuccessHandling.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                    val updateBlogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )
                    updateLocalDb(updateBlogPost)
                }
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(
                                response.body.response,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                    "Token ${authToken.token}",
                    title,
                    body,
                    image
                )
            }

            override fun loadFromCache(): LiveData<CreateBlogViewState> = AbsentLiveData.create()

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPostDao.insert(it) }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost",job)
            }
        }.asLiveData()
    }
}