package com.diogomenezes.jetpackarchitcture.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountStateEvent
import com.diogomenezes.jetpackarchitcture.util.SuccessHandling
import kotlinx.android.synthetic.main.fragment_change_password.*


class ChangePasswordFragment : BaseAccountFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_account_password, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        update_password_button.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    input_current_password.text.toString(),
                    input_new_password.text.toString(),
                    input_confirm_new_password.text.toString()
                )
            )
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.OnDataStateChange(dataState)
            Log.d("ChangePasswordFragment", "subscribeObservers (line 26): dataState:${dataState}")

            dataState.data?.let { data ->
                data.response?.let { event ->
                    if (event
                            .peekContent()
                            .message
                            .equals(SuccessHandling.RESPONSE_PASSWORD_UPDATE_SUCCESS)
                    ) {
                        stateChangeListener.hideSoftKeyboard()
                        findNavController().popBackStack()
                    }
                }
            }
        })
    }
}