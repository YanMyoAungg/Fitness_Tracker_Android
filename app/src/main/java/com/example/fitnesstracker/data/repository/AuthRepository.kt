package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.remote.RetrofitClient

class AuthRepository {

    private val apiService = RetrofitClient.instance

    suspend fun login(email: String, password: String) =
        apiService.login(email, password)

    suspend fun register(username: String, email: String, password: String) =
        apiService.register(username, email, password)
}
