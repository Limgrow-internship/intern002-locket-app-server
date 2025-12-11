package com.intern002.locketapp.core.utils

import java.util.concurrent.ConcurrentHashMap

object OtpStore {
    private val store = ConcurrentHashMap<String, OtpEntry>()

    data class OtpEntry(
        val code: String,
        val expiresAt: Long
    )

    fun saveOtp(email: String, code: String, lifeTimeMinutes: Int = 5) {
        val expiry = System.currentTimeMillis() + (lifeTimeMinutes * 60 * 1000)
        store[email] = OtpEntry(code, expiry)
    }

    fun verifyOtp(email: String, inputCode: String): Boolean {
        val entry = store[email] ?: return false

        if (System.currentTimeMillis() > entry.expiresAt) {
            store.remove(email)
            return false
        }

        if (entry.code == inputCode) {
            store.remove(email)
            return true
        }

        return false
    }
}