package com.diogomenezes.jetpackarchitcture.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : BaseAccountFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        logout_button.setOnClickListener {
            Log.d("AccountFragment", "onViewCreated (line 29): clicked")
            viewModel.logout()
        }
        change_password.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.OnDataStateChange(dataState)
            dataState?.let {
                it.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { accountViewState ->
                            accountViewState.accountProperties?.let {
                                Log.d(
                                    "AccountFragment",
                                    "subscribeObservers (line 46): DataState $it"
                                )
                                viewModel.setAccountPropertiesData(it)
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState?.let {
                it.accountProperties?.let {
                    Log.d("AccountFragment", "subscribeObservers (line 61): $it")
                    setAccountDataFields(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        email?.text = accountProperties.email
        username?.text = accountProperties.username
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(
            AccountStateEvent.GetAccountPropertiesEvent()
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


}