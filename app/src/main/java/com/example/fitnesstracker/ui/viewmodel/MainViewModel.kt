package com.example.fitnesstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.model.UserProfile
import com.example.fitnesstracker.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    fun fetchProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getProfile(userId)
                if (response.success) {
                    _userProfile.value = response.data?.profile
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
