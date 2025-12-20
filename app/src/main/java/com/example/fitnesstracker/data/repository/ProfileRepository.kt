package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.remote.RetrofitClient

class ProfileRepository {
    private val apiService = RetrofitClient.instance

    suspend fun getProfile(userId: Int) = apiService.getProfile(userId)

    suspend fun updateProfile(userId: Int, height: Float?, weight: Float?, dob: String?, gender: String?, phone: String?) =
        apiService.updateProfile(userId, height, weight, dob, gender, phone)
}
