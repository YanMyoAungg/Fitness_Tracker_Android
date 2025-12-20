package com.example.fitnesstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.model.FitnessRecord
import com.example.fitnesstracker.data.repository.FitnessRepository
import kotlinx.coroutines.launch

class FitnessViewModel : ViewModel() {

    private val repository = FitnessRepository()

    private val _addRecordResult = MutableLiveData<Boolean>()
    val addRecordResult: LiveData<Boolean> = _addRecordResult

    private val _history = MutableLiveData<List<FitnessRecord>>()
    val history: LiveData<List<FitnessRecord>> = _history

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun addRecord(userId: Int, type: String, duration: Int, calories: Int, date: String) {
        viewModelScope.launch {
            try {
                val response = repository.addRecord(userId, type, duration, calories, date)
                if (response.success) {
                    _addRecordResult.value = true
                } else {
                    _errorMessage.value = response.message ?: "Failed to add record"
                    _addRecordResult.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
                _addRecordResult.value = false
            }
        }
    }

    fun fetchHistory(userId: Int, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            try {
                val response = repository.getHistory(userId, startDate, endDate)
                if (response.success) {
                    _history.value = response.data?.activities ?: emptyList()
                } else {
                    _errorMessage.value = response.message ?: "Failed to fetch history"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            }
        }
    }
}
