package com.diogomenezes.jetpackarchitcture.repository.auth

import com.diogomenezes.jetpackarchitcture.api.auth.OpenApiAuthService
import com.diogomenezes.jetpackarchitcture.persistance.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.persistance.AuthTokenDao
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager
) {

}