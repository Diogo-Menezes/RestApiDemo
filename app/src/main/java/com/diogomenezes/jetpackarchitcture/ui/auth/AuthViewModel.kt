package com.diogomenezes.jetpackarchitcture.ui.auth

import androidx.lifecycle.ViewModel
import com.diogomenezes.jetpackarchitcture.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : ViewModel() {

}