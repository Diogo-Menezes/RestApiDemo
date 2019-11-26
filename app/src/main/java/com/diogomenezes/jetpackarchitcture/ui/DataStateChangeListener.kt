package com.diogomenezes.jetpackarchitcture.ui

interface DataStateChangeListener {

    fun OnDataStateChange(dataState: DataState<*>?)

    fun expandAppBar()
}

