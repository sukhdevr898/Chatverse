package com.example

import com.example.data.FirebaseAuthService
import org.junit.Test
import org.junit.Assert.*

class FirebaseApiTest {
    @Test
    fun checkApiInit() {
        val api = FirebaseAuthService.api
        assertNotNull(api)
    }
}
