package com.example.fitnesstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.model.AuthResponse
import com.example.fitnesstracker.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthResult(val success: Boolean, val message: String, val userId: Int? = null)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<AuthResult?>()
    val loginResult: LiveData<AuthResult?> = _loginResult

    private val _registerResult = MutableLiveData<AuthResult?>()
    val registerResult: LiveData<AuthResult?> = _registerResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)
                
                // Matches nested: data -> user -> id
                val userId = response.data?.user?.id
                val isSuccess = response.success
                
                val msg = response.message ?: "Login successful"
                
                _loginResult.value = AuthResult(isSuccess, msg, userId)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = parseError(errorBody) ?: "Login failed"
                _loginResult.value = AuthResult(false, errorMsg)
            } catch (e: Exception) {
                _loginResult.value = AuthResult(false, e.message ?: "An unknown error occurred")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.register(username, email, password)
                
                val userId = response.data?.user?.id
                val isSuccess = response.success
                
                val msg = response.message ?: "Registration successful"
                _registerResult.value = AuthResult(isSuccess, msg, userId)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = parseError(errorBody) ?: "Registration failed"
                _registerResult.value = AuthResult(false, errorMsg)
            } catch (e: Exception) {
                _registerResult.value = AuthResult(false, e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetResults() {
        _loginResult.value = null
        _registerResult.value = null
    }

    private fun parseError(json: String?): String? {
        return try {
            val errorResponse = Gson().fromJson(json, AuthResponse::class.java)
            errorResponse.error ?: errorResponse.message
        } catch (e: Exception) {
            null
        }
    }
}
