package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.Base64
import org.json.JSONObject

// Firestore REST DTOs
data class FirestoreValue(
    val stringValue: String? = null,
    val booleanValue: Boolean? = null,
    val integerValue: String? = null
)

data class FirestoreDocument(
    val name: String? = null,
    val fields: Map<String, FirestoreValue>? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)

data class FirestoreListResponse(
    val documents: List<FirestoreDocument>? = null,
    val nextPageToken: String? = null
)

// Helper methods to convert between our model and Firestore DTO
fun User.toFirestore(): FirestoreDocument {
    return FirestoreDocument(
        fields = mapOf(
            "id" to FirestoreValue(stringValue = this.id),
            "username" to FirestoreValue(stringValue = this.username),
            "isOnline" to FirestoreValue(booleanValue = this.isOnline)
        )
    )
}

fun FirestoreDocument.toUser(): User? {
    val fields = this.fields ?: return null
    val id = fields["id"]?.stringValue ?: return null
    val username = fields["username"]?.stringValue ?: "Unknown"
    val isOnline = fields["isOnline"]?.booleanValue ?: false
    return User(id, username, isOnline)
}

fun FriendRequestData.toFirestore(): FirestoreDocument {
    return FirestoreDocument(
        fields = mapOf(
            "senderId" to FirestoreValue(stringValue = this.senderId),
            "senderUsername" to FirestoreValue(stringValue = this.senderUsername),
            "timestamp" to FirestoreValue(integerValue = this.timestamp.toString())
        )
    )
}

fun FirestoreDocument.toFriendRequest(): FriendRequestData? {
    val fields = this.fields ?: return null
    val senderId = fields["senderId"]?.stringValue ?: return null
    val senderUsername = fields["senderUsername"]?.stringValue ?: "Unknown"
    val timestamp = fields["timestamp"]?.integerValue?.toLongOrNull() ?: 0L
    return FriendRequestData(senderId, senderUsername, timestamp)
}

fun ChatMessageData.toFirestore(): FirestoreDocument {
    return FirestoreDocument(
        fields = mapOf(
            "senderId" to FirestoreValue(stringValue = this.senderId),
            "text" to FirestoreValue(stringValue = this.text),
            "timestamp" to FirestoreValue(integerValue = this.timestamp.toString())
        )
    )
}

fun FirestoreDocument.toChatMessage(): ChatMessageData? {
    val fields = this.fields ?: return null
    val senderId = fields["senderId"]?.stringValue ?: return null
    val text = fields["text"]?.stringValue ?: return null
    val timestamp = fields["timestamp"]?.integerValue?.toLongOrNull() ?: System.currentTimeMillis()
    return ChatMessageData(senderId, text, timestamp)
}

interface FirestoreApi {
    @POST("projects/{projectId}/databases/(default)/documents/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("projectId") projectId: String,
        @Path("chatId") chatId: String,
        @Header("Authorization") auth: String,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @GET("projects/{projectId}/databases/(default)/documents/chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("projectId") projectId: String,
        @Path("chatId") chatId: String,
        @Header("Authorization") auth: String,
        @Query("pageSize") pageSize: Int = 1000
    ): Response<FirestoreListResponse>

    @PATCH("projects/{projectId}/databases/(default)/documents/users/{userId}")
    suspend fun createUser(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") auth: String,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @GET("projects/{projectId}/databases/(default)/documents/users")
    suspend fun getUsers(
        @Path("projectId") projectId: String,
        @Header("Authorization") auth: String,
        @Query("pageSize") pageSize: Int = 1000
    ): Response<FirestoreListResponse>

    @PATCH("projects/{projectId}/databases/(default)/documents/friend_requests/{targetUserId}/requests/{senderId}")
    suspend fun sendFriendRequest(
        @Path("projectId") projectId: String,
        @Path("targetUserId") targetUserId: String,
        @Path("senderId") senderId: String,
        @Header("Authorization") auth: String,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @GET("projects/{projectId}/databases/(default)/documents/friend_requests/{userId}/requests")
    suspend fun getFriendRequests(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") auth: String
    ): Response<FirestoreListResponse>

    @DELETE("projects/{projectId}/databases/(default)/documents/friend_requests/{userId}/requests/{senderId}")
    suspend fun removeFriendRequest(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Path("senderId") senderId: String,
        @Header("Authorization") auth: String
    ): Response<Any>

    @PATCH("projects/{projectId}/databases/(default)/documents/friends/{userId}/user_friends/{friendId}")
    suspend fun addFriend(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Path("friendId") friendId: String,
        @Header("Authorization") auth: String,
        @Body document: FirestoreDocument
    ): Response<FirestoreDocument>

    @GET("projects/{projectId}/databases/(default)/documents/friends/{userId}/user_friends")
    suspend fun getFriends(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String,
        @Header("Authorization") auth: String
    ): Response<FirestoreListResponse>
}

object FirestoreService {
    private const val BASE_URL = "https://firestore.googleapis.com/v1/"
    
    val api: FirestoreApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FirestoreApi::class.java)
    }
    
    fun getProjectIdFromToken(idToken: String): String {
        try {
            val parts = idToken.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.getUrlDecoder().decode(parts[1]))
                val json = JSONObject(payload)
                return json.optString("aud", "chatverse-898")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "chatverse-898"
    }
}
