package com.diogomenezes.jetpackarchitcture.ui.auth


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import kotlinx.android.synthetic.main.fragment_launcher.*

class LauncherFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeUI()

        launch_register.setOnClickListener {
            navRegistration()
        }
        launch_login.setOnClickListener {
            navLogin()
        }
        launch_forgot_password.setOnClickListener {
            navForgotPassword()
        }

        focusable_view.requestFocus()
    }

    private fun subscribeUI() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { authViewState ->
            if (authViewState?.authToken?.token == null) {
                Log.i("LauncherFragment", "subscribeUI: authtoken null")
                launch_login.visibility = View.VISIBLE
                launch_register.visibility = View.VISIBLE
                launch_forgot_password.visibility = View.VISIBLE
            } else {
                launch_login.visibility = View.INVISIBLE
                launch_register.visibility = View.INVISIBLE
                launch_forgot_password.visibility = View.INVISIBLE
                Log.i("LauncherFragment", "subscribeUI: authtoken not null")
            }
        })
    }

    private fun navForgotPassword() {
        this.findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
    }

    private fun navLogin() {
        this.findNavController().navigate(R.id.action_launcherFragment_to_loginFragment)
    }

    private fun navRegistration() {
        this.findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
    }


}
