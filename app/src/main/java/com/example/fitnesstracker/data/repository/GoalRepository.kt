package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.remote.RetrofitClient

class GoalRepository {
    private val apiService = RetrofitClient.instance

    suspend fun getCurrentGoal(userId: Int) = apiService.getCurrentGoal(userId)

    suspend fun setGoal(userId: Int, targetCalories: Int) = apiService.setGoal(userId, targetCalories)
}
