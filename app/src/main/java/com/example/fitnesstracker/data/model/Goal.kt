package com.example.fitnesstracker.data.model

import com.google.gson.annotations.SerializedName

data class GoalResponse(
    val success: Boolean,
    val message: String?,
    val error: String?,
    val data: GoalData?
)

data class GoalData(
    @SerializedName("target_calories") val targetCalories: Int?,
    @SerializedName("current_calories") val currentCalories: Int?,
    @SerializedName("progress_percent") val progressPercent: Double?
)
