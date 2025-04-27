package com.littleb01s.ashasakhichat.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.littleb01s.ashasakhichat.data.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun saveUserProfile(profile: UserProfile) {
        val profileJson = gson.toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, profileJson).apply()
    }

    fun getUserProfile(): UserProfile? {
        val profileJson = prefs.getString(KEY_USER_PROFILE, null)
        return if (profileJson != null) {
            gson.fromJson(profileJson, UserProfile::class.java)
        } else null
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "asha_sakhi_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_PROFILE = "user_profile"
    }
} 