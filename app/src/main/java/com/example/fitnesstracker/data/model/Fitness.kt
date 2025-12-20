package com.example.fitnesstracker.data.model

import com.google.gson.annotations.SerializedName

enum class ActivityType(val displayName: String, val databaseValue: String) {
    WALKING("Walking", "walking"),
    RUNNING("Running", "running"),
    SWIMMING("Swimming", "swimming"),
    JUMPING_ROPE("Jumping Rope", "jumping_rope"),
    CYCLING("Cycling", "cycling")
}

data class FitnessRecord(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("activity_type") val activityType: String,
    val duration: Int,
    @SerializedName("calories_burned") val caloriesBurned: Int,
    @SerializedName("activity_date") val activityDate: String
)

// Wrapper for the list response
data class FitnessListResponse(
    val success: Boolean,
    val message: String?,
    val data: FitnessListData?
)

data class FitnessListData(
    val count: Int,
    val activities: List<FitnessRecord>
)

// Wrapper for single record response (Create/Update)
data class FitnessSingleResponse(
    val success: Boolean,
    val message: String?,
    val data: FitnessSingleData?
)

data class FitnessSingleData(
    @SerializedName("activity_id") val activityId: Int?,
    val activity: FitnessRecord?
)
