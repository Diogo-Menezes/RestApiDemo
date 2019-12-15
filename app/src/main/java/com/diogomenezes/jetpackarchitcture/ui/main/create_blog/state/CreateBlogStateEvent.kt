package com.diogomenezes.jetpackarchitcture.ui.main.create_blog.state

import okhttp3.MultipartBody

sealed class CreateBlogStateEvent {

    data class CreateNewBlogEvent(
        var title: String,
        var body: String,
        var image: MultipartBody.Part
    ) : CreateBlogStateEvent()


    class None : CreateBlogStateEvent()
}