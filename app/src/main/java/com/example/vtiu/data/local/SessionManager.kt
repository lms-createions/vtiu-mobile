package com.example.vtiu.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("vtiu_prefs", Context.MODE_PRIVATE)

    fun saveSession(userId: String, role: String, userName: String? = null, profilePic: String? = null, numericId: Int = 0) {
        prefs.edit()
            .putString("user_id", userId)
            .putString("user_role", role)
            .putString("user_name", userName)
            .putString("profile_pic", profilePic)
            .putInt("numeric_id", numericId)
            .apply()
    }

    fun getUserId(): String? = prefs.getString("user_id", null)
    fun getUserRole(): String? = prefs.getString("user_role", null)
    fun getUserName(): String? = prefs.getString("user_name", null)
    fun getProfilePic(): String? = prefs.getString("profile_pic", null)
    fun getNumericId(): Int = prefs.getInt("numeric_id", 0)

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }

    fun logout() {
        prefs.edit()
            .remove("user_id")
            .remove("user_role")
            .apply()
    }
}
