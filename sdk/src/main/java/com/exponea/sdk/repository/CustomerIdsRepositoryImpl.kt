package com.exponea.sdk.repository

import com.exponea.sdk.models.CustomerIds
import com.exponea.sdk.preferences.ExponeaPreferences
import com.exponea.sdk.util.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class CustomerIdsRepositoryImpl(private val gson: Gson,
                                private val uuidRepo: UniqueIdentifierRepository,
                                private val prefs: ExponeaPreferences) :  CustomerIdsRepository {

    companion object {
        private const val PREFS_CUSTOMERIDS = "ExponeaCustomerIds"
    }


    override fun get(): CustomerIds {
        val uuid = uuidRepo.get()
        val json = prefs.getString(PREFS_CUSTOMERIDS, "{}")
        val type = object : TypeToken<HashMap<String, Any?>>() {}.type
        val ids = gson.fromJson<HashMap<String, Any?>>(json,  type)
        return CustomerIds().apply {
            cookie = uuid
            externalIds = ids
        }
    }

    override fun set(customerIds: CustomerIds) {
        val json = gson.toJson(customerIds.externalIds)
        prefs.setString(PREFS_CUSTOMERIDS, json)
    }

    override fun clear() {
        Logger.d(this, "Clearing customer ids")
        prefs.remove(PREFS_CUSTOMERIDS)
    }

}