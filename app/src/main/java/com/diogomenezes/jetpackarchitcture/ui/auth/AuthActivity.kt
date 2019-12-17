package com.diogomenezes.jetpackarchitcture.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.diogomenezes.jetpackarchitcture.BaseActivity
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.auth.state.AuthStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.MainActivity
import com.diogomenezes.jetpackarchitcture.util.Constants.Companion.LOGOUT
import com.diogomenezes.jetpackarchitcture.viewmodel.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity(),
    NavController.OnDestinationChangedListener {
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_graph_host_fragment).addOnDestinationChangedListener(this)

        subscribeObservers()

    }

    override fun onResume() {
        super.onResume()
        Log.i("AuthActivity", "onResume: called")
        if (!intent.hasExtra(LOGOUT)) {
            checkPreviousAuthUser()
        }
    }

    private fun subscribeObservers() {

        viewModel.dataState.observe(this, Observer { dataState ->
            OnDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let { viewState ->
                        viewState.authToken?.let {
                            Log.d("AuthActivity", "subscribeObservers (line 35): Token ${it}")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer {
            it.authToken?.let { authToken ->
                sessionManager.login(authToken)
                authToken.token?.let {
                    OnDataStateChange(DataState.loading(true, null)) }
            }
        })


        sessionManager.cachedToken.observe(this, Observer {
            it?.let { authToken ->
                Log.d("AuthActivity", "subscribeObservers (line 28): $it")
                if (authToken != null && authToken.account_pk != 1 && authToken.token != null) {
                    navMainActivity()
                }
            }
        })

    }

    fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }

    private fun navMainActivity() {
        Log.i("AuthActivity", "navMainActivity: called")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

    override fun displayProgressBar(display: Boolean) {
        if (display) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility =
                View.INVISIBLE
        }

    }

    override fun expandAppBar() {
        //Ignore
    }
}
