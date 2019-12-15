package com.diogomenezes.jetpackarchitcture.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.diogomenezes.jetpackarchitcture.BaseActivity
import com.diogomenezes.jetpackarchitcture.R
import com.diogomenezes.jetpackarchitcture.ui.auth.AuthActivity
import com.diogomenezes.jetpackarchitcture.ui.main.account.BaseAccountFragment
import com.diogomenezes.jetpackarchitcture.ui.main.account.ChangePasswordFragment
import com.diogomenezes.jetpackarchitcture.ui.main.account.UpdateAccountFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.BaseBlogFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.UpdateBlogFragment
import com.diogomenezes.jetpackarchitcture.ui.main.blog.ViewBlogFragment
import com.diogomenezes.jetpackarchitcture.ui.main.create_blog.BaseCreateBlogFragment
import com.diogomenezes.jetpackarchitcture.util.BottomNavController
import com.diogomenezes.jetpackarchitcture.util.BottomNavController.*
import com.diogomenezes.jetpackarchitcture.util.setUpNavigation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_auth.progress_bar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(),
    NavGraphProvider,
    OnNavigationGraphChanged,
    OnNavigationReselectedListener {
    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_blog,
            this,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }
        subscribeObservers()


    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer {
            Log.d("MainActivity", "subscribeObservers (line 19): token: $it")
            if (it == null || it.account_pk == -1 || it.token == null) {
                navAuthActivity()
            }
        })
    }

    private fun navAuthActivity() {
        startActivity(Intent(application, AuthActivity::class.java))
        finish()
    }

    override fun displayProgressBar(display: Boolean) {
        if (display) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility =
                View.INVISIBLE
        }
    }

    override fun getNavGraphId(itemId: Int): Int {
        Log.d("MainActivity", "getNavGraphId (line 82): called $itemId")
        return when (itemId) {
            R.id.nav_blog -> {
                R.navigation.nav_blog
            }
            R.id.nav_account -> {
                R.navigation.nav_account
            }
            R.id.nav_create_blog -> {
                R.navigation.nav_create_blog
            }
            else -> {
                R.navigation.nav_blog
            }
        }
    }

    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController
            .supportFragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments

        if (fragments != null) {
            for (fragment in fragments) {
                when (fragment) {
                    is BaseAccountFragment -> fragment.cancelActiveJobs()
                    is BaseBlogFragment -> fragment.cancelActiveJobs()
                    is BaseCreateBlogFragment -> fragment.cancelActiveJobs()

                }
            }
        }
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) {
        Log.d("MainActivity", "onReselectNavItem (line 104): called")
//        TODO(
//            "25/11/2019 - test if it works case not" +
//                    "add to the top = when(fragment"
//        )

        return when (fragment) {
            is ViewBlogFragment -> navController.navigate(R.id.action_viewBlogFragment_to_home)

            is UpdateBlogFragment -> navController.navigate(R.id.action_updateBlogFragment_to_home)

            is UpdateAccountFragment -> navController.navigate(R.id.action_updateAccountFragment_to_home)

            is ChangePasswordFragment -> navController.navigate(R.id.action_changePasswordFragment_to_home)

            else -> {

            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }
}