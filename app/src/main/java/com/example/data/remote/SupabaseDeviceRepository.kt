package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SupabaseDeviceRepository {
    private val tag = "SupabaseDeviceRepo"

    suspend fun registerDevicePublicKey(
        userId: String,
        publicKeyBase64: String,
        keyAlgorithm: String = "Ed25519",
        trustTier: String = "STANDARD"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                Log.d(tag, "Supabase not configured; skipping remote device registration")
                return@withContext Result.success(true)
            }

            val deviceId = java.util.UUID.nameUUIDFromBytes("$userId:$publicKeyBase64".toByteArray()).toString()
            val json = JSONObject().apply {
                put("id", deviceId)
                put("user_id", userId)
                put("public_key", publicKeyBase64)
                put("key_algorithm", keyAlgorithm)
                put("trust_tier", trustTier)
                put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()))
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/devices",
                method = "POST",
                body = body,
                extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates")
            ) ?: return@withContext Result.success(true)

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                Log.d(tag, "Device public key registered successfully in Supabase for user $userId")
                Result.success(true)
            } else {
                Log.w(tag, "Device public key registration warning: ${res.exceptionOrNull()?.message}")
                Result.failure(res.exceptionOrNull() ?: Exception("Device registration failed"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error registering device public key: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getDevicePublicKey(userId: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(null)

            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/devices?user_id=eq.$userId&select=public_key&order=created_at.desc&limit=1",
                method = "GET"
            ) ?: return@withContext Result.success(null)

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val array = JSONArray(res.getOrNull() ?: "[]")
                if (array.length() > 0) {
                    val key = array.getJSONObject(0).optString("public_key", null)
                    Result.success(key)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Device not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
