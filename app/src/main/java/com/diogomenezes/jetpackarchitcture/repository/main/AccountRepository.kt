package com.diogomenezes.jetpackarchitcture.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.diogomenezes.jetpackarchitcture.api.GenericResponse
import com.diogomenezes.jetpackarchitcture.api.main.OpenApiMainService
import com.diogomenezes.jetpackarchitcture.model.AccountProperties
import com.diogomenezes.jetpackarchitcture.model.AuthToken
import com.diogomenezes.jetpackarchitcture.persistance.AccountPropertiesDao
import com.diogomenezes.jetpackarchitcture.repository.NetworkBoundResource
import com.diogomenezes.jetpackarchitcture.session.SessionManager
import com.diogomenezes.jetpackarchitcture.ui.DataState
import com.diogomenezes.jetpackarchitcture.ui.Response
import com.diogomenezes.jetpackarchitcture.ui.ResponseType
import com.diogomenezes.jetpackarchitcture.ui.main.account.state.AccountViewState
import com.diogomenezes.jetpackarchitcture.util.AbsentLiveData
import com.diogomenezes.jetpackarchitcture.util.ApiSuccessResponse
import com.diogomenezes.jetpackarchitcture.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {
    private var repositoryJob: Job? = null

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {

        val isConnected = sessionManager.isConnectedToTheInternet()
        Log.d("AccountRepository", "getAccountProperties (line 33): $isConnected")

        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                isConnected,
                true
            ) {
            override suspend fun createCacheRequestAndReturn() {

                withContext(Dispatchers.Main) {
                    //finish by

                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(
                            DataState.data(
                                data = viewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {

                updateLocalDb(response.body)

                createCacheRequestAndReturn()

            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties("Token ${authToken.token}")
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AccountViewState> {

                return accountPropertiesDao.searchByPk(authToken.account_pk!!).switchMap {
                    object : LiveData<AccountViewState>() {
                        override fun onActive() {
                            super.onActive()
                            value = AccountViewState(it)
                        }
                    }

                }

            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {

                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<GenericResponse, Any, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false
            ) {
            override suspend fun createCacheRequestAndReturn() {
            //Not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)

                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(
                                response.body.response,
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    "Token ${authToken.token}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                Log.d("AccountRepository", "updateLocalDb (line 130): called")
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()
    }

    fun cancelActiveJobs() {
        Log.d("AccountRepository", "cancelActiveJobs (line 20): Cancelling...")
    }
}