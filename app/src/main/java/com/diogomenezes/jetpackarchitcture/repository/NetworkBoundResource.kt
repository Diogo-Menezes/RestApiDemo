package com.diogomenezes.jetpackarchitcture.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.util.*
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


abstract class NetworkBoundResource<ResponseObject, CacheObject, ViewStateType>
    (
    isNetworkAvailable: Boolean,
    isNetworkRequest: Boolean,
    shouldCancelIfNoInternet: Boolean,
    shouldLoadFromCache: Boolean
) {
    val TAG: String = "NetworkBoundResource"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(true, cachedData = null))

        if (shouldLoadFromCache) {
            val dbSource = loadFromCache()
            result.addSource(dbSource) { cachedData ->
                result.removeSource(dbSource)
                setValue(DataState.loading(isLoading = true, cachedData = cachedData))
            }
        }

        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                /**
                 * It was working perfect with the precheck on authRepository
//                coroutineScope.launch {
//
//                    delay(Constants.TESTING_NETWORK_DELAY)
//
//                    withContext(Main) {
//                        //make network call
//                        val apiResponse = createCall()
//                        result.addSource(apiResponse) {
//                            result.removeSource(apiResponse)
//
//                            coroutineScope.launch {
//                                handleNetworkCall(it)
//                            }
//                        }
//                    }
//                }
//                GlobalScope.launch(IO) {
//                    delay(Constants.NETWORK_TIMEOUT)
//                    if (!job.isCompleted) {
//                        Log.d("NetworkBoundResource", " (line 55): NETWORK TIMEOUT")
//                        job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
//                    }
//                }
*/


            } else {
                onErrorReturn(UNABLE_TODO_OPERATION_WO_INTERNET, true, false)
            }
        } else {
            coroutineScope.launch {

                delay(Constants.TESTING_CACHE_DELAY)
                //View data from cache only and return
                createCacheRequestAndReturn()

            }
        }
    }

    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when (response) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse -> {
                Log.d("NetworkBoundResource", "handleNetworkCall (line 74): ")
                onErrorReturn(response.errorMessage, true, false)
            }
            is ApiEmptyResponse -> {
                Log.d("NetworkBoundResource", "handleNetworkCall (line 78): HTTP 204")
                onErrorReturn("HTTP 204 - Returned Nothing", true, false)
            }
        }


    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()

        if (msg == null) {
            msg = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }
        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }
        if (useDialog) {
            responseType = ResponseType.Dialog()
        }
        onCompleteJob(
            DataState.error(
                response = Response(
                    message = msg,
                    responseType = responseType
                )
            )
        )

    }

    @UseExperimental(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {
        Log.d("NetworkBoundResource", "initNewJob (line 20): ")

        job = Job()
        job.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.d("NetworkBoundResource", "invoke (line 29): job cancelled")
                        cause?.let {
                            onErrorReturn(it.message, false, true)
                        } ?: onErrorReturn(ERROR_UNKNOWN, false, false)
                    } else if (job.isCompleted) {
                        Log.d("NetworkBoundResource", "invoke (line 34): Job completed")
                    }
                }

            })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun createCacheRequestAndReturn()

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun loadFromCache(): LiveData<ViewStateType>

    abstract suspend fun updateLocalDb(cacheObject: CacheObject?)

    abstract fun setJob(job: Job)


}