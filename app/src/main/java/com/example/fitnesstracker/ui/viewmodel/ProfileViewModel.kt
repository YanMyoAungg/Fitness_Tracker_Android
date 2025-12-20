package com.example.fitnesstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.model.UserProfile
import com.example.fitnesstracker.data.model.UserProfileResponse
import com.example.fitnesstracker.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _updateResult = MutableLiveData<UserProfileResponse>()
    val updateResult: LiveData<UserProfileResponse> = _updateResult

    fun fetchProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getProfile(userId)
                if (response.success) {
                    _profile.value = response.data?.profile
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProfile(userId: Int, height: Float?, weight: Float?, dob: String?, gender: String?, phone: String?) {
        viewModelScope.launch {
            try {
                val response = repository.updateProfile(userId, height, weight, dob, gender, phone)
                _updateResult.value = response
                if (response.success) {
                    _profile.value = response.data?.profile
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
