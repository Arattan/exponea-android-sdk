package com.exponea.sdk.telemetry.upload

import com.exponea.sdk.telemetry.model.CrashLog
import com.exponea.sdk.telemetry.model.EventLog

internal interface TelemetryUpload {
    fun uploadSessionStart(runId: String, callback: (Result<Unit>) -> Unit)
    fun uploadCrashLog(log: CrashLog, callback: (Result<Unit>) -> Unit)
    fun uploadEventLog(log: EventLog, callback: (Result<Unit>) -> Unit)
}
