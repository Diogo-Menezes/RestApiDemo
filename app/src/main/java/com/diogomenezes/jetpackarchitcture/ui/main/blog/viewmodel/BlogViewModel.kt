package com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.database.BlogQueryUtils
import com.diogomenezes.jetpackarchitcture.repository.main.BlogRepository
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.viewmodel.BaseViewModel
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Loading
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent.*
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState
import com.diogomenezes.jetpackarchitcture.util.AbsentLiveData
import com.diogomenezes.jetpackarchitcture.util.PreferenceKeys.Companion.BLOG_FILTER
import com.diogomenezes.jetpackarchitcture.util.PreferenceKeys.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor

) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        setBlogOrder(
            sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_ASC
            )
        )
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>>? {

        when (stateEvent) {
            is BlogSearchEvent -> {
                return sessionManager.cachedToken.value?.let {
                    blogRepository.searchBlogPosts(
                        authToken = it,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                } ?: AbsentLiveData.create()
            }
            is BlogStateEvent.CheckAuthorBlogPost -> {
                return sessionManager.cachedToken.value?.let {
                    blogRepository.isAuthorOfBlogPost(
                        authToken = it,
                        slug = getSlug()
                    )
                }
            }

            is None -> {
                return object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null

                        )
                    }
                }
            }
            is DeleteBlogPostEvent ->
                return sessionManager.cachedToken.value?.let {
                    blogRepository.deleteBlogPost(
                        authToken = it,
                        blogPost = getBlogPost()

                    )
                }
            is UpdatedBlogPostEvent ->
                return sessionManager.cachedToken.value?.let { authToken ->
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    val image: MultipartBody.Part? = null

                    blogRepository.updateBlogPost(
                        authToken,
                        getSlug(),
                        title,
                        body,
                        image
                    )
                }
        }
    }


    fun cancelActiveJobs() {
        handlePendingData()
        blogRepository.cancelActiveJobs()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter).apply()
        editor.putString(BLOG_ORDER, order).apply()

    }
}