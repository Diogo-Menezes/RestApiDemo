package com.diogomenezes.jetpackarchitcture.repository

import android.util.Log
import kotlinx.coroutines.Job

open class JobManager(private val className: String) {

    private val jobs: HashMap<String, Job> = HashMap()

    fun addJob(methodName: String, job: Job) {
        cancelJob(methodName)
        jobs[methodName] = job
    }

    fun cancelJob(methodName: String) {
        getJob(methodName)?.cancel()
    }

    private fun getJob(methodName: String): Job? {
        Log.d("JobManager", "getJob (line 25):")
        if (jobs.containsKey(methodName)) {
            jobs[methodName]?.let { return it }
        }
        return null


//        return jobs.get(methodName)?.let { it } ?: null
    }

    fun cancelActiveJobs() {
        for ((methodName, job) in jobs) {
            if (job.isActive) {
                Log.e("JobManager", "$className (line 33): cancelling job in method: $methodName")
            }
        }
    }
}