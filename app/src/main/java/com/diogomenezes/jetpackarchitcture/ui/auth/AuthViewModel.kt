package com.diogomenezes.jetpackarchitcture.ui.auth

import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.models.AuthToken
import com.diogomenezes.jetpackarchitcture.repository.auth.AuthRepository
import com.diogomenezes.jetpackarchitcture.viewmodel.BaseViewModel
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.auth.state.AuthStateEvent
import com.diogomenezes.jetpackarchitcture.ui.auth.state.AuthStateEvent.*
import com.diogomenezes.jetpackarchitcture.ui.auth.state.AuthViewState
import com.diogomenezes.jetpackarchitcture.ui.auth.state.LoginFields
import com.diogomenezes.jetpackarchitcture.ui.auth.state.RegistrationFields
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }


    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>>? {
        when (stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is RegisterAttemptEvent -> {
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }
            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
            is None ->
                return object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState.data(null, null)
                    }
                }
        }
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }


    fun cancelActiveJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}