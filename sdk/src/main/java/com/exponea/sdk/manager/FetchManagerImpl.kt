package com.exponea.sdk.manager

import com.exponea.sdk.models.Consent
import com.exponea.sdk.models.CustomerIds
import com.exponea.sdk.models.CustomerRecommendation
import com.exponea.sdk.models.CustomerRecommendationRequest
import com.exponea.sdk.models.CustomerRecommendationResponse
import com.exponea.sdk.models.ExponeaProject
import com.exponea.sdk.models.FetchError
import com.exponea.sdk.models.InAppMessage
import com.exponea.sdk.models.Result
import com.exponea.sdk.network.ExponeaService
import com.exponea.sdk.util.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

internal class FetchManagerImpl(
    private val api: ExponeaService,
    private val gson: Gson
) : FetchManager {

    private fun <T> getFetchCallback(
        resultType: TypeToken<Result<T>>, // gson needs to know the type of result, T gets erased at compile time
        onSuccess: (Result<T>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ): Callback {
        return object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code()
                Logger.d(this, "Response Code: $responseCode")
                val jsonBody = response.body()?.string()
                if (response.isSuccessful) {
                    var result: Result<T>?
                    try {
                        result = gson.fromJson<Result<T>>(jsonBody, resultType.type)
                        if (result.results == null) {
                            throw Exception("Unable to parse response from the server.")
                        }
                    } catch (e: Exception) {
                        val error = FetchError(jsonBody, e.localizedMessage ?: "Unknown error")
                        Logger.e(this, "Failed to deserialize fetch response: $error")
                        onFailure(Result(false, error))
                        return
                    }
                    onSuccess(result) // we need to call onSuccess outside of pokemon exception handling above
                } else {
                    val error = FetchError(jsonBody, response.message())
                    Logger.e(this, "Failed to fetch data: $error")
                    onFailure(Result(false, error))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                val error = FetchError(null, e.localizedMessage ?: "Unknown error")
                Logger.e(this, "Fetch configuration Failed $e")
                onFailure(Result(false, error))
            }
        }
    }

    override fun fetchConsents(
        exponeaProject: ExponeaProject,
        onSuccess: (Result<ArrayList<Consent>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ) {
        api.postFetchConsents(exponeaProject).enqueue(
            getFetchCallback(
                object : TypeToken<Result<ArrayList<Consent>>>() {},
                onSuccess,
                onFailure
            )
        )
    }

    override fun fetchRecommendation(
        exponeaProject: ExponeaProject,
        recommendationRequest: CustomerRecommendationRequest,
        onSuccess: (Result<ArrayList<CustomerRecommendation>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ) {
        api.postFetchAttributes(exponeaProject, recommendationRequest).enqueue(
            getFetchCallback(
                object : TypeToken<Result<ArrayList<CustomerRecommendationResponse>>>() {},
                { result: Result<ArrayList<CustomerRecommendationResponse>> ->
                    if (result.results.isNotEmpty()) {
                        val innerResult = result.results[0]
                        if (innerResult.success && innerResult.value != null) {
                            onSuccess(Result(true, innerResult.value))
                        } else {
                            onFailure(Result(false, FetchError(null, innerResult.error ?: "Server returned error")))
                        }
                    } else {
                        onFailure(Result(false, FetchError(null, "Server returned empty results")))
                    }
                },
                onFailure
            )
        )
    }

    override fun fetchInAppMessages(
        exponeaProject: ExponeaProject,
        customerIds: CustomerIds,
        onSuccess: (Result<ArrayList<InAppMessage>>) -> Unit,
        onFailure: (Result<FetchError>) -> Unit
    ) {
        api.postFetchInAppMessages(exponeaProject, customerIds).enqueue(
            getFetchCallback(
                object : TypeToken<Result<ArrayList<InAppMessage>>>() {},
                onSuccess,
                onFailure
            )
        )
    }
}
