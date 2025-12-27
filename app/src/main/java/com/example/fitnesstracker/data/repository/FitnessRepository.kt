package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.remote.RetrofitClient

class FitnessRepository {
    private val apiService = RetrofitClient.instance

    suspend fun addRecord(
        userId: Int,
        type: String,
        duration: Int,
        calories: Int,
        date: String,
        lat: Double?,
        lng: Double?,
        locName: String?
    ) = apiService.addFitnessRecord(userId, type, duration, calories, date, lat, lng, locName)

    suspend fun getHistory(userId: Int, startDate: String?, endDate: String?) =
        apiService.getFitnessHistory(userId, startDate, endDate)
}
