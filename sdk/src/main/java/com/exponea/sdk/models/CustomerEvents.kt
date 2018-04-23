package com.exponea.sdk.models

data class CustomerEvents(
        var eventTypes: MutableList<String>,
        var sortOrder: String = "desc",
        var limit: Int = 3,
        var skip: Int = 100
)