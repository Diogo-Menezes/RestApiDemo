package com.diogomenezes.jetpackarchitcture.session

import android.app.Application
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diogomenezes.jetpackarchitcture.model.AuthToken
import com.diogomenezes.jetpackarchitcture.persistance.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {
    val TAG: String = "SessionManager"

    private val _cachedToken = MutableLiveData<AuthToken>()
    val cachedToken: LiveData<AuthToken>
        get() = _cachedToken


    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun logout() {
        GlobalScope.launch(IO) {
            var errorMassage: String? = null
            try {

                cachedToken
                cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                    Log.d("SessionManager", "logout (line 40): nullifyToken...")
                }
            } catch (e: CancellationException) {
                errorMassage = e.message
                Log.e(TAG, "logout: $errorMassage");
            } catch (e: Exception) {
                errorMassage = e.message
                Log.e("SessionManager", "logout (line 43): $errorMassage")
            } finally {
                errorMassage?.let {
                    Log.e("SessionManager", "logout (line 48): $errorMassage")
                }
                setValue(null)
            }
        }
    }

    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if (_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }

    fun isConnectedToTheInternet(): Boolean {
        val cm = application.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return cm.activeNetworkInfo.isConnected
        } catch (e: Exception) {
            Log.e("SessionManager", "isConnectedToTheInternet (line 72): ${e.message}")
        }
        return false
    }
}