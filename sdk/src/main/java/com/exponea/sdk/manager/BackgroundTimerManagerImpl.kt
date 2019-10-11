package com.exponea.sdk.manager

import android.content.Context
import androidx.work.*
import com.exponea.sdk.models.ExponeaConfiguration
import com.exponea.sdk.repository.ExponeaConfigRepository
import com.exponea.sdk.services.ExponeaWorkRequest
import com.exponea.sdk.util.Logger
import java.util.concurrent.TimeUnit

/**
 * Handles background session tracking
 */
internal class BackgroundTimerManagerImpl(private val context: Context, private val configuration: ExponeaConfiguration) : BackgroundTimerManager {
    private val keyUniqueName = "KeyUniqueName"

    /**
     * Method will setup a timer  for a time period obtained from configuration
     */
    override fun startTimer() {
        ExponeaConfigRepository.set(context, configuration)
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // Build one time work request
        val workRequest = OneTimeWorkRequest
                .Builder(ExponeaWorkRequest::class.java)
                .setConstraints(constraints)
                .setInitialDelay(configuration.sessionTimeout.toLong(), TimeUnit.SECONDS)
                .build()

        // Enqueue request
        WorkManager
                .getInstance(context)
                .beginUniqueWork(
                        keyUniqueName,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                )
                .enqueue()

        Logger.d(this, "BackgroundTimerManagerImpl.start() -> enqueued background task...")
    }

    /**
     * Cancel timer that was set previously
     */
    override fun stopTimer() {
        Logger.d(this, "BackgroundTimerManagerImpl.stop() -> cancelling all work")
        WorkManager
                .getInstance(context)
                .cancelUniqueWork(keyUniqueName)
    }
}