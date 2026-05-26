package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class User(
    val id: String,
    val username: String,
    val isOnline: Boolean = true
)

data class ChatMessageData(
    val senderId: String,
    val text: String,
    val timestamp: Long
)

interface FirebaseDatabaseApi {
    @PUT("users/{userId}.json")
    suspend fun createUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String,
        @Body user: User
    ): Response<User>

    @GET("users.json")
    suspend fun getUsers(
        @Query("auth") auth: String
    ): Response<Map<String, User>?>

    @retrofit2.http.POST("messages/{chatId}.json")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Query("auth") auth: String,
        @Body message: ChatMessageData
    ): Response<Any>

    @GET("messages/{chatId}.json")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("auth") auth: String
    ): Response<Map<String, ChatMessageData>?>
}

object FirebaseDatabaseService {
    private const val BASE_URL = "https://chatverse-898-default-rtdb.asia-southeast1.firebasedatabase.app/"

    val api: FirebaseDatabaseApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FirebaseDatabaseApi::class.java)
    }
}
