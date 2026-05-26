package com.example.data

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private var prefs: SharedPreferences? = null
    var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    var userId: String?
        get() = prefs?.getString("userId", null)
        set(value) {
            prefs?.edit()?.putString("userId", value)?.apply()
        }

    var idToken: String?
        get() = prefs?.getString("idToken", null)
        set(value) {
            prefs?.edit()?.putString("idToken", value)?.apply()
        }

    var email: String?
        get() = prefs?.getString("email", null)
        set(value) {
            prefs?.edit()?.putString("email", value)?.apply()
        }
        
    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
