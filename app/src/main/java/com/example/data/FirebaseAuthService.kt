package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class AuthRequest(
    val email: String,
    val password: String? = null,
    val returnSecureToken: Boolean = true
)

data class AuthResponse(
    val idToken: String?,
    val email: String?,
    val refreshToken: String?,
    val expiresIn: String?,
    val localId: String?,
    val registered: Boolean?
)

data class OobCodeRequest(
    val requestType: String = "PASSWORD_RESET",
    val email: String
)

data class OobCodeResponse(
    val email: String?
)

interface FirebaseAuthApi {
    @POST("v1/accounts:signUp")
    suspend fun signUp(
        @Query("key") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("v1/accounts:signInWithPassword")
    suspend fun signIn(
        @Query("key") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("v1/accounts:sendOobCode")
    suspend fun sendPasswordReset(
        @Query("key") apiKey: String,
        @Body request: OobCodeRequest
    ): Response<OobCodeResponse>
}

object FirebaseAuthService {
    private const val BASE_URL = "https://identitytoolkit.googleapis.com/"
    
    val api: FirebaseAuthApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FirebaseAuthApi::class.java)
    }
}
