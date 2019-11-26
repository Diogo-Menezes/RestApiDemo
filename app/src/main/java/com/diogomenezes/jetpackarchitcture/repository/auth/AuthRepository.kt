package com.diogomenezes.jetpackarchitcture.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.diogomenezes.jetpackarchitcture.api.auth.OpenApiAuthService
import com.diogomenezes.jetpackarchitcture.api.auth.network_responses.LoginResponse
import com.diogomenezes.jetpackarchitcture.api.auth.network_responses.RegistrationResponse
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.model.AuthToken
import com.diogomenezes.jetpackarchitcture.persistance.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.persistance.AuthTokenDao
import com.diogomenezes.jetpackarchitcture.repository.NetworkBoundResource
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.ui.auth.state.AuthViewState
import com.diogomenezes.jetpackarchitcture.ui.auth.state.LoginFields
import com.diogomenezes.jetpackarchitcture.ui.auth.state.RegistrationFields
import com.diogomenezes.jetpackarchitcture.util.*
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.diogomenezes.jetpackarchitcture.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPreferencesEditor: SharedPreferences.Editor
) {

    /*
    //
//        return openApiAuthService.login(email, password)
//            .switchMap { response ->
//                object : LiveData<DataState<AuthViewState>>() {
//                    override fun onActive() {
//                        super.onActive()
//                        when (response) {
//
//                            is ApiSuccessResponse -> {
//                                value = DataState.data(
//                                    AuthViewState(
//                                        authToken = AuthToken(
//                                            response.body.pk,
//                                            response.body.token
//                                        )
//                                    ), response = null
//                                )
//                            }
//                            is ApiErrorResponse -> {
//                                value = DataState.error(
//                                    response = Response(
//                                        message = response.errorMessage,
//                                        responseType = ResponseType.Dialog()
//                                    )
//                                )
//                            }
//                            is ApiEmptyResponse -> {
//                                value = DataState.error(
//                                    response = Response(
//                                        message = ERROR_UNKNOWN,
//                                        responseType = ResponseType.Dialog()
//                                    )
//                                )
//                            }
//
//                        }
//                    }
//                }
//            }
    */
    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        Log.d("AuthRepository", "attemptLogin (line 82): called $email $password")
        val loginFieldsErrors = LoginFields(email, password).isValidForLogin()
        if (!loginFieldsErrors.equals(LoginFields.LoginError.none())) {
            return returnErrorResponse(loginFieldsErrors, ResponseType.Dialog())
        } else {
            return object :
                NetworkBoundResource<LoginResponse, Any, AuthViewState>(
                    sessionManager.isConnectedToTheInternet(),
                    true,
                    true,
                    false
                ) {
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                    Log.d("AuthRepository", "handleApiSuccessResponse (line 85): ${response} ")
                    if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                        return onErrorReturn(response.body.errorMessage, true, false)
                    }

                    accountPropertiesDao.insertOrIgnore(
                        AccountProperties(
                            response.body.pk,
                            response.body.email,
                            ""
                        )
                    )
                    val result = authTokenDao.insert(
                        AuthToken(
                            response.body.pk,
                            response.body.token
                        )
                    )
                    if (result < 0) {
                        return onCompleteJob(
                            DataState.error(
                                Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    ResponseType.Dialog()
                                )
                            )
                        )
                    }
                    saveAuthenticatedUserToPrefs(email)

                    onCompleteJob(
                        DataState.data(
                            data = AuthViewState(
                                authToken = AuthToken(response.body.pk, response.body.token)
                            )
                        )
                    )
                }

                override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                    return openApiAuthService.login(email, password)
                }

                override fun setJob(job: Job) {
                    repositoryJob?.cancel()
                    repositoryJob = job
                }

                override suspend fun createCacheRequestAndReturn() {
                    //Not used
                }

                override fun loadFromCache(): LiveData<AuthViewState> {
                    return AbsentLiveData.create()
                }

                override suspend fun updateLocalDb(cacheObject: Any?) {
//Not used 
                }
            }.asLiveData()
        }

    }


    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(
                "AuthRepository",
                "checkPreviousAuthUser (line 157): No previous auth user found.."
            )
            return returnNoTokenFound()
        }

        return object : NetworkBoundResource<Void, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            false,
            false,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {

                accountPropertiesDao.searchByEmail(previousAuthUserEmail)?.let {
                    Log.d(
                        "AuthRepository",
                        "createCacheRequestAndReturn (line 169): search for token: ${it}"
                    )
                    if (it.pk > -1) authTokenDao.searchByPk(it.pk).let { authToken ->
                        if (authToken != null) {
                            onCompleteJob(
                                DataState.data(
                                    data = AuthViewState(
                                        authToken = authToken
                                    )
                                )
                            )
                            return
                        }
                    }
                }
                Log.d(
                    "AuthRepository",
                    "createCacheRequestAndReturn (line 186): authtoken not found.. "
                )
                onCompleteJob(
                    DataState.data(
                        null,
                        response = Response(
                            SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                            ResponseType.None()
                        )
                    )
                )
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                //not used. Cache request
            }

            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
            //Not used
            }

        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(
                        SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                        ResponseType.None()
                    )
                )
            }
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPreferencesEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email).apply()
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType.Dialog
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }

    fun cancelActiveJobs() {
        Log.d("AuthRepository", "cancelActiveJobs (line 133): called")
        repositoryJob?.cancel()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        val registrationFieldsError =
            RegistrationFields(
                email,
                username,
                password,
                confirmPassword
            ).isValidForRegistration()
        if (!registrationFieldsError.equals(RegistrationFields.RegistrationError.none())) {
            return returnErrorResponse(registrationFieldsError, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {

                Log.d("AuthRepository", "handleApiSuccessResponse (line 153): ${response}")
                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_SAVE_AUTH_TOKEN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )

            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override suspend fun createCacheRequestAndReturn() {
                //Not used 
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                //Not used 
            }

        }.asLiveData()
    }
}