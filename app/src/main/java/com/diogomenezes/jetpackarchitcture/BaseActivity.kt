package com.diogomenezes.jetpackarchitcture

import dagger.android.support.DaggerAppCompatActivity

abstract class BaseActivity : DaggerAppCompatActivity() {
    val TAG: String = "BaseActivity"
}