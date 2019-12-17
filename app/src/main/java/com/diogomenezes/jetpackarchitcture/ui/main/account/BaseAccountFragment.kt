package com.diogomenezes.jetpackarchitcture.ui.main.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.models.AccountProperties
import com.diogomenezes.jetpackarchitcture.ui.DataStateChangeListener
import com.diogomenezes.jetpackarchitcture.util.Constants
import com.diogomenezes.jetpackarchitcture.viewmodel.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject

abstract class BaseAccountFragment : DaggerFragment() {

    lateinit var stateChangeListener: DataStateChangeListener

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    lateinit var viewModel: AccountViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(
                this,
                viewModelProviderFactory
            ).get(AccountViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(
            R.id.accountFragment,
            activity as AppCompatActivity
        )

        cancelActiveJobs()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.d("BaseAccountFragment", "onAttach (line 61): Must implement listener in $context")
        }
    }
}
