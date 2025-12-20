package com.example.fitnesstracker.data.remote

import com.example.fitnesstracker.data.model.*
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("register.php")
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("activities.php")
    suspend fun addFitnessRecord(
        @Field("userId") userId: Int,
        @Field("activity_type") type: String,
        @Field("duration") duration: Int,
        @Field("calories_burned") calories: Int,
        @Field("activity_date") date: String
    ): FitnessSingleResponse

    @GET("activities.php")
    suspend fun getFitnessHistory(
        @Query("userId") userId: Int,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): FitnessListResponse

    @GET("profile.php")
    suspend fun getProfile(@Query("user_id") userId: Int): UserProfileResponse

    @FormUrlEncoded
    @POST("profile.php")
    suspend fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("height") height: Float?,
        @Field("current_weight") currentWeight: Float?,
        @Field("date_of_birth") dob: String?,
        @Field("gender") gender: String?,
        @Field("phone") phone: String?
    ): UserProfileResponse
}
