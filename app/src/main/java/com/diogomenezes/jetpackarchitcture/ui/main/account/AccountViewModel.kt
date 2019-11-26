package com.diogomenezes.jetpackarchitcture.ui.main.account

import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.repository.main.AccountRepository
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.BaseViewModel
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountStateEvent
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountStateEvent.*
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountViewState
import com.diogomenezes.jetpackarchitcture.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {


    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>>? {
        when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let {
                    accountRepository.getAccountProperties(it)
                } ?: AbsentLiveData.create()
            }
            is UpdateAccountProperties -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let {
                        accountRepository.saveAccountProperties(
                            authToken,
                            AccountProperties(
                                it,
                                stateEvent.email,
                                stateEvent.username
                            )
                        )
                    }
                } ?: AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                return AbsentLiveData.create()
            }
            is None -> {
                return AbsentLiveData.create()
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update

    }

    fun logout() {
        sessionManager.logout()
    }

}