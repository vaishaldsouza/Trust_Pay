package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    var authToken: String? = null
    var currentUserId: String? = null

    val supabaseUrl: String
        get() = try {
            val url = BuildConfig.SUPABASE_URL
            if (url.isNotBlank() && !url.contains("your-project-id")) url.trimEnd('/') else ""
        } catch (e: Exception) {
            ""
        }

    val supabaseAnonKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (key.isNotBlank() && !key.contains("example_anon_key")) key else ""
        } catch (e: Exception) {
            ""
        }

    fun isConfigured(): Boolean {
        val url = supabaseUrl
        val key = supabaseAnonKey
        return url.isNotEmpty() && key.isNotEmpty() && url.startsWith("https://")
    }

    fun buildRequest(
        path: String,
        method: String = "GET",
        body: RequestBody? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): Request? {
        if (!isConfigured()) return null
        val url = if (path.startsWith("http")) path else "$supabaseUrl$path"
        val activeToken = authToken ?: supabaseAnonKey

        val requestBuilder = Request.Builder()
            .url(url)
            .header("apikey", supabaseAnonKey)
            .header("Authorization", "Bearer $activeToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        extraHeaders.forEach { (k, v) ->
            requestBuilder.header(k, v)
        }

        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post(body ?: "".toRequestBody(JSON_MEDIA_TYPE))
            "PATCH" -> requestBuilder.patch(body ?: "".toRequestBody(JSON_MEDIA_TYPE))
            "DELETE" -> requestBuilder.delete(body)
            "PUT" -> requestBuilder.put(body ?: "".toRequestBody(JSON_MEDIA_TYPE))
        }

        return requestBuilder.build()
    }

    suspend fun execute(request: Request): Result<String> {
        return try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(bodyString)
                } else {
                    Log.w(TAG, "Supabase HTTP error ${response.code}: $bodyString")
                    Result.failure(IOException("HTTP ${response.code}: $bodyString"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase request failed: ${e.message}")
            Result.failure(e)
        }
    }
}
