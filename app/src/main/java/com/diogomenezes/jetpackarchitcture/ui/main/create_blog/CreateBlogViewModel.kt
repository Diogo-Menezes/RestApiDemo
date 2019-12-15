package com.diogomenezes.jetpackarchitcture.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.diogomenezes.jetpackarchitcture.repository.main.CreateBlogRepository
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.viewmodel.BaseViewModel
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Loading
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogViewState
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import com.diogomenezes.jetpackarchitcture.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {


    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(stateEvent: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>>? {

        when (stateEvent) {
            is CreateBlogStateEvent.CreateNewBlogEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )
                    createBlogRepository.createNewBlogPost(
                        authToken,
                        title,
                        body,
                        stateEvent.image
                    )
                } ?: AbsentLiveData.create()
            }
            is CreateBlogStateEvent.None -> {
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    fun setNewBlogFields(title: String? = null, body: String? = null, uri: Uri? = null) {
        val update = getCurrentViewStateOrNew()
        val newBLogFields = update.blogFields
        title?.let { newBLogFields.newBlogTitle = it }
        body?.let { newBLogFields.newBlogBody = it }
        uri?.let { newBLogFields.newImageUri = it }
        update.blogFields = newBLogFields
        setViewState(update)
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    fun getNewImageUri(): Uri? {
        return getCurrentViewStateOrNew().blogFields.newImageUri
    }

    fun cancelActiveJobs() {
        handlePendingData()
        createBlogRepository.cancelActiveJobs()
    }

    fun handlePendingData() {
        setStateEvent(CreateBlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}