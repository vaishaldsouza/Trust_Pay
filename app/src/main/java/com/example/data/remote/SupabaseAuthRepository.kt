package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SupabaseAuthRepository {
    private val tag = "SupabaseAuthRepo"

    suspend fun signIn(email: String, password: String): Result<RemoteUser> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                // Offline / Local Mock User
                val mockUser = RemoteUser(
                    id = "dev_buyer_01",
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = "BUYER"
                )
                SupabaseClient.currentUserId = mockUser.id
                return@withContext Result.success(mockUser)
            }

            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/auth/v1/token?grant_type=password",
                method = "POST",
                body = body
            ) ?: return@withContext Result.failure(Exception("Supabase not configured"))

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val respJson = JSONObject(res.getOrNull() ?: "{}")
                val token = respJson.optString("access_token", "")
                val userObj = respJson.optJSONObject("user")
                val userId = userObj?.optString("id", "") ?: ""

                SupabaseClient.authToken = token
                SupabaseClient.currentUserId = userId

                // Fetch public user profile
                val profileResult = getUserProfile(userId)
                val user = profileResult.getOrNull() ?: RemoteUser(
                    id = userId,
                    name = userObj?.optJSONObject("user_metadata")?.optString("name", email) ?: email,
                    role = userObj?.optJSONObject("user_metadata")?.optString("role", "BUYER") ?: "BUYER"
                )
                Result.success(user)
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Sign-in failed"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String, role: String): Result<RemoteUser> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                val mockUser = RemoteUser(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    role = role
                )
                SupabaseClient.currentUserId = mockUser.id
                return@withContext Result.success(mockUser)
            }

            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("name", name)
                    put("role", role)
                })
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/auth/v1/signup",
                method = "POST",
                body = body
            ) ?: return@withContext Result.failure(Exception("Supabase not configured"))

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val respJson = JSONObject(res.getOrNull() ?: "{}")
                val token = respJson.optString("access_token", null)
                val userObj = respJson.optJSONObject("user")
                val userId = userObj?.optString("id", "") ?: ""

                if (token != null) {
                    SupabaseClient.authToken = token
                    SupabaseClient.currentUserId = userId
                }

                // Insert into public users table
                createPublicUser(userId, name, role)

                Result.success(RemoteUser(id = userId, name = name, role = role))
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Signup failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPublicUser(userId: String, name: String, role: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(true)

            val json = JSONObject().apply {
                put("id", userId)
                put("name", name)
                put("role", role)
                put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()))
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/users",
                method = "POST",
                body = body,
                extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates")
            ) ?: return@withContext Result.success(true)

            SupabaseClient.execute(request).map { true }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<RemoteUser?> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(null)

            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/users?id=eq.$userId&select=id,name,role,created_at",
                method = "GET"
            ) ?: return@withContext Result.success(null)

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val array = org.json.JSONArray(res.getOrNull() ?: "[]")
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    Result.success(
                        RemoteUser(
                            id = obj.getString("id"),
                            name = obj.optString("name", "User"),
                            role = obj.optString("role", "BUYER"),
                            createdAt = obj.optString("created_at", null)
                        )
                    )
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        SupabaseClient.authToken = null
        SupabaseClient.currentUserId = null
    }
}
