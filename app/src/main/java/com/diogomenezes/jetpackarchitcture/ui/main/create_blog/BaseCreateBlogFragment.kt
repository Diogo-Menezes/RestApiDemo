package com.diogomenezes.jetpackarchitcture.ui.main.create_blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.DataStateChangeListener
import dagger.android.support.DaggerFragment

abstract class BaseCreateBlogFragment : DaggerFragment() {

    lateinit var stateChangeListener: DataStateChangeListener

    fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(
            R.id.createBlogFragment,
            activity as AppCompatActivity
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.d("BaseCreateBlogFragment", "onAttach (line 19): ")
        }
    }
}