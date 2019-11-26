package com.diogomenezes.jetpackarchitcture.ui.auth


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.DataStateChangeListener
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.ui.auth.ForgotPasswordFragment.WebAppInterface.OnWebInteractionCallback
import com.diogomenezes.jetpackarchitcture.util.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForgotPasswordFragment : BaseAuthFragment() {

    lateinit var webView: WebView

    lateinit var stateChangeListener: DataStateChangeListener

    val webInteractionCallback = object : OnWebInteractionCallback {
        override fun onSuccess(email: String) {
            Log.d("ForgotPasswordFragment", "onSuccess (line 29): reset link sucess $email")
            onPasswordResetLinkSent()
        }

        override fun onError(errorMessage: String) {
            Log.d("ForgotPasswordFragment", "onError (line 34): $errorMessage")
            val dataState =
                DataState.error<Any>(
                    response = Response(
                        errorMessage,
                        ResponseType.None()
                    )
                )
            stateChangeListener.OnDataStateChange(dataState)
        }

        override fun onLoading(loading: Boolean) {
            Log.d("ForgotPasswordFragment", "onLoading (line 48)")

            GlobalScope.launch(Main) {
                stateChangeListener.OnDataStateChange(
                    DataState.loading(loading, null)
                )
            }
        }

    }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0f, 0f, 0f
            )
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webview)
        return_to_launcher_fragment.setOnClickListener { findNavController().popBackStack() }
        loadWebView()
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun loadWebView() {
        stateChangeListener.OnDataStateChange(
            DataState.loading(true, null)
        )

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.OnDataStateChange(DataState.loading(false, null))
            }
        }
        webView.loadUrl(Constants.PASSWORD_RESET_URL)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            WebAppInterface(webInteractionCallback),
            "AndroidTextListener"
        )
    }

    class WebAppInterface constructor(
        private val callBack: OnWebInteractionCallback
    ) {
        @JavascriptInterface
        fun onSuccess(email: String) {
            callBack.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callBack.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(loading: Boolean) {
            callBack.onLoading(loading)
        }

        interface OnWebInteractionCallback {
            fun onSuccess(email: String)
            fun onError(errorMessage: String)
            fun onLoading(loading: Boolean)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.d("ForgotPasswordFragment", "onAttach (line 38): $context Must implement interface")

        }

    }


}
