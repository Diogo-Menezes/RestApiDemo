package com.diogomenezes.jetpackarchitcture

import android.util.Log
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.*
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener {
    val TAG: String = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun OnDataStateChange(dataState: DataState<*>?) {
        dataState?.let {
            GlobalScope.launch(Main) {
                displayProgressBar(it.loading.isLoading)

                it.error?.let { event ->
                    handleStateError(event)
                }
                it.data?.let {
                    it.response?.let {
                        handleStateResponse(it)
                    }
                }
            }
        }
    }

    private fun handleStateError(event: Event<StateError>) {

        event.getContentIfNotHandled()?.let {
            when (it.response.responseType) {
                is ResponseType.Toast -> {
                    it.response.message?.let { message ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog -> {
                    it.response.message?.let { message ->
                        displayErrorDialog(message)
                    }
                }

                is ResponseType.None -> {
                    Log.e("BaseActivity", "handleStateError (line 50): ${it.response.message}")
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>) {

        event.getContentIfNotHandled()?.let {
            when (it.responseType) {
                is ResponseType.Toast -> {
                    it.message?.let { message ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog -> {
                    it.message?.let { message ->
                        displayErrorDialog(message)
                    }
                }

                is ResponseType.None -> {
Log.d("BaseActivity", "handleStateResponse (line 73): ")
                }
            }
        }
    }

    abstract fun displayProgressBar(display: Boolean)
}