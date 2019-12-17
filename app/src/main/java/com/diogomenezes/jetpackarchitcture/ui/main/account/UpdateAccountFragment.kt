package com.diogomenezes.jetpackarchitcture.ui.main.account


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.models.AccountProperties
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_update_account.*


class UpdateAccountFragment : BaseAccountFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            stateChangeListener.OnDataStateChange(it)
            Log.d("UpdateAccountFragment", "subscribeObservers (line 35): $it")
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it?.accountProperties?.let {
                setAccountDataFields(it)
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        input_email?.setText(accountProperties.email)
        input_username?.setText(accountProperties.username)
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountProperties(
                input_email.text.toString(),
                input_username.text.toString()
            )
        )
        stateChangeListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
