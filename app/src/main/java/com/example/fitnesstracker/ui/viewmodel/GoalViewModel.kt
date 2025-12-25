package com.example.fitnesstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.model.GoalResponse
import com.example.fitnesstracker.data.repository.GoalRepository
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {

    private val repository = GoalRepository()

    private val _goalResult = MutableLiveData<GoalResponse?>()
    val goalResult: LiveData<GoalResponse?> = _goalResult

    private val _updateResult = MutableLiveData<GoalResponse?>()
    val updateResult: LiveData<GoalResponse?> = _updateResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchCurrentGoal(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getCurrentGoal(userId)
                _goalResult.value = response
            } catch (e: Exception) {
                // Return a failure response with 4 parameters to match Goal.kt
                _goalResult.value = GoalResponse(false, "No goal set", e.message, null)
            }
        }
    }

    fun setGoal(userId: Int, targetCalories: Int) {
        viewModelScope.launch {
            try {
                val response = repository.setGoal(userId, targetCalories)
                _updateResult.value = response
                
                if (response.success) {
                    fetchCurrentGoal(userId)
                } else {
                    _errorMessage.value = response.error ?: response.message ?: "Failed to update goal"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update goal"
            }
        }
    }

    fun resetResults() {
        _updateResult.value = null
        _errorMessage.value = null
    }
}
