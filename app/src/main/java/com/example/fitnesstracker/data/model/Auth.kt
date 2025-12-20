package com.example.fitnesstracker.data.model

data class AuthRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val error: String?,
    val data: AuthData?
)

data class AuthData(
    val user: UserInfo?
)

data class UserInfo(
    val id: Int,
    val username: String?,
    val email: String?
)
