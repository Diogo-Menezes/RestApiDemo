package com.diogomenezes.jetpackarchitcture

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.*
import com.diogomenezes.jetpackarchitcture.ui.UIMessageType.*
import com.diogomenezes.jetpackarchitcture.util.Constants
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener,
    UICommunicationListener {
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

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMM.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }

    }

    override fun onUIMessageReceived(uiMessage: UiMessage) {
        when (uiMessage.uiMessageType) {

            is AreYouSureDialog -> {
                areYouSureDialog(
                    uiMessage.message,
                    uiMessage.uiMessageType.callback
                )
            }
            is Toast -> {
                displayToast(uiMessage.message)
            }
            is Dialog -> {
                displayInfoDialog(uiMessage.message)
            }
            is None -> {
                Log.i("BaseActivity", "onUIMessageReceived: ${uiMessage.message}")
            }
        }
    }

    override fun isStoragePermissionGranted(): Boolean {
        if (
            ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                ),
                Constants.PERMISSIONS_REQUEST_READ_STORAGE
            )
            return false
        } else {
            return true
        }
    }
}