package com.exponea.sdk.telemetry

import com.exponea.sdk.telemetry.model.CrashLog
import com.exponea.sdk.telemetry.storage.TelemetryStorage
import com.exponea.sdk.telemetry.upload.TelemetryUpload
import com.exponea.sdk.util.Logger
import java.util.Date
import java.util.LinkedList

internal class CrashManager(
    private val storage: TelemetryStorage,
    private val upload: TelemetryUpload,
    private val launchDate: Date,
    private val runId: String
) : Thread.UncaughtExceptionHandler {
    companion object {
        const val MAX_LOG_MESSAGES = 100
        const val LOG_RETENTION_MS = 1000 * 60 * 60 * 24 * 15 // 15 days
    }
    private var oldHandler: Thread.UncaughtExceptionHandler? = null
    private var latestLogMessages: LinkedList<String> = LinkedList()

    fun start() {
        Logger.i(this, "Starting crash manager")
        uploadCrashLogs()
        oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.i(this, "Handling uncaught exception(app crash)")
        handleException(e, true)
        oldHandler?.uncaughtException(t, e)
    }

    fun handleException(e: Throwable, fatal: Boolean) {
        try {
            val crashLog = CrashLog(
                e,
                fatal,
                Date(),
                launchDate,
                runId,
                latestLogMessages.toMutableList()
            )
            if (fatal) { // app is crashing, save exception, process it later
                if (TelemetryUtility.isSDKRelated(e)) {
                    Logger.i(this, "Fatal exception is sdk related, saving for later upload.")
                    storage.saveCrashLog(crashLog)
                }
            } else { // we should have time to immediately upload the exception
                upload.uploadCrashLog(crashLog) { result ->
                    if (result.isSuccess) {
                        Logger.i(this, "Crash log upload succeeded.")
                    } else {
                        Logger.i(this, "Crash log upload failed, will retry later.")
                        storage.saveCrashLog(crashLog)
                    }
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
    }

    @Synchronized fun saveLogMessage(parent: Any, message: String, timestamp: Long) {
        latestLogMessages.addFirst("${Date(timestamp)} ${parent.javaClass.simpleName}: $message")
        if (latestLogMessages.size > MAX_LOG_MESSAGES) {
            latestLogMessages.removeLast()
        }
    }

    private fun uploadCrashLogs() {
        try {
            storage.getAllCrashLogs().map { crashLog ->
                if (System.currentTimeMillis() - crashLog.timestampMS > LOG_RETENTION_MS) {
                    storage.deleteCrashLog(crashLog)
                    return@map
                }
                Logger.i(this, "Uploading crash log ${crashLog.id}")
                upload.uploadCrashLog(crashLog) { result ->
                    Logger.i(this, "Crash log upload ${if (result.isSuccess) "succeeded" else "failed" }")
                    if (result.isSuccess) {
                        storage.deleteCrashLog(crashLog)
                    }
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
    }
}
