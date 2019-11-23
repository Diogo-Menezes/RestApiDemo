package com.diogomenezes.jetpackarchitcture.session

import android.app.Application
import com.diogomenezes.jetpackarchitcture.persistance.AuthTokenDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject constructor(val authToken: AuthTokenDao, val application: Application) {
}