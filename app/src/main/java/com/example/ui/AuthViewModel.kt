package com.example.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AuthRequest
import com.example.data.FirebaseAuthService
import com.example.data.OobCodeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class MessageType { SUCCESS, ERROR, INFO }

data class IslandMessage(
    val id: Long,
    val text: String,
    val type: MessageType
)

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val apiKey = BuildConfig.FIREBASE_API_KEY
    val messages = mutableStateListOf<IslandMessage>()

    fun showMessage(text: String, type: MessageType = MessageType.INFO) {
        val message = IslandMessage(System.currentTimeMillis(), text, type)
        messages.add(message)
        viewModelScope.launch {
            delay(3000)
            messages.remove(message)
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    val json = JSONObject(errorBody ?: "")
                    json.getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "An unknown error occurred."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (apiKey.isEmpty() || apiKey == "MY_FIREBASE_API_KEY") {
            showMessage("Please configure FIREBASE_API_KEY in the Secrets panel.", MessageType.ERROR)
            return
        }
        if (email.isBlank() || password.isBlank()) {
            showMessage("Please fill all fields", MessageType.ERROR)
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                FirebaseAuthService.api.signIn(apiKey, AuthRequest(email, password))
            }
            _isLoading.value = false
            result.onSuccess {
                showMessage("Login Successful!", MessageType.SUCCESS)
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Login Failed", MessageType.ERROR)
            }
        }
    }

    fun signup(email: String, password: String, onSuccess: () -> Unit) {
        if (apiKey.isEmpty() || apiKey == "MY_FIREBASE_API_KEY") {
            showMessage("Please configure FIREBASE_API_KEY in the Secrets panel.", MessageType.ERROR)
            return
        }
        if (email.isBlank() || password.isBlank()) {
            showMessage("Please fill all fields", MessageType.ERROR)
            return
        }
        if (password.length < 6) {
            showMessage("Password must be at least 6 characters", MessageType.ERROR)
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                FirebaseAuthService.api.signUp(apiKey, AuthRequest(email, password))
            }
            _isLoading.value = false
            result.onSuccess {
                showMessage("Account Created Successfully!", MessageType.SUCCESS)
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Signup Failed", MessageType.ERROR)
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        if (apiKey.isEmpty() || apiKey == "MY_FIREBASE_API_KEY") {
            showMessage("Please configure FIREBASE_API_KEY in the Secrets panel.", MessageType.ERROR)
            return
        }
        if (email.isBlank()) {
            showMessage("Please enter your email", MessageType.ERROR)
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                FirebaseAuthService.api.sendPasswordReset(apiKey, OobCodeRequest(email = email))
            }
            _isLoading.value = false
            result.onSuccess {
                showMessage("Reset link sent!", MessageType.SUCCESS)
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Failed to send reset link", MessageType.ERROR)
            }
        }
    }
}
