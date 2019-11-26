package com.diogomenezes.jetpackarchitcture.ui.main.account.state

sealed class AccountStateEvent {

    class GetAccountPropertiesEvent : AccountStateEvent()

    data class UpdateAccountProperties(
        val email: String,
        val username: String
    ) : AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmPassword: String
    ) : AccountStateEvent()

    class None : AccountStateEvent()
}