package com.diogomenezes.jetpackarchitcture.ui.main.blog.viewmodel

import android.util.Log
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.blog.state.BlogViewState


fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogStateEvent.BlogSearchEvent())
}

fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page
    update.blogFields.page = page.inc()
    setViewState(update)
}

fun BlogViewModel.nextPage() {
    if (!getIsQueryExhausted()
        && !getIsQueryInProgress()
    ) {
        Log.d("BlogViewModel", "nextPage.... ")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogStateEvent.BlogSearchEvent())
    }
}


fun BlogViewModel.handleIncomingBlogListDate(viewState: BlogViewState) {
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)

}