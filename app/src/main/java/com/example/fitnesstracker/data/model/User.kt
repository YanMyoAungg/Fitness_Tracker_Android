package com.example.fitnesstracker.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    val success: Boolean,
    val message: String?,
    val data: UserProfileData?
)

data class UserProfileData(
    val profile: UserProfile?
)

data class UserProfile(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val email: String,
    val phone: String?,
    val height: Float?,
    @SerializedName("current_weight") val currentWeight: Float?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    val age: Int?,
    val gender: String?,
    val photo: String?
)
