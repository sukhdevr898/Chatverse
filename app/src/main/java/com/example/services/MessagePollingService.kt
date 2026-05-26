package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.FirestoreService
import com.example.data.UserSession
import com.example.data.toChatMessage
import com.example.data.toUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessagePollingService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var lastSeenMessageTimestamps = mutableMapOf<String, Long>()
    private var isFirstPoll = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createForegroundNotification())
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "polling_service_channel",
                "Chat Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, "polling_service_channel")
            .setContentTitle("ChatVerse is running")
            .setContentText("Listening for new messages")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val token = UserSession.idToken
                    val currentUserId = UserSession.userId
                    if (token != null && currentUserId != null) {
                        pollMessages(token, currentUserId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    private suspend fun pollMessages(token: String, currentUserId: String) {
        val projectId = FirestoreService.getProjectIdFromToken(token)
        val friendsResponse = FirestoreService.api.getFriends(projectId, currentUserId, "Bearer $token")
        
        val friendsList = if (friendsResponse.isSuccessful) {
            friendsResponse.body()?.documents?.mapNotNull { it.name?.substringAfterLast("/") }?.toSet() ?: emptySet()
        } else return

        val response = FirestoreService.api.getUsers(projectId, "Bearer $token")
        if (response.isSuccessful) {
            val usersList = response.body()?.documents?.mapNotNull { it.toUser() } ?: emptyList()
            val friendsData = usersList.filter { friendsList.contains(it.id) }
            
            for (user in friendsData) {
                val chatId = if (currentUserId < user.id) "${currentUserId}_${user.id}" else "${user.id}_${currentUserId}"
                val msgResp = FirestoreService.api.getMessages(projectId, chatId, "Bearer $token")
                
                if (msgResp.isSuccessful) {
                    val msgs = msgResp.body()?.documents?.mapNotNull { it.toChatMessage() } ?: emptyList()
                    if (msgs.isNotEmpty()) {
                        val sorted = msgs.sortedBy { it.timestamp }
                        val latest = sorted.last()
                        
                        if (isFirstPoll) {
                            lastSeenMessageTimestamps[user.id] = latest.timestamp
                        } else {
                            val prevTime = lastSeenMessageTimestamps[user.id] ?: 0L
                            val newMsgsCount = sorted.count { it.senderId != currentUserId && it.timestamp > prevTime }
                            if (newMsgsCount > 0) {
                                com.example.NotificationHelper.showSystemNotification(applicationContext, user.username, latest.text)
                            }
                            lastSeenMessageTimestamps[user.id] = latest.timestamp
                        }
                    }
                }
            }
            isFirstPoll = false
        }
    }
}
