package com.example.fitnesstracker.data.remote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("USER_ID", userId).apply()
    }

    fun getUserId(): Int = prefs.getInt("USER_ID", -1)

    fun saveBodyInfo(weight: Float, height: Float, age: Int) {
        prefs.edit().apply {
            putFloat("WEIGHT", weight)
            putFloat("HEIGHT", height)
            putInt("AGE", age)
            apply()
        }
    }

    fun getWeight(): Float = prefs.getFloat("WEIGHT", 0f)
    fun getHeight(): Float = prefs.getFloat("HEIGHT", 0f)
    fun getAge(): Int = prefs.getInt("AGE", 0)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
